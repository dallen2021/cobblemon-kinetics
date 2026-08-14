# Contributing to Create: Cobblemon Kinetics

Thank you for helping build Pokémon-operated Create machinery. Contributions
of code, tests, documentation, translations, bug reproductions, profiling, and
legally original art are welcome.

The project is currently an unreleased `0.1.0-SNAPSHOT`. Keep changes narrow,
testable, and honest about what exists today. The only implemented gameplay
role is the Hydro Coupler operated through the Worker Whistle; roadmap entries
are not shipped features.

By submitting a contribution, you agree that it may be distributed under the
repository's [Mozilla Public License 2.0](LICENSE) and that you have the right
to provide every part of it under those terms.

## Before starting

- Search existing issues and pull requests before duplicating work.
- Open a design issue before adding a machine role, public API, mixin, optional
  Create add-on integration, new dependency, or dependency-version upgrade.
- Use the dedicated compatibility proposal form for Create add-ons. Include an
  official project link, exact version, upstream license, integration boundary,
  add-on-absent behavior, and test plan.
- Read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for integration boundaries
  and [docs/GEN1_ROADMAP.md](docs/GEN1_ROADMAP.md) for planned scope.
- Keep gameplay changes within Generation 1 unless maintainers have explicitly
  accepted a later roadmap milestone.
- Do not combine formatting churn, dependency upgrades, and gameplay changes
  in one pull request.

Small bug fixes and documentation corrections can go directly to a pull
request when their intent is clear.

## Development environment

