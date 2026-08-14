# Generation 1 roadmap

This roadmap turns Create: Cobblemon Kinetics' initial compatibility proof into a cohesive, Pokémon-operated Create experience while keeping the first playable scope to Generation 1. It is a planning document, not a promise that every listed idea will ship unchanged.

## Status key

- **MVP — implemented:** available in the current MVP implementation.
- **In progress:** part of the milestone is implemented, but its exit criteria are not complete.
- **Next:** the next stabilization or framework milestone; not implemented yet unless linked to a merged pull request.
- **Planned:** accepted direction, still requiring design and implementation.
- **Explore:** a candidate that needs technical, balance, licensing, or playtesting validation.

## Current playable scope

### MVP — implemented: Assigned Water Pokémon drive a Hydro Coupler

Create: Cobblemon Kinetics adds a custom directional Hydro Coupler that participates in Create as a native generating kinetic block entity. The coupler produces fixed, configurable speed and stress capacity only while all of the following are true:

- a Pokémon has been explicitly assigned with the two-click Worker Whistle interaction;
- the assigned persistent Pokémon UUID is loaded within the configured radius of the coupler;
- the loaded entity matches the owner UUID stored by the assignment;
- the Pokémon is alive, not fainted, idle, and not participating in a battle;
- it is a Water type and, by default, National Pokédex number 1–151; and
- a Create Water Wheel or Large Water Wheel is attached to the coupler's shaft face.

The player selects an owned eligible deployed Pokémon by right-clicking it with the Worker Whistle, then right-clicks a Hydro Coupler to assign it. The whistle stores the selected Pokémon and owner UUIDs as item custom data; the coupler persists its assigned Pokémon and owner UUIDs in block-entity data. Shift-right-clicking a coupler with the whistle clears the assignment. A transient per-level claims registry prevents one Pokémon UUID from operating two loaded couplers and reconstructs claims when persisted couplers initialize.

The wheel is visibly driven by the coupler's normal Create kinetic output. The Pokémon does not directly provide or replace a wheel flow score. A narrow mixin separately sets Create's environmental water-wheel flow score to zero only while the server option `replaceNaturalWaterPower` is enabled; disabling that option preserves normal Create fluid behavior.

Defaults are 8 RPM, 64 SU/RPM capacity, a six-block worker radius, Gen 1 only, player ownership required, and visible splash-stream particles. No water blocks are placed. No other machine roles, add-on integrations, data-pack job definitions, pathfinding, or worker simulation systems are part of the MVP.

### MVP acceptance checklist

- [x] Target Minecraft 1.21.1, NeoForge, and Java 21.
- [x] Pin NeoForge 21.1.244, Create 6.0.10, Cobblemon 1.7.3, and Kotlin for Forge 5.12.0.
- [x] Implement the Hydro Coupler as a directional native Create kinetic source with required attached small or large water wheel.
- [x] Implement fixed configurable RPM and stress capacity, with zero generated output while inactive.
- [x] Implement two-click Worker Whistle selection/assignment and sneaking clear behavior.
- [x] Persist selected UUIDs on the whistle and assigned Pokémon/owner UUIDs on the coupler.
- [x] Scan only the configured local area around the coupler for the exact assigned persistent UUID and owner.
- [x] Accept an alive, healthy, idle, owned Generation 1 Water Pokémon and reject fainted, busy, battling, wild, non-Water, later-generation, unloaded, and out-of-range workers under default settings.
- [x] Prevent a persistent Pokémon UUID from holding two loaded coupler claims in the same level.
- [x] Keep assignment, eligibility, claims, active state, and generation server-authoritative.
- [x] Add a narrow configurable mixin that suppresses only environmental water-wheel fluid scoring and leaves it unchanged when replacement mode is disabled.
- [x] Add server configuration for replacement mode, range, ownership, generation, RPM, capacity, and particles.
- [x] Add action-bar assignment/rejection feedback, Create-goggle status, and optional splash-stream particles.
- [x] Add fast unit tests for the primary eligibility/rejection matrix and inclusive Gen 1 boundaries.
- [x] Add `buildAndInstall` to copy the remapped jar into the ignored repo-local modpack target.
- [x] Document a repeatable five-minute integrated-client smoke test and Java 21/`JAVA_HOME` setup in the root README.
- [ ] Add NeoForge GameTests for the world-facing coupler, mixin, persistence, claims, and lifecycle behavior.
- [ ] Complete repeatable dedicated-server and multiplayer smoke-test evidence for the first public release.
- [ ] Publish the first signed/tagged open-source release artifact and launcher-ready development modpack.

