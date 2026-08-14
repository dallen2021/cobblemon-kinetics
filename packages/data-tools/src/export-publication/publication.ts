import { randomUUID } from "node:crypto";
import { lstat, mkdir, readFile, readdir, realpath, rename, rm, writeFile } from "node:fs/promises";
import { homedir } from "node:os";
import { basename, dirname, parse, relative, resolve, sep } from "node:path";

import {
  assertPublicSafe,
  formatValidationErrors,
  validateAssetManifest,
  validatePublicationBundle,
  validatePublishedManifest,
  validatePublicNamedRecord,
  validatePublicPokemon,
  validateWorkProfile,
  type PublicationBundle,
  type PublicationBundlePayload,
  type PublishedManifest,
  type WorkProfile,
} from "@cobblemon-kinetics/domain";

import {
  canonicalJson,
  compactCanonicalJson,
  hmacSha256,
  safeHexEqual,
  sha256,
  type JsonValue,
} from "../lib/canonical-json.js";
import {
  validateAssetsAgainstPolicy,
  validatePublicAssetProjection,
  type AssetPolicy,
} from "../assets/asset-policy.js";
import {
  canonicalDirectoryTarget,
  isPathInside,
  repositoryRootPath,
} from "../lib/repository-paths.js";

export interface PublishedFile {
  path: string;
  sha256: string;
  kind: "pokemon_collection" | "job" | "machine" | "work_profile" | "asset_manifest";
  record_count: number;
}

export interface ApplyPublicationOptions {
  publishedRoot: string;
  modWorkProfilesRoot: string;
  signingKey?: string;
  allowUnsigned?: boolean;
  allowExternalOutput?: boolean;
  assetPolicy?: AssetPolicy;
}

export interface VerificationResult {
  ok: boolean;
  errors: string[];
  manifest?: PublishedManifest;
}

function payloadOf(bundle: PublicationBundle): PublicationBundlePayload {
  return {
    bundle_version: bundle.bundle_version,
    schema_version: bundle.schema_version,
    batch_id: bundle.batch_id,
    records: bundle.records,
    asset_manifest: bundle.asset_manifest,
  };
}

function compareText(left: string, right: string): number {
  return left < right ? -1 : left > right ? 1 : 0;
}

function normalizePublicationPayload(payload: PublicationBundlePayload): PublicationBundlePayload {
  return {
    ...payload,
    records: {
      pokemon: sortNamed(payload.records.pokemon),
      jobs: sortNamed(payload.records.jobs),
      machines: sortNamed(payload.records.machines),
      work_profiles: sortNamed(payload.records.work_profiles),
    },
    asset_manifest: {
      ...payload.asset_manifest,
      assets: [...payload.asset_manifest.assets].sort((left, right) =>
        compareText(left.asset_key, right.asset_key),
      ),
    },
  };
}

export function publicationContentHash(payload: PublicationBundlePayload): string {
  return sha256(compactCanonicalJson(normalizePublicationPayload(payload) as unknown as JsonValue));
}

export function createPublicationBundle(
  payload: PublicationBundlePayload,
  signingKey?: string,
): PublicationBundle {
  const normalized = normalizePublicationPayload(payload);
  assertPublicSafe(normalized);
  const contentSha256 = publicationContentHash(normalized);
  return {
    ...normalized,
    integrity: {
      content_sha256: contentSha256,
      ...(signingKey
        ? {
            signature: {
              algorithm: "hmac-sha256" as const,
              value: hmacSha256(contentSha256, signingKey),
            },
          }
        : {}),
    },
  };
}

