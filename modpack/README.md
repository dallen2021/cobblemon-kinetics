# Cobblemon Kinetics development modpack

This folder is the repo-local launch target for the mod. Run:

```sh
./gradlew buildAndInstall
```

The distributable Cobblemon Kinetics JAR is copied to `modpack/run/mods/`.
The same task downloads the three pinned required mod files from Modrinth and
verifies their SHA-512 hashes. All JARs and generated game state are ignored;
never commit Create, Cobblemon, NeoForge, or Minecraft binaries.

[`modrinth.index.json`](modrinth.index.json) is the reproducible external-file
manifest. It intentionally omits the unpublished local Cobblemon Kinetics JAR;
`buildAndInstall` supplies that file directly.

Pinned runtime versions are recorded in [`versions.json`](versions.json). A launcher-ready public pack is planned once the gameplay loop is stable enough for playtesting.
