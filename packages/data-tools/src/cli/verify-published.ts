#!/usr/bin/env node
import { assertAllowedArgs, optionalFlag, parseArgs } from "../lib/args.js";
import { verifyPublishedData } from "../export-publication/publication.js";
import { repositoryDefaultPath } from "../lib/repository-paths.js";

async function main(): Promise<void> {
  const args = parseArgs(process.argv.slice(2));
  assertAllowedArgs(args, {
    flags: ["published-root", "mod-work-profiles-root"],
  });
  const result = await verifyPublishedData(
    repositoryDefaultPath(optionalFlag(args, "published-root"), "data/published"),
    repositoryDefaultPath(
      optionalFlag(args, "mod-work-profiles-root"),
      "src/main/resources/data/cobblemon_kinetics/work_profiles",
    ),
  );
  if (!result.ok) {
    for (const error of result.errors) console.error(`- ${error}`);
    process.exitCode = 1;
    return;
  }
  console.log(`Published data verified: ${result.manifest?.files.length ?? 0} generated files.`);
}

main().catch((error: unknown) => {
  console.error(error instanceof Error ? error.message : String(error));
  process.exitCode = 1;
});