export function verifyPublicationBundleIntegrity(
  bundle: PublicationBundle,
  options: { signingKey?: string; allowUnsigned?: boolean } = {},
): void {
  const validation = validatePublicationBundle(bundle);
  if (!validation.ok) {
    throw new Error(
      `Publication bundle schema is invalid: ${formatValidationErrors(validation.errors)}`,
    );
  }
  assertPublicSafe(bundle);
  const payload = payloadOf(bundle);
  const normalized = normalizePublicationPayload(payload);
  if (
    compactCanonicalJson(payload as unknown as JsonValue) !==
    compactCanonicalJson(normalized as unknown as JsonValue)
  ) {
    throw new Error("Publication bundle arrays are not in deterministic identifier order.");
  }
  const expectedHash = publicationContentHash(normalized);
  if (!safeHexEqual(bundle.integrity.content_sha256, expectedHash)) {
    throw new Error("Publication bundle content hash does not match its payload.");
  }

  const signature = bundle.integrity.signature;
  if (!signature) {
    if (!options.allowUnsigned) {
      throw new Error(
        "Publication bundle is unsigned. Pass --allow-unsigned only for reviewed local fixtures.",
      );
    }
    return;
  }
  if (!options.signingKey) {
    throw new Error(
      "PUBLICATION_SIGNING_KEY is required to verify this signed publication bundle.",
    );
  }
  const expectedSignature = hmacSha256(expectedHash, options.signingKey);
  if (!safeHexEqual(signature.value, expectedSignature)) {
    throw new Error("Publication bundle signature is invalid.");
  }
}

function safeSlug(value: string): string {
  if (value.length > 120 || !/^[a-z0-9]+(?:-[a-z0-9]+)*$/u.test(value)) {
    throw new Error(`Unsafe output slug: ${value}`);
  }
  return value;
}

function safeProfileName(profile: WorkProfile): string {
  const path = profile.id.split(":", 2)[1];
  if (!path || !/^[a-z0-9_./-]+$/u.test(path))
    throw new Error(`Unsafe work profile ID: ${profile.id}`);
  const name = path.split("/").at(-1);
  if (!name || !/^[a-z0-9](?:[a-z0-9_.-]*[a-z0-9])?$/u.test(name)) {
    throw new Error(`Work profile ID has no safe output name: ${profile.id}`);
  }
  return name;
}

function assertGeneratedDirectoryName(
  lexical: string,
  canonical: string,
  allowedNames: readonly string[],
): void {
  if (!allowedNames.includes(basename(lexical)) || !allowedNames.includes(basename(canonical))) {
    throw new Error(
      `Refusing to replace ${lexical}; generated directory must be named ${allowedNames.join(" or ")}.`,
    );
  }
}

export async function validatePublicationOutputRoots(
  publishedRoot: string,
  modWorkProfilesRoot: string,
  options: { allowExternalOutput?: boolean } = {},
): Promise<{ publishedRoot: string; modWorkProfilesRoot: string }> {
  const published = resolve(publishedRoot);
  const profiles = resolve(modWorkProfilesRoot);
  const canonicalPublished = await canonicalDirectoryTarget(published, "published output root");
  const canonicalProfiles = await canonicalDirectoryTarget(
    profiles,
    "mod work-profile output root",
  );
  const repository = resolve(repositoryRootPath());
  const canonicalRepository = await realpath(repository);
  const home = resolve(homedir());
  const canonicalHome = await realpath(home);

  for (const [label, lexical, canonical] of [
    ["published output root", published, canonicalPublished],
    ["mod work-profile output root", profiles, canonicalProfiles],
  ] as const) {
    if (
      lexical === parse(lexical).root ||
      canonical === parse(canonical).root ||
      lexical === home ||
      canonical === canonicalHome ||
      lexical === repository ||
      canonical === canonicalRepository
    ) {
      throw new Error(`Refusing broad ${label}: ${lexical}`);
    }
    if (!options.allowExternalOutput && !isPathInside(canonicalRepository, canonical)) {
      throw new Error(`${label} must remain inside the repository: ${lexical}`);
    }
  }

  assertGeneratedDirectoryName(published, canonicalPublished, ["published"]);
  assertGeneratedDirectoryName(profiles, canonicalProfiles, ["work_profiles", "profiles"]);
  if (
    canonicalPublished === canonicalProfiles ||
    isPathInside(canonicalPublished, canonicalProfiles) ||
    isPathInside(canonicalProfiles, canonicalPublished)
  ) {
    throw new Error("Published and mod work-profile output roots must be distinct and non-nested.");
  }
  return { publishedRoot: published, modWorkProfilesRoot: profiles };
}