The checked items describe the MVP implementation contract. Release-process items remain open until a public release is cut.

## Milestone 1 — harden the MVP

**Status: In progress**

The first milestone focuses on making the narrow feature dependable for contributors and pack authors.

Implemented foundation:

- Fast policy tests cover owned Gen 1 Water acceptance; fainted, busy, battling, wild, non-Water, and later-generation rejection; and inclusive Pokédex boundaries.
- A documented server configuration surface controls environmental replacement, worker radius, ownership, Gen 1 restriction, Hydro RPM/capacity, and particles.
- Assignment and eligibility failures provide translated action-bar messages; the coupler exposes active/waiting/unassigned goggle state.
- The repo-local development target and `buildAndInstall` task install the remapped `cobblemon-kinetics` jar into the ignored `modpack/run/mods/` directory.
- The development-pack task downloads hash-verified pinned dependencies, and a Modrinth index records the external runtime files without committing their jars.
- The root README contains the Java 21 setup caveat and an integrated-client five-minute smoke test.

Remaining stabilization work:

- Extend pure tests to the not-alive case, configuration toggles, and claims behavior that can be isolated from a running world.
- Add NeoForge GameTests for replacement-mode on/off behavior, coupler orientation and attached-wheel requirements, configured output, explicit assignment/clearing, persistence, chunk unload, Pokémon removal, wheel removal, and claim reconstruction.
- Capture repeatable integrated-client, dedicated-server, and multiplayer smoke-test evidence on the pinned versions.
- Add guarded maintainer diagnostics for lifecycle and claim failures without logging each normal validation scan.
- Validate the existing Modrinth manifest in a supported launcher and add the unpublished local mod cleanly during developer imports.

### Exit criteria

- [ ] A clean checkout builds with the committed Gradle wrapper and Java 21 in CI and a documented contributor environment.
- [ ] Automated unit and GameTests cover the eligibility matrix, configured replacement modes, assignment persistence, claims, and lifecycle edge cases.
- [ ] Integrated-client, dedicated-server, and multiplayer smoke tests pass on the pinned dependency versions.
- [ ] A contributor can launch the complete test pack through reproducible metadata without downloading untracked third-party jars by hand.

## Milestone 2 — data-driven job foundation

**Status: Planned**

Build the reusable job system before adding many machines. The current Hydro Coupler behavior should migrate onto the framework without changing player-visible behavior or assignment data unnecessarily.

- Define a versioned, reloadable JSON schema for Pokémon selectors and workstation roles.
- Support selectors based on resource location, elemental type, Pokémon/data tags, and National Pokédex range.
- Represent shared constraints such as ownership, battle state, dimension, distance, and worker caps once.
- Introduce a code-side workstation adapter registry. Data selects a reviewed adapter; it cannot invoke arbitrary methods or mutate unrestricted game state.
- Define deterministic priority and conflict rules when several roles match one Pokémon or workstation.
- Validate data on reload and report the file, field, and reason for each rejected definition.
- Expose data generation helpers and schema examples for add-on authors.

### Exit criteria

- The existing Gen 1 Water Hydro Coupler role is expressed with the supported schema.
- Invalid resources fail safely and provide actionable diagnostics.
- Reloading data cannot duplicate work, leak entity references, or require restarting a dedicated server.
- Two packs defining overlapping selectors produce a documented deterministic result.

