# Project governance

Create: Cobblemon Kinetics is maintained in public. Design decisions, issue
triage, review, and releases should be understandable from the repository's
issues, discussions, pull requests, changelog, and architecture notes.

## Maintainers

- [`@dallen2021`](https://github.com/dallen2021) and
  [`@CrayolaNoJutsu`](https://github.com/CrayolaNoJutsu) are equal project
  maintainers. Each may triage issues, make project decisions, review and
  merge pull requests, manage releases and access, and enforce the Code of
  Conduct.

Neither maintainer owns a permanent technical or community area. Provider
billing ownership or an account-level limitation does not grant greater
project authority. Both maintainers are default code owners. Maintainer access
is separate from authorship: all contributions, including maintainer
contributions, use the same review and CI expectations.

Work items and design ideas are unassigned by default. Their creator or
suggester is recorded separately and is never treated as the owner. An owner
is added only by an explicit choice; work genuinely shared by both maintainers
records both assignees and a short division or handoff note.

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
- The companion
  [`cobblemon-kinetics-website`](https://github.com/dallen2021/cobblemon-kinetics-website)
  repository applies the same equal-maintainer and neutral-ownership model to
  its studio and publication workflow. A profile exported from that project
  still enters this mod through the normal pull-request and CI process.
- Anyone with a conflict of interest in a conduct or security report recuses
  themselves from handling it.

An administrator may bypass the ordinary flow only for an urgent security
fix, repository recovery, or broken required automation. The reason and any
follow-up work must be documented publicly as soon as disclosure is safe.

## Releases

Releases use Semantic Versioning-style `vMAJOR.MINOR.PATCH` tags, with optional
pre-release suffixes such as `-alpha.1`. A release pull request updates the
changelog and version-facing documentation. After it merges, a maintainer runs
the Release workflow from the default branch with the requested version.
GitHub Actions rebuilds and tests that reviewed commit before creating the
exact tag, publishing the single distributable JAR, and attaching its SHA-256
checksum.

## Changing governance

Governance changes use a pull request and normal review. Adding or removing a
maintainer requires agreement from the existing non-conflicted maintainers,
an update to this file and `CODEOWNERS`, and the matching GitHub access change.