async function rejectSymlink(path: string): Promise<void> {
  const stats = await lstat(path).catch((error: NodeJS.ErrnoException) => {
    if (error.code === "ENOENT") return undefined;
    throw error;
  });
  if (stats?.isSymbolicLink())
    throw new Error(`Refusing to replace generated symbolic link: ${path}`);
}

async function pathExists(path: string): Promise<boolean> {
  return lstat(path)
    .then(() => true)
    .catch((error: NodeJS.ErrnoException) => {
      if (error.code === "ENOENT") return false;
      throw error;
    });
}

async function installStagedDirectories(
  stagedPublishedRoot: string,
  publishedRoot: string,
  stagedModRoot: string,
  modRoot: string,
  transactionId: string,
): Promise<void> {
  const publishedBackup = resolve(
    dirname(publishedRoot),
    `.${basename(publishedRoot)}.backup-${transactionId}`,
  );
  const modBackup = resolve(dirname(modRoot), `.${basename(modRoot)}.backup-${transactionId}`);
  let publishedBackedUp = false;
  let publishedInstalled = false;
  let modBackedUp = false;
  let modInstalled = false;

  try {
    if (await pathExists(publishedRoot)) {
      await rejectSymlink(publishedRoot);
      await rename(publishedRoot, publishedBackup);
      publishedBackedUp = true;
    }
    await rename(stagedPublishedRoot, publishedRoot);
    publishedInstalled = true;

    if (await pathExists(modRoot)) {
      await rejectSymlink(modRoot);
      await rename(modRoot, modBackup);
      modBackedUp = true;
    }
    await rename(stagedModRoot, modRoot);
    modInstalled = true;
  } catch (error) {
    if (modInstalled) await rm(modRoot, { recursive: true, force: true });
    if (modBackedUp) await rename(modBackup, modRoot);
    if (publishedInstalled) await rm(publishedRoot, { recursive: true, force: true });
    if (publishedBackedUp) await rename(publishedBackup, publishedRoot);
    throw error;
  }

  if (publishedBackedUp) {
    await rm(publishedBackup, { recursive: true, force: true }).catch(() => undefined);
  }
  if (modBackedUp) {
    await rm(modBackup, { recursive: true, force: true }).catch(() => undefined);
  }
}

function duplicateValue(values: readonly string[]): string | undefined {
  return values.find((value, index) => values.indexOf(value) !== index);
}

function asNonEmpty<T>(items: T[]): [T, ...T[]] {
  if (items.length === 0)
    throw new Error("A publication must contain at least one generated file.");
  return items as [T, ...T[]];
}

function sortNamed<T extends { public_id?: string; id?: string; national_dex?: number }>(
  items: readonly T[],
): T[] {
  return [...items].sort((left, right) => {
    if (left.national_dex !== undefined || right.national_dex !== undefined) {
      return (
        (left.national_dex ?? Number.MAX_SAFE_INTEGER) -
        (right.national_dex ?? Number.MAX_SAFE_INTEGER)
      );
    }
    return compareText(left.public_id ?? left.id ?? "", right.public_id ?? right.id ?? "");
  });
}

