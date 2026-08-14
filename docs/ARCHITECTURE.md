# Create: Cobblemon Kinetics architecture

Create: Cobblemon Kinetics is a server-authoritative compatibility mod that lets Pokémon operate Create machinery. Its stable technical mod ID is `cobblemon_kinetics`, and its repository and artifact slug is `cobblemon-kinetics`.

## Supported stack

| Component | Pinned version |
| --- | --- |
| Minecraft | 1.21.1 |
| Mod loader | NeoForge 21.1.244 |
| Java | 21 |
| Create | 6.0.10 |
| Cobblemon | 1.7.3 |
| Kotlin for Forge | 5.12.0 |
| Architectury Loom | 1.11.458 |
| Architectury plugin | 3.4.164 |
| Node.js | 24.19.0 (website/data workspace only) |
| pnpm | 11.19.0 |
| Web runtime | Next.js 16 App Router |
| Draft backend | Supabase Postgres/Auth/Storage |

These versions define the supported development and test environment. Changes to any pin require an explicit compatibility pull request with a successful build and in-game integration test.

## Scope and status

### Current MVP: implemented behavior

The MVP changes one automation path through two deliberately separate pieces:

1. A custom **Hydro Coupler** is the native Create kinetic source. It produces configured, fixed rotation only when its assigned Pokémon is active and a Create Water Wheel or Large Water Wheel is attached to the coupler's shaft face. The wheel is a visible, driven part of the kinetic network; it does not receive a synthetic fluid score from the Pokémon.
2. A narrow water-wheel mixin suppresses Create's environmental fluid score only when `replaceNaturalWaterPower` is enabled. When the option is disabled, the mixin returns immediately and normal Create fluid behavior remains intact.

Worker selection and assignment are explicit:

- The player right-clicks an owned, eligible deployed Pokémon with a **Worker Whistle**. The whistle stores the Pokémon's persistent Cobblemon UUID and the player's UUID in item custom data.
- The player right-clicks a Hydro Coupler with that whistle. The coupler claims the Pokémon and persists the Pokémon and owner UUIDs in block-entity data.
- Shift-right-clicking a coupler with a whistle releases and clears its assignment.
- A loaded-level claims registry prevents the same Pokémon UUID from operating two loaded couplers at once.

On its server tick cadence, the coupler searches only its configured local area for the specifically assigned Pokémon. It becomes active only when that exact entity is loaded, satisfies the configured ownership policy, remains eligible, and the required wheel is attached. Eligibility currently requires an alive, non-fainted, idle, non-battling Water-type Pokémon; the defaults additionally require player ownership and National Pokédex number 1 through 151.

An active coupler supplies the configured fixed speed and stress-capacity coefficient (defaults: 8 RPM and 64 SU/RPM), synchronizes state through the normal block-entity and Create kinetic paths, exposes a Create-goggle status, and optionally emits a splash stream between worker and wheel. No water blocks are placed and clients do not decide production.

This is intentionally narrow. It proves a native Create generator, persistent explicit assignment, bounded Cobblemon entity validation, lifecycle-aware claims, optional environmental-power replacement, multiplayer authority, unit-testable eligibility, and the build-install pipeline before the project adds a generalized job system.

### Implemented private website vertical slice

The repository also contains a separately runnable Node workspace for the
first Squirtle → Hydro Coupler content flow. It includes versioned JSON
Schemas, a workbook dry-run importer with flavor-text quarantine, deterministic
published-data and mod-profile generation, a Next.js wiki/private studio, and
Supabase migrations with database-backed access and revision checks.

This does not make Supabase authoritative for gameplay. Drafts live in
Supabase; only reviewed files committed under `data/published` are public, and
the mod reads only the generated work profile. The Java loader validates
format 1 resources and registered adapter IDs on server-data reload. The
existing Hydro implementation does not consume profile balance/eligibility
yet, so current configuration, NBT, and world behavior remain unchanged.

### Roadmap only: not implemented yet

The following ideas are architectural direction, not current features:

- Live Hydro gameplay selected and balanced by published work profiles (the
  contract and validation loader exist, but live migration is deferred)
- Other Create machines, kinetic sources, processing steps, logistics, or contraptions
- Compatibility modules for Create add-ons
- Non-Water roles, autonomous pathfinding, schedules, fatigue, happiness, leveling, stat/move scaling, or a management GUI
- NeoForge GameTests, completed dedicated-server/multiplayer evidence, and a launcher-ready public pack
- A Fabric build
- Pokémon outside Generation 1

Do not document a roadmap item as available gameplay until its acceptance tests ship with the implementation.

## Design principles