## Milestone 3 — broaden base Create gameplay

**Status: Planned; individual roles require design approval**

Add a small set of roles that make Pokémon operate recognizable Create workflows. Generation 1 remains the hard boundary. Candidate roles include:

| Candidate fantasy | Possible Create interaction | Status / key question |
| --- | --- | --- |
| Fire Pokémon tend a heat source | Operate or augment a supported heating workstation | Explore: preserve Create progression and avoid replacing every fuel cost. |
| Electric Pokémon provide controlled motion | Drive a reviewed kinetic-source adapter | Explore: define stress, speed, and caps without creating infinite portable power. |
| Flying Pokémon sustain airflow | Operate an encased-fan workstation | Explore: distinguish powering the fan from replacing its processing recipes. |
| Fighting Pokémon perform repetitive labor | Assist a press, hand crank, or deployer-like station | Explore: choose an interaction that is legible and does not bypass recipe timing. |
| Grass Pokémon tend crops | Operate a harvesting/planting contraption workflow | Explore: respect claims/protection mods and avoid autonomous global searches. |
| Ice Pokémon maintain cooling | Serve a future cooling-capable workstation | Explore: base Create may not expose a useful target; do not invent an add-on dependency silently. |
| Psychic Pokémon aid sorting | Operate a bounded logistics workstation | Explore: maintain deterministic filters and server performance. |

These are prompts for proposals, not implemented features. A role may be changed or rejected after prototyping.

### Role proposal checklist

Every new role pull request or design issue should answer:

1. Which Generation 1 Pokémon qualify, and is the selector explainable in-game?
2. Is the Pokémon operating Create machinery rather than merely replacing the entire machine or recipe?
3. What starts and stops work (range, battle, ownership, chunk unload, workstation removal)?
4. How are speed, stress, heat, output, and worker count capped?
5. Can a player duplicate output, assign one Pokémon to several jobs, or bypass progression?
6. What happens on a dedicated server and when an optional dependency is absent?
7. Which policy and world tests prove the behavior?
8. What animation, sound, particles, tooltip, or advancement makes the behavior understandable?

### Exit criteria

- At least three complementary roles form a useful early-game production loop.
- Each role is data-selected, bounded by a code-side adapter, and covered by lifecycle tests.
- Players can tell which Pokémon is working, where, and why it stopped.
- A single Pokémon cannot accidentally power an unbounded number of machines.

## Milestone 4 — worker experience and assignment hardening

**Status: In progress**

The MVP already has explicit management rather than arbitrary proximity selection:

- Worker Whistle selection verifies acting-player ownership and current eligibility.
- A second click assigns the stored persistent Pokémon UUID and owner UUID to a Hydro Coupler.
- Sneaking use clears the assignment.
- Coupler NBT preserves the assignment, while a per-level transient claims registry prevents a second loaded coupler from using the same worker.
- Recalling, moving away, fainting, becoming busy, entering battle, or losing the attached wheel pauses generation while retaining the assignment.
- Translated action-bar messages, particles, and goggle state communicate the basic workflow.

Remaining worker-experience work:

- Add GameTests for saved assignment reload, claim reconstruction, safe reassignment, block/entity removal, dimension changes, and ownership edge cases.
- Decide how capture, release, ownership transfer, and server-side administrative changes affect a persisted assignment.
- Improve accessibility so state is not communicated by particles or color alone; consider a richer inspection UI only if tooltips and messages are insufficient.
- Prototype autonomous pathfinding to an explicit work position, but do not teleport workers or force-load factories. Pathfinding is not part of the current MVP.
- Consider schedules, rest, feeding, friendship, or fatigue only if playtesting shows they create useful decisions rather than maintenance chores.

### Exit criteria