Use the versions pinned in `gradle.properties` and summarized in the
[README](README.md#compatibility-pins). A 64-bit JDK 21 is required.

The Gradle toolchain declaration does not help if the wrapper cannot start.
If `java -version` fails or reports the wrong major version, set `JAVA_HOME` to
an installed JDK 21 first:

```sh
export JAVA_HOME=/absolute/path/to/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew build
```

On Windows, use `gradlew.bat` and set `$env:JAVA_HOME` in PowerShell or
`JAVA_HOME` in the system environment. Do not point it at a JRE or nonexistent
directory.

Useful tasks:

```sh
./gradlew test
./gradlew build
./gradlew runClient
./gradlew runServer
./gradlew buildAndInstall
```

`buildAndInstall` prepares the ignored, pinned local test pack and copies this
project's remapped jar into `modpack/run/mods/`. Supply third-party runtime
dependencies through Gradle or reproducible pack metadata; never commit
dependency jars.

## Branch and commit guidance

1. Fork the repository and branch from the current default branch.
2. Use a short branch prefix that describes the work: `feat/`, `fix/`,
   `docs/`, `test/`, `refactor/`, `build/`, or `chore/`. Include the issue
   number when one exists, for example `fix/42-worker-claim-release`.
3. Use [Conventional Commits](https://www.conventionalcommits.org/) with an
   imperative summary, such as `fix(coupler): release stale worker claim` or
   `docs: add dedicated-server test steps`.
4. Keep each commit reviewable and keep each pull request focused on one
   concern. A work-in-progress branch may contain several commits.
5. Update the branch from `main` before requesting final review when it has
   drifted. Do not rewrite another contributor's work without coordination.

Pull request titles should also follow Conventional Commits because the
repository uses squash merging. After the required build passes and review
conversations are resolved, a maintainer can merge the pull request. The
squash commit becomes the single permanent commit on `main`; the source branch
is deleted automatically. There is no long-lived `develop` branch.

## Code expectations

- Target Java 21 and preserve the existing package namespace,
  `dev.cobblemonkinetics`.
- Keep Create- and Cobblemon-version-sensitive hooks small. Put game rules in
  project-owned, testable policy classes.
- Make production, ownership, assignment, and eligibility server-authoritative.
- Bound entity searches to loaded local areas. Do not scan a whole level,
  force-load chunks, or retain raw entity references across lifecycle events.
- Use a Pokémon's persistent Cobblemon UUID for identity, not a transient
  runtime entity ID.
- Release claims and cached state on removal, reassignment, invalidation, and
  chunk or level unload.
- Add translatable language keys for every player-facing string.
- Give configuration values safe defaults and bounded ranges. Document new
  settings in the README.
- Avoid unrelated generated-file or import-order churn.
- Comment a mixin or access workaround with the public-API limitation that made
  it necessary.

Optional integrations must not load their classes when the optional mod is
absent. Each integration proposal must identify its exact supported version,
public API or minimal hook, license, failure behavior, and test plan.

## Tests and manual verification

Every pull request must run:

```sh
./gradlew test
./gradlew build
```

Add or update fast tests for eligibility, scoring, ownership, configuration,
and deterministic policy behavior. World-facing changes should also include a
NeoForge GameTest or a documented manual reproduction until equivalent
automation exists.

For gameplay changes, use the README's five-minute playtest as a baseline and
test the relevant lifecycle transitions:

- assignment and explicit clearing;
- worker recall, range exit, fainting, busy state, and battle entry;
- block and entity removal;
- chunk unload and world reload;
- several players, workers, and couplers;
- integrated client and dedicated server; and
- behavior with optional dependencies absent.

Include the tested Minecraft, NeoForge, Create, Cobblemon, Java, and mod
versions in the pull request. Attach concise logs or screenshots when they
materially demonstrate the result, but redact player identifiers, server
addresses, tokens, and filesystem details that should remain private.

## Documentation and changelog

- Describe the implementation as it exists; label proposals as **planned** or
  **future**, never as available.
- Update `README.md` when setup, configuration, compatibility, or player-facing
  behavior changes.
- Update `docs/ARCHITECTURE.md` when a component boundary or integration rule
  changes.
- Update `docs/GEN1_ROADMAP.md` only when project direction or milestone status
  changes.
- Add a concise entry under `CHANGELOG.md` → `Unreleased` for every
  user-visible change.

## Art, code provenance, and third-party material

Contributions must be original or have documented, compatible permission.

Project policy is **not to copy upstream assets**. Do not submit:

- Create or Cobblemon textures, models, animations, sounds, logos, or other
  assets copied into this repository;
- Pokémon game or anime sprites, cries, music, fonts, logos, or ripped data;
- code copied from an All Rights Reserved mod or from an unknown source;
- generated AI output whose training-source rights or redistribution terms you
  cannot reasonably represent; or
- Create, Cobblemon, NeoForge, Minecraft, or third-party mod jars.

Create's upstream code is MIT-licensed while its asset tree is All Rights
Reserved. Cobblemon and every optional add-on have their own terms. API calls,
registry references, and runtime resource references are preferred over
copying. If a contribution legitimately incorporates compatible third-party
material, identify every file, source URL, author, license, modification, and
required notice in the pull request and update `NOTICE` where necessary.

Maintainers may reject otherwise legal reuse when original work would give the
project clearer provenance.

## Pull request checklist

Before requesting review, confirm that:

- [ ] The change has one clear purpose and links its issue or design decision.
- [ ] `./gradlew test` and `./gradlew build` pass on JDK 21.
- [ ] Relevant integrated-client or dedicated-server behavior was tested.
- [ ] New behavior is server-authoritative and lifecycle-safe.
- [ ] User-facing text is translatable.
- [ ] README, architecture, roadmap, and changelog updates are included where
      applicable.
- [ ] No jars, secrets, logs with private data, generated run state, or
      unlicensed assets are included.
- [ ] All submitted material is original or has complete compatible provenance.

## Review and conduct

Reviewers may request a smaller scope, more tests, licensing evidence, or a
different compatibility boundary. Technical disagreement is normal; keep it
specific to the work and follow [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) and
[GOVERNANCE.md](GOVERNANCE.md).

For a suspected vulnerability, do not open a public bug report. Follow
[SECURITY.md](SECURITY.md).
