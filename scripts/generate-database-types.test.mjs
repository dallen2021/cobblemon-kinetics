import assert from "node:assert/strict";
import { mkdtempSync, readFileSync, readdirSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import test from "node:test";
import {
  generateDatabaseTypes,
  isRetryableGenerationFailure,
  normalizeGeneratedTypes,
} from "./generate-database-types.mjs";

const validGeneratedTypes = `export type Json = string\n\nexport type Database = { public: never }\n`;

function withTarget(run) {
  const directory = mkdtempSync(join(tmpdir(), "kinetics-db-types-"));
  const targetPath = join(directory, "database.generated.ts");
  writeFileSync(targetPath, "original contents\n");
  return Promise.resolve(run({ directory, targetPath })).finally(() => {
    rmSync(directory, { recursive: true, force: true });
  });
}

test("normalization rejects empty or incomplete generator output", () => {
  assert.throws(() => normalizeGeneratedTypes(""), /incomplete/u);
  assert.throws(() => normalizeGeneratedTypes("export type Json = string\n"), /incomplete/u);
  assert.equal(normalizeGeneratedTypes(`${validGeneratedTypes}\n\n`), validGeneratedTypes);
});

test("failed generation preserves the existing tracked file", async () => {
  await withTarget(async ({ directory, targetPath }) => {
    await assert.rejects(
      generateDatabaseTypes({
        targetPath,
        maxAttempts: 1,
        runner: () => ({ status: 1, stdout: "", stderr: "schema error\n" }),
        writeDiagnostic: () => {},
      }),
      /exited with status 1/u,
    );

    assert.equal(readFileSync(targetPath, "utf8"), "original contents\n");
    assert.deepEqual(readdirSync(directory), ["database.generated.ts"]);
  });
});

test("a registry throttle is retried before an atomic replacement", async () => {
  await withTarget(async ({ targetPath }) => {
    let calls = 0;
    const delays = [];

    await generateDatabaseTypes({
      targetPath,
      maxAttempts: 3,
      retryDelayMs: 5,
      runner: () => {
        calls += 1;
        return calls === 1
          ? { status: 125, stdout: "", stderr: "toomanyrequests: Rate exceeded\n" }
          : { status: 0, stdout: validGeneratedTypes, stderr: "" };
      },
      sleep: async (delay) => delays.push(delay),
      writeDiagnostic: () => {},
    });

    assert.equal(calls, 2);
    assert.deepEqual(delays, [5]);
    assert.equal(readFileSync(targetPath, "utf8"), validGeneratedTypes);
  });
});

test("deterministic generator errors are not retried", async () => {
  await withTarget(async ({ targetPath }) => {
    let calls = 0;
    await assert.rejects(
      generateDatabaseTypes({
        targetPath,
        maxAttempts: 3,
        runner: () => {
          calls += 1;
          return { status: 1, stdout: "", stderr: "invalid schema\n" };
        },
        sleep: async () => assert.fail("non-transient errors must not sleep"),
        writeDiagnostic: () => {},
      }),
      /exited with status 1/u,
    );
    assert.equal(calls, 1);
    assert.equal(readFileSync(targetPath, "utf8"), "original contents\n");
  });
});

test("the transient-failure classifier is intentionally narrow", () => {
  assert.equal(isRetryableGenerationFailure("HTTP status 429"), true);
  assert.equal(isRetryableGenerationFailure("TLS handshake timeout"), true);
  assert.equal(isRetryableGenerationFailure("invalid schema"), false);
});