- [x] Players can intentionally select, assign, and unassign a worker.
- [x] An unresolved or temporarily ineligible assignment produces zero output and can resume when the exact worker returns.
- [ ] GameTests prove server restarts, saved-data reload, reassignment, and chunk unload cannot create ghost claims or free machine output.
- [ ] Multiplayer verification proves assignment messages and synchronized active state remain server-authoritative.
- [ ] Accessibility does not depend on color or particles alone.
- [ ] Any accepted pathfinding keeps simulation local, bounded, and chunk-safe.

## Milestone 5 — selected Create add-ons

**Status: Explore**

Evaluate add-ons one at a time after base Create roles are healthy. Popularity alone is not enough; an integration must be maintainable and fit the Pokémon-operated-factory premise.

### Admission criteria

- The add-on supports Minecraft 1.21.1, NeoForge, and the pinned or newly approved Create version.
- Its license and API allow the proposed integration. Prefer API use; do not copy code or assets.
- The dependency can remain optional and absence does not break class loading or data reload.
- The role cannot be expressed more cleanly against base Create.
- The maintainer burden and upstream update strategy are documented.
- Pack metadata can fetch the dependency reproducibly; its jar is not committed to this repository.

Each accepted add-on receives an isolated compatibility module or package, exact version tests, and its own support statement. A broad “supports all Create add-ons” claim is out of scope.

## Milestone 6 — Gen 1 modpack beta

**Status: Planned**

Ship an opinionated test pack that demonstrates the full Generation 1 loop and provides fast feedback without becoming the only supported way to use the mod.

- Extend the implemented pinned Modrinth metadata into a published, configured pack. The current `modpack/run/mods/` target is a local development instance, not a complete public pack.
- Include quests or advancements that teach worker eligibility, assignment, safety, and Create progression.
- Provide a showcase/test world with small machines for every supported role.
- Measure dedicated-server tick cost in representative factories with several players and workers.
- Audit recipe conflicts, keybinds, resource loading, server/client mod requirements, and upgrade behavior.
- Publish a known-issues list and a structured bug-report template including logs, versions, and reproduction steps.

### Exit criteria

- New players can discover the Pokémon-worker loop without external instructions.
- A representative multiplayer factory remains within the documented performance budget.
- Pack updates preserve worlds or clearly document required migration steps.
- Every bundled third-party project is distributed according to its license and platform rules.

## Milestone 7 — stable Gen 1 release

**Status: Planned**

The first stable release should prioritize a reliable, coherent Generation 1 experience over a large number of shallow integrations.

- Freeze and document the first supported role-data schema.
- Publish a compatibility matrix, configuration reference, data-pack author guide, and server admin guide.
- Complete localization keys and accessibility review.
- Define semantic versioning and deprecation policy for public APIs and data.
- Run multiplayer regression, performance, missing-optional-mod, and world-upgrade test suites.
- Tag source, publish checksums and artifacts, and keep release notes clear about implemented versus experimental roles.

## Explicitly out of scope for the Gen 1 cycle

- Pokémon introduced after National Pokédex number 151
- Reimplementing Create or its add-ons inside this mod
- Bundling third-party jars in Git
- Global entity scans or forced chunk loading for worker simulation
- Client-authoritative production or power calculations
- Unbounded power, stress, speed, or output based solely on the number of nearby entities
- Combat automation, capture automation, or systems that remove Cobblemon's core progression
- A guarantee to integrate every Create add-on

## Open-source contribution lanes

Contributors do not need to implement an entire machine role to help. Useful pull requests include:

- eligibility and lifecycle tests;
- dedicated-server reproduction cases;
- profiling and benchmark fixtures;
- data-schema validation and examples;
- translations and accessibility improvements;
- small compatibility investigations with upstream API links;
- documentation, diagrams, or smoke-test improvements; and
- legally original particles, sounds, models, or textures that follow the repository's contribution and licensing policy.

Keep feature proposals narrowly scoped. Link the relevant milestone, state whether the work changes the current MVP, and avoid presenting exploratory roles as shipped behavior.

The technical boundaries and testing expectations are documented in [ARCHITECTURE.md](ARCHITECTURE.md).
