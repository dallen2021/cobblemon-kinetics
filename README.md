# Create: Cobblemon Kinetics

> **Pokémon power real Create contraptions.**

[![Build](https://github.com/dallen2021/cobblemon-kinetics/actions/workflows/build.yml/badge.svg)](https://github.com/dallen2021/cobblemon-kinetics/actions/workflows/build.yml)
[![License: MPL-2.0](https://img.shields.io/badge/license-MPL--2.0-blue.svg)](LICENSE)

Create: Cobblemon Kinetics is an open-source compatibility mod for
[Create](https://github.com/Creators-of-Create/Create) and
[Cobblemon](https://gitlab.com/cable-mc/cobblemon). It replaces passive
automation with deliberately assigned Pokémon workers while keeping Create's
kinetic networks, stress system, and visible machinery at the center of the
factory.

The current build is a playable **public alpha**. It proves one complete
job—the Hydro Coupler operated with a Worker Whistle—on Minecraft 1.21.1 and
NeoForge. Everything described under [Future scope](#future-scope) is planned,
not shipped.

| Project field | Value |
| --- | --- |
| Latest release | [`v0.1.0-alpha.1`](https://github.com/dallen2021/cobblemon-kinetics/releases/tag/v0.1.0-alpha.1) |
| Development version | `0.1.0-SNAPSHOT` |
| Project slug | `cobblemon-kinetics` |
| Mod ID | `cobblemon_kinetics` |
| Default gameplay scope | Generation 1 |
| License | [Mozilla Public License 2.0](LICENSE) |
| Website and design studio | [`cobblemon-kinetics-website`](https://github.com/dallen2021/cobblemon-kinetics-website) |

## Reproducible development pack

From a clean clone (outside any Modrinth/CurseForge profile), validate the
committed manifest with hash-verified dependency downloads, run
`buildAndInstall`, and confirm exactly one Cobblemon Kinetics development JAR
is installed before launching the pinned core stack to the title screen. Do
not commit downloaded third-party JARs or machine-specific absolute paths.

## What works today

### Hydro Coupler

The Hydro Coupler is a custom native Create kinetic source. Its shaft face
must touch the shaft of a Create Water Wheel or Large Water Wheel. The coupler
generates only while that wheel is attached and its specifically assigned
Pokémon is loaded, nearby, and eligible.

At the default settings, an active coupler supplies a fixed **8 RPM** and
**64 SU/RPM** of stress capacity. Create goggles report whether it is active,
waiting, or unassigned, and an optional splash stream runs from the worker to
the wheel.

The coupler—not the Pokémon and not fluid flow—is the generator. The attached
wheel is a required, visible part of the kinetic network; no synthetic fluid
score is applied to it.

### Worker Whistle

Assignment is explicit and server-authoritative:

1. Deploy an eligible Pokémon.
2. Right-click it with a Worker Whistle to select it.
3. Right-click a Hydro Coupler with the same whistle to assign it.
4. Keep the Pokémon in the same dimension and within the configured radius.
5. Shift-right-click the coupler with any Worker Whistle to clear it.

The whistle stores the Pokémon's persistent Cobblemon UUID and the selecting
player's UUID. The coupler persists both identifiers with its saved block
entity data. The assigning player owns that station assignment; another
player cannot replace or clear it.

Under the default configuration, a worker must be:

- alive and not fainted;
- idle and not in battle;
- owned by the assigning player;
- Water-type;
- National Pokédex number 1 through 151; and
- loaded within six blocks of the assigned coupler.

A per-level claims registry prevents one persistent Pokémon UUID from
operating two loaded Hydro Couplers in that level. Recalling, unloading,
moving, fainting, or making the worker busy pauses output without deleting the
assignment, so work can resume when the exact worker becomes eligible again.
The mod does not pathfind, teleport, or force-load workers.

### Natural water-wheel replacement

`replaceNaturalWaterPower` is enabled by default. A narrow mixin then
suppresses Create's environmental fluid-flow score for water wheels, making
the Hydro Coupler the intended source in this gameplay loop. Disabling the
setting leaves Create's normal fluid scoring unchanged.

This rule affects Create water wheels globally. It never places or removes
water blocks; the visible worker stream is particles only.

## Project status

The MVP currently includes:

- the Hydro Coupler and two-click Worker Whistle workflow;
- persistent selection and assignment identifiers;
- bounded server-side worker lookup and active-state validation;
- loaded-level duplicate-job claims and assignment ownership;
- configurable RPM, capacity, range, ownership, Gen 1 restriction, particles,
  and natural-water replacement;
- crafting recipes, loot, translations, basic runtime-resource-based models,
  and Create-goggle feedback;
- fast unit tests for worker eligibility and configuration-policy branches;
- Java 21 build CI and a protected maintainer-dispatched release workflow; and
- a reproducible, ignored local development-pack staging target.

This alpha is not a stable release. NeoForge GameTests, complete lifecycle
coverage, repeatable multiplayer and dedicated-server evidence, and a
launcher-ready public pack are still outstanding. Save data, balance,
configuration, and compatibility boundaries may change before the first
stable release.

### Separate website and design studio

The wiki, private design studio, collaborative drafts, publication tooling,
and language-neutral schemas live in the separate
[`cobblemon-kinetics-website`](https://github.com/dallen2021/cobblemon-kinetics-website)
repository. Keeping them separate leaves this repository as an independently
buildable Java mod.

Reviewed work-profile exports cross the repository boundary only through a
normal pull request. This repository bundles and validates those explicit JSON
resources under `src/main/resources/data/cobblemon_kinetics/work_profiles/`;
it does not contain private drafts, workbook imports, database state, or web
deployment credentials. Existing Hydro gameplay still uses its current
configuration and saved-data path: the profile loader validates the contract
but does not silently migrate live world behavior.

## Future scope

These are roadmap items, **not current features**:

- Fire, Electric, Flying, Fighting, Grass, Ice, Psychic, Ghost, logistics, or
  other Pokémon jobs;
- live gameplay driven by published work profiles or a public workstation
  adapter API (the first schema/parser exists, but is not yet authoritative);
- stat-, move-, evolution-, friendship-, stamina-, or fatigue-based output;
- autonomous pathfinding, schedules, feeding, or worker management screens;
- Poké Ball, medicine, candy, or other Create processing recipes;
- integrations with third-party Create add-ons;
- Fabric support or a published launcher-ready modpack; and
- default support for Pokémon introduced after National Pokédex number 151.

See the [Generation 1 roadmap](docs/GEN1_ROADMAP.md) for proposed milestones
and [architecture notes](docs/ARCHITECTURE.md) for integration boundaries.

## Compatibility pins

Development and reproducible testing target these exact versions. A declared
loader range means the game may accept that version; it does not mean every
version in the range has been tested.

| Component | Development pin | Declared runtime compatibility |
| --- | --- | --- |
| Java | JDK 21 | Required to build and run the development environment |
| Minecraft | 1.21.1 | Exactly 1.21.1 |
| NeoForge | 21.1.244 | 21.1.219 or newer |
| Create | 6.0.10 (`6.0.10-280` Maven artifact) | 6.0.10 up to, but not including, 6.1.0 |
| Cobblemon | 1.7.3+1.21.1 | 1.7.3 up to, but not including, 1.8.0 |
| Kotlin for Forge | 5.12.0 | Pinned runtime dependency for the Cobblemon NeoForge stack |

The Gradle build additionally pins Gradle 8.14.4, Architectury Loom 1.11.458,
Architectury plugin 3.4.164, Kotlin JVM plugin 2.4.10, Ponder 1.0.82, Flywheel
1.0.6, and Registrate `MC1.21-1.3.0+67`. The authoritative values are in
[`gradle.properties`](gradle.properties); the local pack's runtime pins are
also recorded in [`modpack/versions.json`](modpack/versions.json).

Create, Cobblemon, Kotlin for Forge, and this mod are required on both the
client and server for the pinned NeoForge setup. Please reproduce compatibility
reports on the exact development pins before reporting a version-range issue.

## Build from source

### Prerequisites

- Git
- A 64-bit [JDK 21](https://adoptium.net/temurin/releases/?version=21), not
  only a JRE
- Network access for the first Gradle dependency resolution

The Gradle wrapper must start before Gradle can provision or select a Java
toolchain. Verify the shell is using Java 21:

```text
java -version
```

If that command fails or reports another major version, configure
`JAVA_HOME` for your operating system to point to the installed **JDK 21
directory**, put its `bin` directory first on `PATH`, reopen the shell, and
check again. Pointing `JAVA_HOME` at a JRE or a nonexistent directory will
not install Java.

Node.js, pnpm, Docker, Supabase, and Vercel are not required to build, test, or
play the mod. Website and design-data contributions belong in the separate
[`cobblemon-kinetics-website`](https://github.com/dallen2021/cobblemon-kinetics-website)
repository.

From the repository root, run:

```sh
./gradlew test
./gradlew build
```

On Windows, use:

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

The distributable is the non-`sources` JAR in `build/libs/`. The snapshot
filename is `cobblemon-kinetics-1.21.1-0.1.0-SNAPSHOT.jar`.

Useful development tasks:

| Task | Purpose |
| --- | --- |
| `./gradlew test` | Run the fast eligibility-policy unit tests |
| `./gradlew build` | Run tests and produce remapped distributable and source JARs |
| `./gradlew runClient` | Launch the integrated development client |
| `./gradlew runServer` | Launch the development dedicated server |
| `./gradlew clientSettingsStatus` | Check the runtime and recovery copies of personal client options |
| `./gradlew prepareDevPack` | Download and hash-check the pinned local-pack dependencies |
| `./gradlew buildAndInstall` | Build, test, stage dependencies, and install the development JAR |

Use `gradlew.bat` instead of `./gradlew` for these tasks on Windows.
`runServer` creates its state under `run/server/`; review and accept
Minecraft's server EULA yourself before continuing past its first stop.

### Persistent development-client settings

`runClient` always uses the project-relative `run/client/` game directory, so
Minecraft writes personal settings to the same ignored `run/client/options.txt`
on every launch. The build also keeps an ignored recovery copy at
`.dev-client-settings/options.txt`: it snapshots valid settings before launch,
restores them if the generated run directory was removed, and captures them
again when the Gradle client task ends. Neither file is committed or shared.

Run `./gradlew clientSettingsStatus` to verify both copies. Deleting both
ignored files intentionally resets the development client's settings.

## Install for local play

Download the non-`sources` JAR and `SHA256SUMS` from the
[`v0.1.0-alpha.1` prerelease](https://github.com/dallen2021/cobblemon-kinetics/releases/tag/v0.1.0-alpha.1),
then:

1. Create a Minecraft 1.21.1 instance with NeoForge 21.1.244.
2. Install
   [Create 6.0.10](https://modrinth.com/mod/create),
   [Cobblemon 1.7.3 for NeoForge](https://modrinth.com/mod/cobblemon), and
   [Kotlin for Forge 5.12.0](https://modrinth.com/mod/kotlin-for-forge) from
   their official distribution pages.
3. Copy `cobblemon-kinetics-1.21.1-0.1.0-alpha.1.jar` into the instance's
   `mods/` directory. Verify it against `SHA256SUMS` when possible.
4. For multiplayer, install the same project and dependency versions on the
   dedicated server.

Do not install a `sources`, `dev`, or `shadow` artifact into a game
instance. Contributors can instead run `./gradlew build` and use the
non-`sources` snapshot JAR from `build/libs/`.

### Repo-local development pack

`./gradlew buildAndInstall` stages a reproducible local test set in
`modpack/run/mods/`. It:

- builds and tests this project;
- copies the remapped output as `cobblemon-kinetics-dev.jar`;
- downloads the pinned Create, Cobblemon, and Kotlin for Forge files from
  Modrinth; and
- verifies every downloaded file against its committed SHA-512 hash.

This directory is ignored and is **not** a complete launcher instance or a
published modpack. NeoForge and Minecraft remain launcher-managed. The
[`modpack/modrinth.index.json`](modpack/modrinth.index.json) manifest records
the external runtime files and intentionally omits this unpublished project's
JAR. See [`modpack/README.md`](modpack/README.md) for the staging policy.

## Optional add-on fixtures

The ignored `dev-addons/` directory exists only for lawful, machine-local
compatibility experiments:

```text
dev-addons/*.jar
dev-addons/embedded-mods/*.jar
dev-addons/embedded-libs/*.jar
```

Gradle treats the first two locations as development-only mod runtime
fixtures, allowing Loom to remap them for local runs. Files under
`embedded-libs/` are development runtime libraries. None of these paths is
on the production compile classpath or included in this mod's artifacts.

Only use add-on files you obtained lawfully. Never commit, publish, bundle, or
redistribute fixture jars, and never treat a successful local fixture run as
a public compatibility guarantee. A supported integration must use a reviewed,
license-compatible dependency and remain safe when that optional mod is
absent.

## Five-minute playtest

1. Run `./gradlew runClient` and create a test world.
2. Obtain a Hydro Coupler, Worker Whistle, Create Water Wheel or Large Water
   Wheel, shafts, Engineer's Goggles, and a small kinetic consumer such as a
   Mechanical Press.
3. Place the wheel's shaft directly against the Hydro Coupler's shaft face.
   Connect the wheel to the consumer; use Create's wrench if the axes do not
   align.
4. Deploy an owned, healthy Generation 1 Water-type Pokémon such as Squirtle
   and keep it within six blocks of the coupler.
5. Right-click the Pokémon with the Worker Whistle, then right-click the
   Hydro Coupler.
6. Confirm the assignment message, splash stream, wheel and shaft rotation,
   powered consumer, and active goggle status.
7. Recall the Pokémon, move it out of range, or enter a battle. Confirm output
   stops while the coupler remains assigned and reports that it is waiting.
8. Return the same eligible Pokémon and confirm output resumes.
9. Shift-right-click the coupler with a Worker Whistle and confirm the
   assignment clears.
10. With the default configuration, confirm that ordinary flowing water alone
    does not power a Create water wheel. Then set
    `replaceNaturalWaterPower=false`, reload the world, and confirm normal
    Create fluid power returns.

Gameplay changes should also be checked with `./gradlew runServer` and a
real client before they are considered release-ready. This repository does
not yet have GameTests for the world-facing workflow.

## Server configuration

NeoForge writes `cobblemon-kinetics-server.toml` in each world's
`serverconfig/` directory. Stop or unload the world before changing values
for a repeatable test.

| Setting | Default | Allowed values | Effect |
| --- | ---: | --- | --- |
| `replaceNaturalWaterPower` | `true` | Boolean | Suppress Create water-wheel environmental flow scoring |
| `workerRadius` | `6.0` | 2.0–16.0 | Maximum coupler-to-worker distance in blocks |
| `requirePlayerOwned` | `true` | Boolean | Require a player-owned Pokémon during selection and active validation |
| `genOneOnly` | `true` | Boolean | Restrict workers to Pokédex numbers 1–151 |
| `hydroRpm` | `8` | 1–256 | Active Hydro Coupler speed magnitude |
| `hydroCapacity` | `64` | 1–1024 | Active stress-capacity coefficient per RPM |
| `showWorkParticles` | `true` | Boolean | Show the worker-to-wheel splash stream |

When `requirePlayerOwned=false`, the whistle and active lookup allow an
otherwise eligible wild or NPC-owned Pokémon. The selecting player still owns
the coupler assignment, so other players cannot replace or clear it.

## Contributing and project workflow

Contributions are welcome. Start with the
[contribution guide](CONTRIBUTING.md), follow the
[Code of Conduct](CODE_OF_CONDUCT.md), review the
[project governance](GOVERNANCE.md), and add user-visible changes to the
[changelog](CHANGELOG.md). The repository also provides a
[pull request template](.github/pull_request_template.md) and issue forms for
[bugs](.github/ISSUE_TEMPLATE/bug_report.yml) and
[feature proposals](.github/ISSUE_TEMPLATE/feature_request.yml).

- [Build workflow](.github/workflows/build.yml): runs the Java 21 Gradle build
  for pull requests and pushes to `main`.
- [Release workflow](.github/workflows/release.yml): runs only from the default
  branch, rebuilds the reviewed source, and then creates the release tag and
  attaches the single distributable JAR and checksum.
- [Security policy](SECURITY.md): use the private reporting path for suspected
  vulnerabilities rather than a public issue.

New machine roles, mixins, dependencies, optional integrations, and version
upgrades require design discussion. Keep gameplay decisions
server-authoritative, entity searches bounded, and roadmap claims clearly
marked as future until their tests and implementation ship.

## License, credits, and asset policy

Create: Cobblemon Kinetics is licensed under the
[Mozilla Public License 2.0](LICENSE). See [NOTICE](NOTICE) for project notices.
Contributions must be original or supplied with documented, compatible rights.

Create, Cobblemon, NeoForge, Minecraft, Kotlin for Forge, and every optional
add-on remain separate projects under their own terms. In particular,
[Create licenses its code and asset tree separately](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/LICENSE.md),
while [Cobblemon source is distributed under MPL-2.0](https://gitlab.com/cable-mc/cobblemon/-/blob/main/LICENSE).
Installing a dependency does not place it under this repository's license.

This repository does not copy or redistribute Create or Cobblemon textures,
models, animations, sounds, logos, or JARs. Project models use runtime resource
references instead of copied upstream files. Do not submit ripped Pokémon
media, upstream assets, third-party mod binaries, or code whose license is
unknown. API, registry, and runtime resource references are preferred.

Thanks to the Create and Cobblemon teams and their contributors for the
projects and APIs that make this compatibility work possible.

## Unofficial project notice

Create: Cobblemon Kinetics is independent and unofficial. It is not affiliated
with or endorsed by Mojang Studios, Microsoft, Nintendo, Creatures Inc., GAME
FREAK inc., The Pokémon Company, the Cobblemon team, or the Create team.

Minecraft, Pokémon, Create, Cobblemon, and related names and marks belong to
their respective owners and are used only to identify compatibility and
required dependencies.