1. **Integrate at narrow boundaries.** Create-specific hooks and Cobblemon-specific lookups stay in adapter packages so upstream changes are localized.
2. **The server owns work decisions.** Eligibility, assignment, contribution, and machine state changes happen on the logical server to avoid desynchronization and duplication.
3. **Prefer composition to copied code.** Use supported APIs, events, tags, and small, documented access hooks. Never copy an add-on's implementation or assets into this repository.
4. **Make future roles data-driven.** The current Gen 1 Water eligibility policy is intentionally code-defined, but additional species and machine behavior must not grow into a long chain of hard-coded `if` statements.
5. **Fail safely.** Missing entities, unloaded chunks, invalid data, or unavailable optional integrations must result in no work—not a crash or free power.
6. **Keep the first release observable.** Eligibility failures and invalid role data should be diagnosable through concise debug logging without spamming normal logs.

## Runtime flow

### Selection and assignment

```text
Player right-clicks a deployed Pokémon with Worker Whistle
        |
        v
Server verifies ownership and evaluates current worker eligibility
        |
        v
Whistle stores Pokémon UUID + owner UUID as item custom data
        |
        v
Player right-clicks a Hydro Coupler with the selected whistle
        |
        v
Server-level claims registry rejects a conflicting loaded assignment
        |
        v
Coupler persists Pokémon UUID + owner UUID and requests a kinetic update
```

Shift-right-clicking the coupler releases the claim and clears its persisted assignment.

### Coupler activation and kinetic generation

```text
Hydro Coupler reaches its short server validation cadence
        |
        v
Query loaded Pokémon inside a bounded box around the coupler, then enforce
the configured exact spherical distance
        |
        v
Match only the assigned persistent Pokémon UUID and, when configured,
the stored owner UUID
        |
        v
Eligibility policy checks alive, health, busy, battle, ownership,
Gen-1 setting, and Water type
        |
        v
Coupler verifies a Create Water Wheel or Large Water Wheel on its shaft face
        |
        v
Active-state transition requests Create's normal generated-rotation update
        |
        v
Create propagates configured fixed RPM and capacity through the wheel/network
```

No eligible assigned entity or no attached wheel means zero generated speed and capacity. The coupler retains the assignment while waiting, allowing work to resume when the entity returns to range or becomes eligible again.

### Environmental water-wheel rule

```text
Create evaluates a water wheel's environmental fluid flow
        |
        v
If replaceNaturalWaterPower is false: leave Create behavior unchanged
        |
        v
If replaceNaturalWaterPower is true: set flow score to zero and cancel that evaluation
```

The mixin never looks for Pokémon and never provides worker power. It contains only the configurable policy for disabling environmental flow; the Hydro Coupler owns all worker lookup and kinetic generation.

## Component boundaries

Package names may evolve during the initial scaffold, but responsibilities should remain separated.

### Bootstrap, registration, and configuration

`CobblemonKinetics` and the registry classes own NeoForge block, item, and block-entity registration. `CobblemonKineticsConfig` owns the server configuration for environmental water power, worker radius, ownership and Gen 1 restrictions, fixed Hydro RPM/capacity, and particles. Client-only registration must remain behind a client distribution boundary when client-specific features are added.

### Hydro Coupler Create component

`HydroCouplerBlock` defines the directional shaft face. `HydroCouplerBlockEntity` extends Create's `GeneratingKineticBlockEntity` and owns persisted assignment, bounded worker resolution, attached-wheel validation, active-state transitions, fixed generated speed/capacity, goggle status, and optional particles. It is the only current worker-powered kinetic source.

Create network recalculation should occur on meaningful state transitions rather than every tick. A missing worker or wheel must produce zero rather than retaining free power.

### Environmental water-wheel mixin

`WaterWheelBlockEntityMixin` is a version-sensitive but intentionally narrow compatibility hook. It intercepts Create's environmental flow evaluation, sets the flow score to zero, and cancels only when `replaceNaturalWaterPower` is true. It must not acquire workers, create assignments, calculate contribution, or duplicate coupler logic.

### Worker Whistle interaction

`WorkerWhistleItem` owns the two-click player interaction. Pokémon interaction applies the configured ownership policy and verifies eligibility before recording UUIDs. Coupler interaction verifies that the stored assigning player is the acting server player, assigns through the coupler/claims registry, or clears while the player is sneaking. Player-facing failures use translatable messages.

### Cobblemon worker facts and eligibility policy

`PokemonWorker` converts `PokemonEntity` state into the integration-neutral `WorkerFacts` record. `WorkerEligibility` is the pure policy for alive, fainted, busy, battling, ownership, Pokédex range, and Water-type checks. `WorkerRejection` provides deterministic rejection reasons used by the whistle.

The policy is covered by fast unit tests without booting a Minecraft client. World location and attached-wheel checks remain in the coupler because they require level state.

### Loaded assignment claims

