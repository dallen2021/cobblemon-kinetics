import { mkdtemp, readFile, writeFile } from "node:fs/promises";
import { homedir } from "node:os";
import { tmpdir } from "node:os";
import { resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vitest";

import type {
  AssetManifest,
  PublicationBundlePayload,
  PublicNamedRecord,
  PublicPokemon,
  WorkProfile,
} from "@cobblemon-kinetics/domain";

import {
  applyPublicationBundle,
  createPublicationBundle,
  verifyPublishedData,
  verifyPublicationBundleIntegrity,
  validatePublicationOutputRoots,
} from "../src/export-publication/publication.js";

const fixtureRoot = fileURLToPath(new URL("../../../data/published/", import.meta.url));
async function fixture<T>(path: string): Promise<T> {
  return JSON.parse(await readFile(resolve(fixtureRoot, path), "utf8")) as T;
}

async function payload(): Promise<PublicationBundlePayload> {
  const collection = await fixture<{ pokemon: PublicPokemon[] }>("pokemon/gen1.json");
  return {
    bundle_version: 1,
    schema_version: "1.0.0",
    batch_id: "cobblemon_kinetics:test_publication",
    records: {
      pokemon: collection.pokemon,
      jobs: [await fixture<PublicNamedRecord>("jobs/hydro-operator.json")],
      machines: [await fixture<PublicNamedRecord>("machines/hydro-coupler.json")],
      work_profiles: [await fixture<WorkProfile>("work_profiles/hydro_operator.json")],
    },
    asset_manifest: await fixture<AssetManifest>("assets/manifest.json"),
  };
}

describe("publication pipeline", () => {
  it("requires a valid signature and produces byte-stable output", async () => {
    const key = "test-signing-key-that-is-not-a-production-secret";
    const bundle = createPublicationBundle(await payload(), key);
    expect(() => verifyPublicationBundleIntegrity(bundle, { signingKey: key })).not.toThrow();
    expect(() => verifyPublicationBundleIntegrity(bundle, { signingKey: "wrong" })).toThrow(
      /signature/,
    );

    const root = await mkdtemp(resolve(tmpdir(), "ck-publication-"));
    const published = resolve(root, "published");
    const profiles = resolve(root, "profiles");
    const first = await applyPublicationBundle(bundle, {
      publishedRoot: published,
      modWorkProfilesRoot: profiles,
      signingKey: key,
      allowExternalOutput: true,
    });
    const firstManifest = await readFile(resolve(published, "manifest.json"), "utf8");
    const second = await applyPublicationBundle(bundle, {
      publishedRoot: published,
      modWorkProfilesRoot: profiles,
      signingKey: key,
      allowExternalOutput: true,
    });
    expect(second).toEqual(first);
    expect(await readFile(resolve(published, "manifest.json"), "utf8")).toBe(firstManifest);
    expect(await verifyPublishedData(published, profiles)).toMatchObject({ ok: true, errors: [] });
  });

  it("detects public-data drift", async () => {
    const key = "test-signing-key-that-is-not-a-production-secret";
    const bundle = createPublicationBundle(await payload(), key);
    const root = await mkdtemp(resolve(tmpdir(), "ck-publication-drift-"));
    const published = resolve(root, "published");
    const profiles = resolve(root, "profiles");
    await applyPublicationBundle(bundle, {
      publishedRoot: published,
      modWorkProfilesRoot: profiles,
      signingKey: key,
      allowExternalOutput: true,
    });
    await writeFile(resolve(published, "jobs/hydro-operator.json"), "{}\n", "utf8");
    const verification = await verifyPublishedData(published, profiles);
    expect(verification.ok).toBe(false);
    expect(verification.errors.join(" ")).toMatch(/SHA-256|invalid job/);
  });

  it("rejects later-generation records until the exporter has generation-specific outputs", async () => {
    const key = "test-signing-key-that-is-not-a-production-secret";
    const root = await mkdtemp(resolve(tmpdir(), "ck-publication-generation-"));
    const publishedRoot = resolve(root, "published");
    const modWorkProfilesRoot = resolve(root, "profiles");
    await applyPublicationBundle(createPublicationBundle(await payload(), key), {
      publishedRoot,
      modWorkProfilesRoot,
      signingKey: key,
      allowExternalOutput: true,
    });
    const previousManifest = await readFile(resolve(publishedRoot, "manifest.json"), "utf8");

    const unsafePayload = await payload();
    unsafePayload.records.pokemon[0] = {
      ...unsafePayload.records.pokemon[0]!,
      generation: 2,
      national_dex: 152,
    };
    const bundle = createPublicationBundle(unsafePayload, key);

    await expect(
      applyPublicationBundle(bundle, {
        publishedRoot,
        modWorkProfilesRoot,
        signingKey: key,
        allowExternalOutput: true,
      }),
    ).rejects.toThrow(/accepts only Generation I species/u);
    expect(await readFile(resolve(publishedRoot, "manifest.json"), "utf8")).toBe(previousManifest);
    expect(await verifyPublishedData(publishedRoot, modWorkProfilesRoot)).toMatchObject({
      ok: true,
      errors: [],
    });
  });

  it("rejects broad, equal, nested, and traversal output roots before deletion", async () => {
    const repository = fileURLToPath(new URL("../../../", import.meta.url));
    const safePublished = resolve(repository, "data/published");
    const safeProfiles = resolve(
      repository,
      "src/main/resources/data/cobblemon_kinetics/work_profiles",
    );
    await expect(validatePublicationOutputRoots(safePublished, safeProfiles)).resolves.toEqual({
      publishedRoot: safePublished,
      modWorkProfilesRoot: safeProfiles,
    });
    await expect(validatePublicationOutputRoots("/", safeProfiles)).rejects.toThrow(/broad/);
    await expect(validatePublicationOutputRoots(homedir(), safeProfiles)).rejects.toThrow(/broad/);
    await expect(validatePublicationOutputRoots(repository, safeProfiles)).rejects.toThrow(/broad/);
    await expect(validatePublicationOutputRoots(safePublished, safePublished)).rejects.toThrow();
    await expect(
      validatePublicationOutputRoots(safePublished, resolve(safePublished, "profiles")),
    ).rejects.toThrow(/distinct and non-nested/);
    await expect(
      validatePublicationOutputRoots(resolve(repository, "../published"), safeProfiles),
    ).rejects.toThrow(/inside the repository/);
  });
});
