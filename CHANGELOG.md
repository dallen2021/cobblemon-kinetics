# Changelog

All notable user-visible changes to Create: Cobblemon Kinetics will be
documented in this file.

The project intends to follow [Semantic Versioning](https://semver.org/) after
its first release and uses the structure from [Keep a
Changelog](https://keepachangelog.com/en/1.1.0/). While the version remains
`0.x`, gameplay, configuration, saved data, and compatibility hooks may change
between minor versions.

## [Unreleased]

### Added

- Format-1 Hydro Operator work profile, server-data reload validation,
  reviewed workstation-adapter registry, and Java parser contract tests. The
  profile is not yet authoritative for live Hydro gameplay.

### Changed

- Moved the wiki, private design studio, publication data, database migrations,
  and Node tooling to the separate
  [`cobblemon-kinetics-website`](https://github.com/dallen2021/cobblemon-kinetics-website)
  repository so the mod remains an independent Java/Gradle project.

## [0.1.0-alpha.1] - 2026-08-13

First public alpha. This is a playable development milestone, not a stable
compatibility promise.

### Added

- NeoForge 1.21.1 project scaffold targeting Java 21, with pinned development
  versions for NeoForge 21.1.244, Cobblemon 1.7.3, Create 6.0.10, and Kotlin
  for Forge 5.12.0.
- Reproducible Gradle plugin pins for Architectury Loom 1.11.458 and the
  Architectury plugin 3.4.164.
- `cobblemon_kinetics:hydro_coupler`, a directional Create kinetic source that
  requires an attached Water Wheel or Large Water Wheel and an active assigned
  Pokémon.
- `cobblemon_kinetics:worker_whistle`, which selects an owned eligible Pokémon,
  assigns it to a Hydro Coupler, and clears assignments when used while
  sneaking.
- Server-authoritative worker validation for alive, non-fainted, idle,
  non-battling, player-owned Water-type Pokémon.
- Default Generation 1 restriction using inclusive National Pokédex numbers
  1–151.
- Configurable worker radius, ownership and generation restrictions, Hydro
  Coupler RPM and stress capacity, natural water-power replacement, and work
  particles.
- Persistent worker and owner identifiers on Hydro Couplers and persistent
  whistle selection through item custom data.
- Loaded-level worker claims that prevent one Pokémon from operating two
  loaded Hydro Couplers simultaneously.
- Splash-stream feedback from active workers and Create-goggle status for
  active, waiting, and unassigned couplers.
- Crafting recipes, block loot, mining tags, English translations, and basic
  models based on runtime Minecraft resource references.
- Fast unit tests for the Generation 1 eligibility boundary, configuration
  toggles, and primary worker rejection states.
- Gradle `installDevJar` and `buildAndInstall` tasks plus a repo-local,
  jar-ignored development modpack target with hash-verified dependency
  downloads and a Modrinth manifest.
- Architecture and Generation 1 roadmap documentation.
- Public project governance, contribution and support policies, structured
  issue and pull request templates, dependency updates, and hardened GitHub
  build and release automation.
- Project and issue-tracker links in NeoForge mod metadata.
- Ignored repo-local recovery for development-client `options.txt`, including
  Gradle restore, capture, and status tasks so personal settings survive a
  regenerated run directory without entering version control.

### Changed

- Create water-wheel fluid scoring is replaced with zero by default. Pack
  authors can set `replaceNaturalWaterPower=false` to retain normal flowing
  water behavior.

### Security

- Worker selection verifies Cobblemon ownership on the server.
- Coupler activation revalidates persistent Pokémon identity, owner, health,
  activity, battle state, type, generation, range, and attached wheel.
- Entity searches are bounded to the configured local radius and do not
  force-load chunks.

Future roles, data-driven work profiles, add-on integrations, Fabric support,
worker pathfinding, fatigue, and a launcher-ready pack are tracked in
`docs/GEN1_ROADMAP.md`; they are not part of the current implementation.

[Unreleased]: https://github.com/dallen2021/cobblemon-kinetics/compare/v0.1.0-alpha.1...HEAD
[0.1.0-alpha.1]: https://github.com/dallen2021/cobblemon-kinetics/releases/tag/v0.1.0-alpha.1