`WorkerAssignmentRegistry` owns transient, per-`ServerLevel` claims keyed by persistent Pokémon UUID. It prevents a second loaded coupler from claiming the same worker, uses weak level keys, and releases a claim when the owning coupler clears or invalidates. A persisted coupler rebuilds its transient claim during initialization; the registry itself is not saved data.

### Diagnostics and presentation

The MVP exposes whistle action-bar feedback, deterministic rejection text, Create-goggle state, and optional splash particles. Future diagnostics must remain guarded; normal gameplay must not log every scan or rejected Pokémon.

## Query and performance rules

Coupler validation occurs often enough to respond to worker state, so implementations must bound their work.

- Query only the coupler's configured local bounding volume and only loaded entities.
- Resolve only the explicitly assigned persistent Pokémon UUID; do not select arbitrary nearby candidates.
- Never scan all entities in a level or force chunks to load.
- Do not persist raw entity references across ticks.
- Keep validation local to loaded Hydro Coupler block entities; do not create a second global worker tick loop.
- Request Create generated-rotation recalculation when active state or configured contribution changes, not on every successful validation.
- If caching is added, cache derived results for a short, documented lifetime and invalidate on entity removal, ownership/battle changes, reassignment, chunk unload, coupler removal, and wheel removal.
- Profile before introducing complex caches; correctness is more important than speculative optimization.

## Multiplayer and lifecycle behavior

- With the default ownership policy, selection requires the acting player to own the Pokémon and active validation matches both the persisted Pokémon UUID and owner UUID. When a server disables that policy, non-player-owned workers may be selected, but the assigning player still owns the station assignment.
- Battle participation is read from authoritative Cobblemon state. Entering battle must stop qualification at the next supported reevaluation.
- Only the assigned, same-dimension, loaded entity inside the configured coupler volume is considered.
- Recalling, unloading, moving, fainting, or making the worker busy/in battle pauses generation without erasing its assignment.
- Removing the attached wheel pauses generation. Clearing or invalidating the coupler releases its transient claim.
- A loaded-level claim prevents one persistent Pokémon UUID from powering multiple loaded couplers. Claims are reconstructed from coupler data after load.
- Couplers and workers do not force chunks to remain loaded.
- Dedicated-server startup must not load client-only classes.

## Persistence model

- **Whistle selection:** Pokémon UUID and selecting owner UUID are stored in the ItemStack's custom data.
- **Coupler assignment:** Pokémon UUID, owner UUID, and synchronized active state are stored in Hydro Coupler block-entity NBT.
- **Runtime claim:** the per-level claims registry is transient and rebuilt when persisted couplers initialize.
- **Entity identity:** assignments use the persistent Cobblemon Pokémon UUID, never a transient Minecraft entity ID or raw object reference.
- **Missing entity behavior:** an unresolved assignment is a waiting state with zero output, not deletion, forced loading, or an error.

## Implemented server configuration

| Setting | Default | Current responsibility |
| --- | ---: | --- |
| `replaceNaturalWaterPower` | `true` | Suppress Create water-wheel environmental flow scoring |
| `workerRadius` | `6.0` | Bound the assigned-entity query around each coupler |
| `requirePlayerOwned` | `true` | Require player ownership during active eligibility evaluation |
| `genOneOnly` | `true` | Restrict active workers to National Pokédex numbers 1–151 |
| `hydroRpm` | `8` | Fixed active Hydro Coupler rotation speed |
| `hydroCapacity` | `64` | Fixed active stress-capacity coefficient per RPM |
| `showWorkParticles` | `true` | Enable the worker-to-wheel splash stream |

The assigning player's UUID always protects a populated coupler from another player replacing or clearing its assignment. The Pokémon ownership check itself follows `requirePlayerOwned` consistently during selection and active validation.

## Published work-profile contract

Format 1 is defined by
`packages/domain/schemas/work-profile.schema.json`, generated deterministically
into `src/main/resources/data/cobblemon_kinetics/work_profiles`, and validated
again by the Java parser. The first approved resource has this shape:

```json
{
  "format_version": 1,
  "id": "cobblemon_kinetics:hydro_operator",
  "title": "Hydro Operator",
  "priority": 0,
  "status": "approved",
  "selector": {
    "kind": "type",
    "types": ["cobblemon:water"],
    "national_dex": { "min": 1, "max": 151 }
  },
  "constraints": {
    "requires_owner": true,
    "must_be_alive": true,
    "must_not_be_fainted": true,
    "must_not_be_battling": true,
    "must_be_idle": true
  },
  "workstation": {
    "adapter_id": "cobblemon_kinetics:hydro_coupler",
    "registry_ids": ["cobblemon_kinetics:hydro_coupler"],
    "required_attachment_tag": "create:water_wheels",
    "radius": 6.0
  },
  "contribution": {
    "mode": "fixed",
    "rpm": 8,
    "capacity_per_rpm": 64,
    "efficiency_multiplier": 1.0
  },
  "public_rationale": "Eligible Generation I Water-type workers replace passive flowing-water automation while retaining a visible attached wheel."
}
```

