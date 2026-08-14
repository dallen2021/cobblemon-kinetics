# Create: Cobblemon Kinetics

> **Pokémon power real Create contraptions.**

Create: Cobblemon Kinetics is an open-source, unofficial compatibility mod for
[Create](https://github.com/Creators-of-Create/Create) and
[Cobblemon](https://gitlab.com/cable-mc/cobblemon). Its first playable loop
replaces passive water-wheel power with a visible, player-owned Cobblemon
worker operating a real Create kinetic network.

The project is currently an **unreleased `0.1.0-SNAPSHOT`**. The implemented
scope is intentionally narrow: the Hydro Coupler and Worker Whistle described
below work today; other Pokémon jobs and Create add-on integrations are future
work.

## Project identity

| Field | Value |
| --- | --- |
| Display name | Create: Cobblemon Kinetics |
| Project slug | `cobblemon-kinetics` |
| Mod ID | `cobblemon_kinetics` |
| Java package | `dev.cobblemonkinetics` |
| License | [Mozilla Public License 2.0](LICENSE) |

## Implemented MVP

### Hydro Coupler

The Hydro Coupler is a configurable Create kinetic source. Place it so its
shaft face touches a Create Water Wheel or Large Water Wheel, then connect the
other side of the wheel to the rest of the kinetic network. The coupler only
generates rotation while its assigned Pokémon is present and eligible.

At the default settings an active coupler supplies:

- `8 RPM`; and
- `64 SU/RPM` of stress capacity.

Create goggles report whether the coupler is active, waiting for its assigned
worker, or unassigned. While active, splash particles draw a visible stream
from the worker to the wheel.

### Worker Whistle

The Worker Whistle provides explicit two-step assignment:

1. Right-click one of your deployed Pokémon with the whistle to select it.
2. Right-click a Hydro Coupler with the same whistle to assign it.
3. Keep the Pokémon deployed within the configured radius of the coupler.
4. Shift-right-click the coupler with a whistle to clear its assignment.

The selected Pokémon ID is stored on the whistle, while the assignment is
stored on the coupler. A Pokémon cannot be claimed by two loaded Hydro
Couplers at once.

With the default server configuration, a worker must be:

- alive and not fainted;
- idle and not participating in a battle;
- owned by the assigning player;
- a Water type;
- National Pokédex number 1 through 151; and
- deployed in the same dimension, within six blocks of the coupler.

The MVP does not move or teleport workers into position. A sent-out or
pastured Pokémon must be kept nearby by the player. Recalling it, moving it
out of range, entering battle, fainting, or unloading it pauses the coupler;
returning it to an eligible state resumes work.

### Water-wheel replacement rule

By default, `replaceNaturalWaterPower` is enabled. Create water wheels then
receive zero power from ordinary fluid flow, making the Pokémon-operated Hydro
Coupler the intended source. Disable that option if an additive experience is
preferred.

No water blocks are placed by this mod. The working stream is a visual effect,
and all kinetic state is decided by the server.

## Not implemented yet

The following ideas are roadmap items, **not current features**:

- Fire, Electric, Flying, Fighting, Grass, Psychic, Ice, Ghost, logistics, or
  other machine jobs;
- data-pack-defined work profiles or a public workstation adapter API;
- stat-, move-, evolution-, friendship-, stamina-, or fatigue-based output;
- autonomous worker pathfinding, schedules, feeding, or forced chunk loading;
- Poké Ball, medicine, candy, or other Create processing recipes;
- compatibility modules for third-party Create add-ons;
- a Fabric build, a launcher-ready public modpack, or a stable release; and
- support for generations after the original 151 in the default gameplay
  profile.

See [the Generation 1 roadmap](docs/GEN1_ROADMAP.md) for proposals and
[the architecture notes](docs/ARCHITECTURE.md) for technical boundaries.

## Compatibility pins

Development and playtesting use the following exact versions:

| Component | Pinned version |
| --- | --- |
| Java | 21 |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.244 |
| Cobblemon | 1.7.3+1.21.1 |
| Create | 6.0.10 (`6.0.10-280` Maven artifact) |
| Kotlin for Forge | 5.12.0 |
| Gradle wrapper | 8.14.3 |
| Architectury Loom | 1.11.458 |
| Architectury plugin | 3.4.164 |

Create and Cobblemon are required on both the client and server. Kotlin for
Forge is part of the pinned Cobblemon/NeoForge development runtime. Dependency
upgrades should be isolated from gameplay changes because the Create
water-wheel hook is version-sensitive.

## Installing a locally built jar

There is no public release artifact yet. For local testing:

1. Install NeoForge for Minecraft 1.21.1.
2. Install the pinned Create, Cobblemon, and Kotlin for Forge dependencies.
3. Build this project as described below.
4. Copy the remapped `cobblemon-kinetics-1.21.1-0.1.0-SNAPSHOT.jar` from
   `build/libs/` into the instance's `mods/` directory.
5. Install the same mod and dependency versions on a dedicated server.

Do not copy development or `sources` jars into the game instance.

## Development setup

### Prerequisites

- Git
- A **64-bit JDK 21**, not only a JRE
- Network access for Gradle's first dependency resolution

The Gradle Java toolchain is configured for Java 21, but the Gradle wrapper
still needs a working Java installation to start. If `java -version` fails or
reports another major version, set `JAVA_HOME` explicitly before invoking the
wrapper.

macOS or Linux:

```sh
export JAVA_HOME=/absolute/path/to/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"
java -version
./gradlew build
```

On macOS with a registered JDK 21, the first line can usually be:

```sh
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
```

Windows PowerShell:

```powershell
$env:JAVA_HOME = "C:\absolute\path\to\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
.\gradlew.bat build
```

If `/usr/libexec/java_home`, `java`, or the wrapper says it cannot locate a
runtime, install a JDK 21 first; setting `JAVA_HOME` to a nonexistent folder
will not trigger a download.

### Common tasks

Run these from the repository root:

```sh
./gradlew test             # fast eligibility-policy tests
./gradlew build            # tests and builds the distributable jar
./gradlew runClient        # launches the integrated development client
./gradlew runServer        # launches the development dedicated server
./gradlew buildAndInstall  # builds and copies the jar to modpack/run/mods/
```

Use `gradlew.bat` instead of `./gradlew` on Windows. The `modpack/run/mods/`
folder is only a repo-local installation target. `buildAndInstall` downloads
the pinned Create, Cobblemon, and Kotlin for Forge files from Modrinth and
verifies their SHA-512 hashes before installing this mod. NeoForge and
Minecraft remain launcher/Gradle-managed dependencies, and downloaded jars
must never be committed.

## Five-minute playtest

1. Run `./gradlew runClient` and create a test world.
2. Obtain a Hydro Coupler, Worker Whistle, Create Water Wheel, shafts, goggles,
   and a small stress-consuming machine such as a Mechanical Press.
3. Place the Hydro Coupler with its shaft face directly against the wheel.
   Connect the opposite wheel shaft to the test machine. Rotate components with
   Create's wrench if their axes do not align.
4. Send out an owned, healthy Gen 1 Water-type Pokémon such as Squirtle and
   keep it within six blocks.
5. Right-click the Pokémon with the Worker Whistle, then right-click the Hydro
   Coupler.
6. Confirm the splash stream appears, the wheel and shafts rotate, the machine
   receives power, and the goggles show an active worker.
7. Recall the Pokémon, move it out of range, or begin a battle. Confirm the
   network stops and the coupler reports that it is waiting.
8. Return the eligible Pokémon and confirm power resumes.
9. Shift-right-click the coupler with the whistle and confirm the assignment
   clears.
10. With default configuration, verify that flowing water alone does not power
    an unassisted Create water wheel.

Please repeat behavior-changing tests on a dedicated server before opening a
pull request. Do not accept Minecraft's server EULA unless you have read and
agree to it.

## Server configuration

NeoForge writes `cobblemon-kinetics-server.toml` into the world's
`serverconfig` directory. Stop the world before changing values during a
repeatable test.

| Setting | Default | Purpose |
| --- | ---: | --- |
| `replaceNaturalWaterPower` | `true` | Prevent fluid flow from powering Create water wheels |
| `workerRadius` | `6.0` | Maximum coupler-to-worker distance in blocks |
| `requirePlayerOwned` | `true` | Require player ownership during active validation |
| `genOneOnly` | `true` | Restrict eligibility to Pokédex numbers 1–151 |
| `hydroRpm` | `8` | Active Hydro Coupler speed |
| `hydroCapacity` | `64` | Active stress capacity per RPM |
| `showWorkParticles` | `true` | Render the worker-to-wheel splash stream |

When `requirePlayerOwned` is disabled, the whistle and active-worker lookup
both allow otherwise eligible non-player-owned Pokémon. The assigning player
still owns the workstation assignment, so other players cannot replace or
clear it.

## Contributing and support

- Read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting a pull request.
- Follow [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) in all project spaces.
- Report vulnerabilities according to [SECURITY.md](SECURITY.md).
- User-visible changes belong in [CHANGELOG.md](CHANGELOG.md).

Regular bugs should include the exact dependency versions, relevant logs, and
a minimal reproduction. This is an unofficial compatibility project, so do
not ask the Create or Cobblemon maintainers to diagnose an issue until it has
been reproduced without this mod.

## Licensing, assets, and attribution

This repository is licensed under the [Mozilla Public License 2.0](LICENSE).
Create, Cobblemon, NeoForge, Minecraft, Kotlin for Forge, and all other
dependencies are separately distributed projects governed by their own
licenses.

Create's code and assets do not share the same upstream license; in
particular, Create's upstream assets are All Rights Reserved. This repository
does **not** copy or redistribute Create or Cobblemon models, textures, sounds,
logos, jars, or other upstream assets. Runtime resource references and API use
are not bundled asset copies. Contributions must be original or accompanied by
clear, compatible permission and provenance; project policy is to prefer
original work.

See [NOTICE](NOTICE) for required project notices.

## Unofficial project notice

Create: Cobblemon Kinetics is independent and unofficial. It is not affiliated
with or endorsed by Mojang Studios, Microsoft, Nintendo, Creatures Inc., GAME
FREAK inc., The Pokémon Company, the Cobblemon team, or the Create team.

Minecraft, Pokémon, Create, Cobblemon, and related names and marks belong to
their respective owners. They are used only to identify compatibility and
required dependencies.
