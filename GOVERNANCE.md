# Project governance

Create: Cobblemon Kinetics is maintained in public. Design decisions, issue
triage, review, and releases should be understandable from the repository's
issues, discussions, pull requests, changelog, and architecture notes.

## Maintainers

- [`@dallen2021`](https://github.com/dallen2021) is the repository owner and
  release steward.
- [`@CrayolaNoJutsu`](https://github.com/CrayolaNoJutsu) is an administrator
  and community moderator, with authority to triage issues, review and merge
  pull requests, manage releases, and enforce the Code of Conduct.

Both maintainers are default code owners. Maintainer access is separate from
authorship: all contributions, including maintainer contributions, use the
same review and CI expectations.

## Decisions and review

- Routine fixes follow the normal issue and pull request workflow.
- New worker roles, public APIs, mixins, dependencies, compatibility targets,
  and saved-data changes start with an issue or discussion before code.
- Maintainers seek consensus using player impact, compatibility, maintenance
  cost, performance, licensing, and test evidence.
- A pull request needs a green required build and all review conversations
  resolved. Non-author review is encouraged for gameplay, API, security,
  release, and governance changes, but is not a mandatory merge gate. The
  repository squash-merges accepted work.
- Anyone with a conflict of interest in a conduct or security report recuses
  themselves from handling it.

An administrator may bypass the ordinary flow only for an urgent security
fix, repository recovery, or broken required automation. The reason and any
follow-up work must be documented publicly as soon as disclosure is safe.

## Releases

Releases use Semantic Versioning-style `vMAJOR.MINOR.PATCH` tags, with optional
pre-release suffixes such as `-alpha.1`. A release pull request updates the
changelog and version-facing documentation before a maintainer tags the exact
reviewed commit. GitHub Actions rebuilds the source, runs tests, publishes the
single distributable JAR, and attaches its SHA-256 checksum.

## Changing governance

Governance changes use a pull request and normal review. Adding or removing a
maintainer requires agreement from the existing non-conflicted maintainers,
an update to this file and `CODEOWNERS`, and the matching GitHub access change.