The schema, deterministic exporter, Java parser, server reload, resource-ID
validation, and actionable rejection logs are implemented. Format 1 supports
only the selectors and fixed contribution mode represented by its
discriminated unions. Unknown fields, formats, adapters, registry IDs, or
out-of-range values are rejected. Conflict resolution between multiple active
profiles and live adapter execution remain milestone work.

Data-driven does not mean data can directly mutate arbitrary Create internals.
Each `workstation.adapter_id` maps to a reviewed code-side descriptor and,
later, a bounded adapter implementation.

## Website, drafts, and publication boundary

Supabase holds mutable draft heads plus immutable revisions. Writes include an
expected revision and client mutation ID; a stale write returns a conflict
rather than overwriting another maintainer. Approval freezes an exact revision
inside a publication batch but does not publish it.

The studio downloads a public-only bundle. The local exporter verifies its
schema, hash/signature, removes and regenerates only owned output directories,
sorts records deterministically, and writes `data/published/manifest.json`.
CI rejects any file/hash/schema drift. Wiki server components read those Git
files only. Private notes, comments, raw import rows, user IDs, and quarantined
flavor text are not fields in the publication schema.

GitHub OAuth identities are allowlisted by durable numeric ID. Each protected
server request checks active database membership, and RLS remains the final
deny-by-default boundary. Daniel and Jake have equal `maintainer` capability;
task ownership stays blank unless explicitly assigned. Deployment and recovery
details are in [`WEBSITE.md`](WEBSITE.md).

## Optional Create add-on compatibility

Create and Cobblemon are required dependencies for the core mod. Each future add-on integration should be isolated behind its own compatibility boundary and loaded only when that add-on is present. Contributors proposing one must document:

- exact supported add-on and version;
- whether its license permits any reused material (prefer no copied code or assets);
- the public API or minimal access hook used;
- behavior when the add-on is absent;
- dedicated-server and multiplayer test coverage; and
- whether the role remains balanced in a base-Create pack.

Third-party mod jars do not belong in source control. Development and modpack dependencies should be declared through reproducible Gradle or pack metadata.

## Testing strategy

### Fast policy tests

Implemented unit tests currently cover:

- healthy, owned Gen 1 Water type: eligible;
- fainted, busy, and battling workers: rejected;
- wild, non-Water, and later-generation workers: rejected; and
- inclusive Generation 1 boundaries at National Pokédex numbers 1 and 151.

Additional pure tests should cover the not-alive case, configuration switches, and any future contribution/profile policy. Candidate ordering is no longer part of the MVP contract because the coupler resolves one explicitly assigned persistent UUID.

### NeoForge GameTests or integration tests

Verify world-facing behavior with the pinned dependency set:

- replacement mode suppresses still/flowing-fluid wheel power while disabling replacement mode leaves Create behavior unchanged;
- a correctly oriented coupler with an attached wheel and eligible assigned Pokémon starts the network at configured RPM/capacity;
- an unassigned worker, missing attached wheel, out-of-range worker, recall, faint, busy state, or battle stops generated output;
- selection, assignment, explicit clearing, saved-data reload, block removal, and chunk unload clean up or reconstruct claims correctly;
- two players, several couplers, and one Pokémon UUID cannot cross-assign or duplicate power;
- a dedicated server starts without client-class loading errors.

These world-facing GameTests are **future work**; they are not present in the current test suite.

### Manual smoke test and development install

The root README contains an implemented five-minute integrated-client checklist for selection, assignment, power, pause/resume, clearing, and environmental-flow replacement. Gradle's implemented `buildAndInstall` task builds/tests the project and copies the remapped jar into the ignored `modpack/run/mods/` target.

That target is not a published launcher instance. It does download and
SHA-512-verify the pinned Create, Cobblemon, and Kotlin for Forge jars, and the
adjacent Modrinth index records the same external files. Repeatable client and
multiplayer evidence remains required before a release candidate.

## Contributor guidance

- Keep compatibility hooks small and place game rules in testable project-owned code.
- Include an issue or design note before adding a new machine role; explain the Pokémon fantasy, Create interaction, balance, and failure behavior.
- Add tests for every eligibility predicate and lifecycle transition changed by a pull request.
- Avoid unrelated formatting or generated-file churn.
- Preserve attribution and comply with upstream licenses. An idea or API integration is acceptable; copied code, models, textures, sounds, translations, or data require explicit compatible licensing.
- Treat the version table above as a contract. Dependency upgrades and new gameplay should be separate pull requests whenever practical.

See [GEN1_ROADMAP.md](GEN1_ROADMAP.md) for planned work beyond the water-wheel MVP.
