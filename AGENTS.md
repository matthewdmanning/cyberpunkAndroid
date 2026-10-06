# Agent instructions

Use this file to find the authoritative project guidance for your task. Read
only the relevant references before editing, and treat the routed documents as
mandatory within their scope.

## Route the task

Use the following routes:

| Task | Read before editing |
| --- | --- |
| Public component, modifier, or API signature | [Library API conventions](docs/agents/api-conventions.md), [Reference index](docs/reference/README.md), and [Modifiers & effects catalog](docs/reference/modifiers-and-effects.md) |
| Drawing, shader, visual effect, or modifier composition | [Effect implementation rules](docs/agents/effects-rules.md), [Shader guide](docs/reference/shaders.md), relevant Android references discovered below, and official API documentation through `context7-mcp` |
| Screen layout examples & UI usage recipes | [Usage examples](docs/EFFECTS_EXAMPLES.md) |
| Path deformation, border tracers, & edge animations | [Path effects](docs/PATH_EFFECTS.md) |
| Radar sweep, circular pulse, & radial metrics | [Radial effects](docs/RADIAL_EFFECTS.md) |
| Vector icons & render variants | [Icon reference](docs/reference/icons.md) |
| Module boundary or architecture | [Architecture](ARCHITECTURE.md) |
| Contribution workflow & branching | [Contributing guidelines](CONTRIBUTING.md) |
| Physical-device feedback batch | [Feedback flow runbook](docs/agents/feedback_flow.md) |
| Verification protocol & test gates | [Testing plan](docs/testing_plan.md) |
| Local issue or specification | [Issue tracker conventions](docs/agents/issue-tracker.md) and the referenced `.scratch/` ticket |
| Domain terminology or decisions | [Domain-document routing](docs/agents/domain.md), then the relevant `CONTEXT.md` when one exists |

Verify documentation claims against the current source. Source code owns
runtime behavior; the routed documents own project policy and workflow.

## Discover Android references

List the available Android reference documents in `docs/reference/` from the repository root:

```cmd
rg --files docs/reference -g "ANDROID*.md"
```

Read the files whose names match the task before planning or implementing the
change. On Windows, don't pass `docs/reference/ANDROID*.md` as a path to `rg`; the shell
can pass the wildcard literally instead of expanding it.

## File and script locations

Place newly created files and scripts in their designated project directories instead of the repository root:

| File type | Location | Purpose |
| --- | --- | --- |
| Python automation & batch scripts | `py_scripts/` | Data processing, refactoring helpers, rating aggregation, DoE scripts |
| Shell & toolchain scripts | `scripts/` | Shell scripts (`.sh`, `.ps1`), asset converters, icon generation |
| Gradle build infrastructure | `gradle/` | Version catalog (`libs.versions.toml`), daemon configs (`gradle-daemon-jvm.properties`), wrapper files |
| Ephemeral / agent scratch scripts | Agent `scratch/` | Temporary run scripts belong in the agent session scratch directory or `.scratch/` |

Maintain clean root conventions: the repository root is reserved strictly for primary project entry points (`build.gradle.kts`, `settings.gradle.kts`, `gradlew`, `gradlew.bat`, `README.md`, `ARCHITECTURE.md`, `AGENTS.md`). Keep Gradle logic in `gradle/` or module `build.gradle.kts` files following standard Android project structure.

## Work safely

- Preserve unrelated tracked, untracked, and user-authored changes in the
  shared worktree.
- Trace callers before changing shared behavior, and fix the root cause at the
  narrowest common boundary.
- Run the smallest relevant check. Report source checks, JVM tests, Android
  builds, emulator evidence, and physical-device evidence separately.
- Don't claim a verification layer that you didn't run.