function recordRelationshipErrors(records: PublicationBundlePayload["records"]): string[] {
  const errors: string[] = [];
  const identifiers = [
    ...records.pokemon.map((record) => record.public_id),
    ...records.jobs.map((record) => record.public_id),
    ...records.machines.map((record) => record.public_id),
    ...records.work_profiles.map((record) => record.id),
  ];
  const duplicateIdentifier = duplicateValue(identifiers);
  if (duplicateIdentifier) errors.push(`Duplicate public identifier: ${duplicateIdentifier}`);

  for (const [label, values] of [
    ["Pokemon slug", records.pokemon.map((record) => record.slug)],
    ["National Dex number", records.pokemon.map((record) => String(record.national_dex))],
    ["Cobblemon ID", records.pokemon.map((record) => record.cobblemon_id)],
    ["Pokemon form ID", records.pokemon.map((record) => record.form.public_id)],
    ["job output slug", records.jobs.map((record) => record.slug)],
    ["machine output slug", records.machines.map((record) => record.slug)],
    ["work-profile output name", records.work_profiles.map(safeProfileName)],
  ] as const) {
    const duplicate = duplicateValue(values);
    if (duplicate) errors.push(`Duplicate ${label}: ${duplicate}`);
  }

  const profiles = new Map(records.work_profiles.map((profile) => [profile.id, profile] as const));
  for (const pokemon of records.pokemon) {
    for (const assignment of pokemon.work_assignments) {
      const profile = profiles.get(assignment.work_profile_id);
      if (!profile) {
        errors.push(
          `${pokemon.public_id}: assignment references missing profile ${assignment.work_profile_id}`,
        );
      } else if (!profile.workstation.registry_ids.includes(assignment.machine_registry_id)) {
        errors.push(
          `${pokemon.public_id}: machine ${assignment.machine_registry_id} is not supported by ${profile.id}`,
        );
      }
    }
  }
  return errors;
}

async function writeTrackedJson(
  root: string,
  path: string,
  value: JsonValue,
  kind: PublishedFile["kind"],
  recordCount: number,
): Promise<PublishedFile> {
  const absolute = resolve(root, path);
  const rootPrefix = `${resolve(root)}${sep}`;
  if (!absolute.startsWith(rootPrefix))
    throw new Error(`Output path escaped published root: ${path}`);
  const contents = canonicalJson(value);
  await mkdir(dirname(absolute), { recursive: true });
  await writeFile(absolute, contents, "utf8");
  return {
    path: path.replaceAll(sep, "/"),
    sha256: sha256(contents),
    kind,
    record_count: recordCount,
  };
}

function validateRecords(bundle: PublicationBundle, assetPolicy?: AssetPolicy): void {
  for (const pokemon of bundle.records.pokemon) {
    const result = validatePublicPokemon(pokemon);
    if (!result.ok)
      throw new Error(`${pokemon.public_id}: ${formatValidationErrors(result.errors)}`);
    if (pokemon.generation !== 1 || pokemon.national_dex > 151) {
      throw new Error(
        `${pokemon.public_id}: the current pokemon/gen1.json exporter accepts only Generation I species (National Dex 1-151).`,
      );
    }
  }
  for (const profile of bundle.records.work_profiles) {
    const result = validateWorkProfile(profile);
    if (!result.ok) throw new Error(`${profile.id}: ${formatValidationErrors(result.errors)}`);
  }
  const assetResult = validateAssetManifest(bundle.asset_manifest);
  if (!assetResult.ok)
    throw new Error(`Asset manifest: ${formatValidationErrors(assetResult.errors)}`);
  const projectionErrors = validatePublicAssetProjection(bundle.asset_manifest);
  if (projectionErrors.length > 0) {
    throw new Error(`Asset publication projection is invalid: ${projectionErrors.join("; ")}`);
  }
  if (bundle.asset_manifest.assets.length > 0 && !assetPolicy) {
    throw new Error(
      "A deny-by-default asset policy is required when a publication contains assets.",
    );
  }
  if (assetPolicy) {
    const assetErrors = validateAssetsAgainstPolicy(bundle.asset_manifest, assetPolicy);
    if (assetErrors.length > 0)
      throw new Error(`Asset policy rejected publication: ${assetErrors.join("; ")}`);
  }
  const relationshipErrors = recordRelationshipErrors(bundle.records);
  if (relationshipErrors.length > 0) {
    throw new Error(`Publication relationships are invalid: ${relationshipErrors.join("; ")}`);
  }
  const duplicateAssetKey = duplicateValue(
    bundle.asset_manifest.assets.map((asset) => asset.asset_key),
  );
  if (duplicateAssetKey) throw new Error(`Duplicate asset key: ${duplicateAssetKey}`);
}

