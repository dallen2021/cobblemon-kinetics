#!/usr/bin/env node
import { readFile } from "node:fs/promises";

import type { PublicationBundle } from "@cobblemon-kinetics/domain";

import { readAssetPolicy } from "../assets/asset-policy.js";
import { applyPublicationBundle } from "../export-publication/publication.js";
import { assertAllowedArgs, booleanFlag, optionalFlag, parseArgs } from "../lib/args.js";
import { repositoryDefaultPath } from "../lib/repository-paths.js";

async function main(): Promise<void> {
  const args = parseArgs(process.argv.slice(2));
  assertAllowedArgs(args, {
    flags: [
      "bundle",
      "allow-unsigned",
      "published-root",
      "mod-work-profiles-root",
      "asset-policy",
      "allow-external-output",
    ],
    maxPositionals: 1,
  });
  const allowUnsigned = booleanFlag(args, "allow-unsigned");
  const allowExternalOutput = booleanFlag(args, "allow-external-output");
  const bundlePath = optionalFlag(args, "bundle") ?? args.positional[0];
  if (!bundlePath) {
    throw new Error("Usage: pnpm data:apply -- --bundle <bundle.json> [--allow-unsigned]");
  }
  const bundle = JSON.parse(
    await readFile(repositoryDefaultPath(bundlePath, bundlePath), "utf8"),
  ) as PublicationBundle;
  const assetPolicy = await readAssetPolicy(
    repositoryDefaultPath(optionalFlag(args, "asset-policy"), "data/asset-policy.yml"),
  );
  const manifest = await applyPublicationBundle(bundle, {
    publishedRoot: repositoryDefaultPath(optionalFlag(args, "published-root"), "data/published"),
    modWorkProfilesRoot: repositoryDefaultPath(
      optionalFlag(args, "mod-work-profiles-root"),
      "src/main/resources/data/cobblemon_kinetics/work_profiles",
    ),
    ...(process.env.PUBLICATION_SIGNING_KEY
      ? { signingKey: process.env.PUBLICATION_SIGNING_KEY }
      : {}),
    allowUnsigned,
    allowExternalOutput,
    assetPolicy,
  });
  console.log(`Applied ${manifest.files.length} generated files for ${manifest.batch_id}.`);
}

main().catch((error: unknown) => {
  console.error(error instanceof Error ? error.message : String(error));
  process.exitCode = 1;
});
