# Security Policy

## Current support status

Create: Cobblemon Kinetics is pre-release software. The latest tag is an alpha,
and development builds identify themselves as `0.1.0-SNAPSHOT`.

| Version or branch | Security support |
| --- | --- |
| Current default branch / `0.1.0-SNAPSHOT` | Best-effort reports accepted |
| Latest tagged alpha release | Best-effort reports accepted |
| Private website/studio prototype on the default branch | Best-effort reports accepted |
| Older snapshots, forks, or modified jars | Not supported directly |
| Unpinned Minecraft, NeoForge, Create, or Cobblemon combinations | Reproduction may be requested on pinned versions |

Security fixes may require users to update dependencies, configuration, or the
mod itself. Until the first stable release, compatibility and save migration
are not guaranteed.

## Reporting a vulnerability

Please do **not** disclose a suspected vulnerability in a public issue,
discussion, pull request, chat, or crash-log paste.

Preferred reporting path:

1. Open this repository's **Security** tab.
2. Choose **Advisories** and **Report a vulnerability** if private vulnerability
   reporting is enabled.
3. If no private reporting option is available, open a minimal public issue
   asking maintainers to establish a private contact channel. Do not include
   exploit details, affected server addresses, player identities, or secrets.

Include as much of the following as is safe:

- affected commit or mod version;
- Java, Minecraft, NeoForge, Create, Cobblemon, and Kotlin for Forge versions;
- single-player, integrated-server, or dedicated-server context;
- concise impact and who can trigger it;
- prerequisites and a minimal reproduction;
- relevant logs, stack traces, configuration, or proof-of-concept material;
- whether the problem reproduces with this mod removed; and
- suggested mitigations, if known.

Redact access tokens, private server addresses, filesystem usernames, player
identifiers, and unrelated log contents. Maintainers may request an encrypted
or otherwise safer transfer method for large proof-of-concept files.

## What counts as a security issue

Examples include:

- remote code execution, arbitrary file access, or unsafe deserialization;
- permission or ownership bypass that lets one player control another player's
  Pokémon or machinery;
- client-controlled production, assignment, or kinetic state that can be
  forged against a server;
- practical item, power, or resource duplication with multiplayer impact;
- denial of service through unbounded entity scans, chunk loading, packets,
  particles, or malformed saved data;
- a reliable server crash trigger available to an untrusted player;
- exposure of private player or server information; and
- an OAuth/allowlist/RLS bypass, draft or private-note exposure, forged
  publication bundle, preview-to-production credential leak, or unauthorized
  private asset access;
- a vulnerable bundled or declared dependency that materially affects this
  mod's users.

Ordinary gameplay bugs, balancing concerns, unsupported-version crashes,
visual glitches, and feature requests should use the public issue tracker
unless they create a meaningful confidentiality, integrity, or availability
risk.

## Response and disclosure

This volunteer project has no guaranteed response-time service level. The
maintainers will make a best effort to:

1. acknowledge a complete private report;
2. reproduce and assess its impact on the pinned dependency set;
3. coordinate a fix or mitigation and identify affected versions;
4. prepare release notes and credit, unless anonymity is requested; and
5. agree on public disclosure after users have a reasonable opportunity to
   update.

Please allow maintainers to investigate before publishing exploit details.
Good-faith research that respects privacy, avoids unnecessary disruption, and
follows this coordinated process is welcome. This policy does not authorize
testing against servers, accounts, or systems without their owners' permission.

## Dependency vulnerabilities

Create, Cobblemon, NeoForge, Minecraft, Kotlin for Forge, Gradle plugins, and
other dependencies are separate upstream projects. Report a vulnerability in
upstream code through that project's private security process. You may also
notify this project privately when the issue affects the pinned versions or a
Create: Cobblemon Kinetics integration path.

Do not send this project private upstream source, embargoed details, or assets
unless you are authorized to share them. This repository does not redistribute
upstream jars or assets.