export async function applyPublicationBundle(
  bundle: PublicationBundle,
  options: ApplyPublicationOptions,
): Promise<PublishedManifest> {
  verifyPublicationBundleIntegrity(bundle, options);
  validateRecords(bundle, options.assetPolicy);

  const validatedRoots = await validatePublicationOutputRoots(
    options.publishedRoot,
    options.modWorkProfilesRoot,
    options.allowExternalOutput ? { allowExternalOutput: true } : {},
  );
  const finalPublishedRoot = validatedRoots.publishedRoot;
  const finalModRoot = validatedRoots.modWorkProfilesRoot;
  const transactionId = randomUUID();
  const publishedRoot = resolve(
    dirname(finalPublishedRoot),
    `.${basename(finalPublishedRoot)}.staging-${transactionId}`,
  );
  const modRoot = resolve(
    dirname(finalModRoot),
    `.${basename(finalModRoot)}.staging-${transactionId}`,
  );
  await mkdir(dirname(publishedRoot), { recursive: true });
  await mkdir(dirname(modRoot), { recursive: true });
  await mkdir(publishedRoot);
  await mkdir(modRoot);

  try {
    const files: PublishedFile[] = [];
    const pokemon = sortNamed(bundle.records.pokemon);
    files.push(
      await writeTrackedJson(
        publishedRoot,
        "pokemon/gen1.json",
        { format_version: 1, generation: 1, pokemon } as unknown as JsonValue,
        "pokemon_collection",
        pokemon.length,
      ),
    );

    for (const job of sortNamed(bundle.records.jobs)) {
      files.push(
        await writeTrackedJson(
          publishedRoot,
          `jobs/${safeSlug(job.slug)}.json`,
          job as unknown as JsonValue,
          "job",
          1,
        ),
      );
    }
    for (const machine of sortNamed(bundle.records.machines)) {
      files.push(
        await writeTrackedJson(
          publishedRoot,
          `machines/${safeSlug(machine.slug)}.json`,
          machine as unknown as JsonValue,
          "machine",
          1,
        ),
      );
    }
    for (const profile of sortNamed(bundle.records.work_profiles)) {
      const name = safeProfileName(profile);
      files.push(
        await writeTrackedJson(
          publishedRoot,
          `work_profiles/${name}.json`,
          profile as unknown as JsonValue,
          "work_profile",
          1,
        ),
      );
      await writeFile(
        resolve(modRoot, `${name}.json`),
        canonicalJson(profile as unknown as JsonValue),
        "utf8",
      );
    }
    files.push(
      await writeTrackedJson(
        publishedRoot,
        "assets/manifest.json",
        bundle.asset_manifest as unknown as JsonValue,
        "asset_manifest",
        bundle.asset_manifest.assets.length,
      ),
    );
    files.sort((left, right) => compareText(left.path, right.path));

    const manifest: PublishedManifest = {
      manifest_version: 1,
      schema_version: bundle.schema_version,
      batch_id: bundle.batch_id,
      bundle_content_sha256: bundle.integrity.content_sha256,
      files: asNonEmpty(files),
    };
    await writeFile(
      resolve(publishedRoot, "manifest.json"),
      canonicalJson(manifest as unknown as JsonValue),
      "utf8",
    );
    const stagedVerification = await verifyPublishedData(publishedRoot, modRoot);
    if (!stagedVerification.ok) {
      throw new Error(
        `Staged publication failed self-verification: ${stagedVerification.errors.join("; ")}`,
      );
    }
    await installStagedDirectories(
      publishedRoot,
      finalPublishedRoot,
      modRoot,
      finalModRoot,
      transactionId,
    );
    return manifest;
  } catch (error) {
    await rm(publishedRoot, { recursive: true, force: true });
    await rm(modRoot, { recursive: true, force: true });
    throw error;
  }
}

async function listJsonFiles(root: string): Promise<string[]> {
  const files: string[] = [];
  async function visit(directory: string): Promise<void> {
    const entries = await readdir(directory, { withFileTypes: true }).catch(() => []);
    for (const entry of entries) {
      const absolute = resolve(directory, entry.name);
      if (entry.isDirectory()) await visit(absolute);
      else if (entry.isFile() && entry.name.endsWith(".json")) {
        files.push(relative(root, absolute).split(sep).join("/"));
      }
    }
  }
  await visit(root);
  return files.sort(compareText);
}

function pathMatchesKind(entry: PublishedFile): boolean {
  switch (entry.kind) {
    case "pokemon_collection":
      return entry.path === "pokemon/gen1.json";
    case "asset_manifest":
      return entry.path === "assets/manifest.json";
    case "job":
      return /^jobs\/[a-z0-9]+(?:-[a-z0-9]+)*\.json$/u.test(entry.path);
    case "machine":
      return /^machines\/[a-z0-9]+(?:-[a-z0-9]+)*\.json$/u.test(entry.path);
    case "work_profile":
      return /^work_profiles\/[a-z0-9](?:[a-z0-9_.-]*[a-z0-9])?\.json$/u.test(entry.path);
  }
}

export async function verifyPublishedData(
  publishedRoot: string,
  modWorkProfilesRoot?: string,
): Promise<VerificationResult> {
  const root = resolve(publishedRoot);
  const errors: string[] = [];
  let manifest: PublishedManifest;
  try {
    manifest = JSON.parse(
      await readFile(resolve(root, "manifest.json"), "utf8"),
    ) as PublishedManifest;
  } catch (error) {
    return { ok: false, errors: [`Cannot read published manifest: ${String(error)}`] };
  }
  const manifestValidation = validatePublishedManifest(manifest);
  if (!manifestValidation.ok) {
    return {
      ok: false,
      errors: [
        `Published manifest is invalid: ${formatValidationErrors(manifestValidation.errors)}`,
      ],
    };
  }

  const expectedFiles = new Set(["manifest.json", ...manifest.files.map((entry) => entry.path)]);
  if (expectedFiles.size !== manifest.files.length + 1)
    errors.push("Published manifest contains duplicate paths.");
  const manifestPaths = manifest.files.map((entry) => entry.path);
  const sortedManifestPaths = [...manifestPaths].sort(compareText);
  if (manifestPaths.some((path, index) => path !== sortedManifestPaths[index])) {
    errors.push("Published manifest file entries are not in deterministic path order.");
  }
  const actualFiles = await listJsonFiles(root);
  for (const extra of actualFiles.filter((file) => !expectedFiles.has(file)))
    errors.push(`Untracked JSON file: ${extra}`);
  for (const missing of [...expectedFiles].filter((file) => !actualFiles.includes(file)))
    errors.push(`Missing JSON file: ${missing}`);

  const reconstructed: PublicationBundlePayload["records"] = {
    pokemon: [],
    jobs: [],
    machines: [],
    work_profiles: [],
  };
  let reconstructedAssets: PublicationBundlePayload["asset_manifest"] | undefined;
  for (const entry of manifest.files) {
    if (entry.path.startsWith("/") || entry.path.split("/").includes("..")) {
      errors.push(`${entry.path}: manifest path is unsafe.`);
      continue;
    }
    if (!pathMatchesKind(entry)) {
      errors.push(`${entry.path}: path does not match manifest kind ${entry.kind}.`);
      continue;
    }
    let raw: string;
    let value: unknown;
    try {
      raw = await readFile(resolve(root, entry.path), "utf8");
      value = JSON.parse(raw);
    } catch (error) {
      errors.push(`${entry.path}: cannot read valid JSON (${String(error)})`);
      continue;
    }
    if (sha256(raw) !== entry.sha256) errors.push(`${entry.path}: SHA-256 differs from manifest.`);
    try {
      assertPublicSafe(value);
    } catch (error) {
      errors.push(`${entry.path}: ${error instanceof Error ? error.message : String(error)}`);
    }
    if (entry.kind === "pokemon_collection") {
      const collection = value as {
        format_version?: unknown;
        generation?: unknown;
        pokemon?: unknown;
      };
      if (
        collection.format_version !== 1 ||
        collection.generation !== 1 ||
        !Array.isArray(collection.pokemon)
      ) {
        errors.push(`${entry.path}: invalid Generation I collection wrapper.`);
      } else {
        for (const pokemon of collection.pokemon) {
          const result = validatePublicPokemon(pokemon);
          if (!result.ok) errors.push(`${entry.path}: ${formatValidationErrors(result.errors)}`);
          else if (result.value!.generation !== 1 || result.value!.national_dex > 151) {
            errors.push(
              `${entry.path}: contains a species outside Generation I (National Dex 1-151).`,
            );
          } else reconstructed.pokemon.push(result.value!);
        }
        if (collection.pokemon.length !== entry.record_count)
          errors.push(`${entry.path}: record count differs from manifest.`);
      }
    } else if (entry.kind === "work_profile") {
      const result = validateWorkProfile(value);
      if (!result.ok) errors.push(`${entry.path}: ${formatValidationErrors(result.errors)}`);
      else reconstructed.work_profiles.push(result.value!);
      if (modWorkProfilesRoot) {
        const modPath = resolve(modWorkProfilesRoot, entry.path.split("/").at(-1) ?? "");
        const modRaw = await readFile(modPath, "utf8").catch(() => undefined);
        if (!modRaw) errors.push(`${entry.path}: generated mod profile is missing.`);
        else if (modRaw !== raw)
          errors.push(`${entry.path}: generated mod profile differs from published profile.`);
      }
      if (entry.record_count !== 1)
        errors.push(`${entry.path}: work-profile record count must be one.`);
    } else if (entry.kind === "asset_manifest") {
      const result = validateAssetManifest(value);
      if (!result.ok) errors.push(`${entry.path}: ${formatValidationErrors(result.errors)}`);
      else {
        reconstructedAssets = result.value!;
        if (result.value!.assets.length !== entry.record_count) {
          errors.push(`${entry.path}: record count differs from manifest.`);
        }
        for (const error of validatePublicAssetProjection(result.value!)) {
          errors.push(`${entry.path}: ${error}`);
        }
      }
    } else {
      const result = validatePublicNamedRecord(value);
      if (!result.ok) {
        errors.push(`${entry.path}: ${formatValidationErrors(result.errors)}`);
      } else if (entry.kind === "job") {
        reconstructed.jobs.push(result.value!);
      } else if (entry.kind === "machine") {
        reconstructed.machines.push(result.value!);
      }
      if (entry.record_count !== 1) errors.push(`${entry.path}: named-record count must be one.`);
    }
  }

  if (!reconstructedAssets) {
    errors.push("Published asset manifest is missing or invalid.");
  } else {
    const reconstructedPayload: PublicationBundlePayload = {
      bundle_version: 1,
      schema_version: manifest.schema_version,
      batch_id: manifest.batch_id,
      records: {
        pokemon: sortNamed(reconstructed.pokemon),
        jobs: sortNamed(reconstructed.jobs),
        machines: sortNamed(reconstructed.machines),
        work_profiles: sortNamed(reconstructed.work_profiles),
      },
      asset_manifest: reconstructedAssets,
    };
    for (const error of recordRelationshipErrors(reconstructedPayload.records)) {
      errors.push(`Published record relationship: ${error}`);
    }
    const assetKeys = reconstructedAssets.assets.map((asset) => asset.asset_key);
    const sortedAssetKeys = [...assetKeys].sort(compareText);
    if (assetKeys.some((key, index) => key !== sortedAssetKeys[index])) {
      errors.push("Published asset manifest entries are not in deterministic asset-key order.");
    }
    const reconstructedHash = publicationContentHash(reconstructedPayload);
    if (!safeHexEqual(manifest.bundle_content_sha256, reconstructedHash)) {
      errors.push("Published content does not match the publication bundle hash.");
    }
  }

  return { ok: errors.length === 0, errors, manifest };
}
