# Agent instructions

Use this file to find the authoritative project guidance for your task. Read
only the relevant references before editing, and treat the routed documents as
mandatory within their scope.

## Route the task

Use the following routes:

| Task | Read before editing |
| --- | --- |
| Public component, modifier, or API signature | [Library API conventions](docs/agents/api-conventions.md) and the relevant source callers |
| Drawing, shader, visual effect, or modifier composition | [Effect implementation rules](docs/agents/effects-rules.md), relevant Android references discovered below, and official API documentation through `context7-mcp` |
| Module boundary or architecture | [Architecture](ARCHITECTURE.md) |
| Physical-device feedback batch | [Feedback flow runbook](docs/agents/feedback_flow.md) |
| Local issue or specification | [Issue tracker conventions](docs/agents/issue-tracker.md) and the referenced `.scratch/` ticket |
| Domain terminology or decisions | [Domain-document routing](docs/agents/domain.md), then the relevant `CONTEXT.md` when one exists |

Verify documentation claims against the current source. Source code owns
runtime behavior; the routed documents own project policy and workflow.

## Discover Android references

List the available Android reference documents in `docs/agents/` from the repository root:

```cmd
rg --files docs/agents -g "ANDROID*.md"
```

Read the files whose names match the task before planning or implementing the
change. On Windows, don't pass `docs/agents/ANDROID*.md` as a path to `rg`; the shell
can pass the wildcard literally instead of expanding it.

## Work safely

- Preserve unrelated tracked, untracked, and user-authored changes in the
  shared worktree.
- Trace callers before changing shared behavior, and fix the root cause at the
  narrowest common boundary.
- Run the smallest relevant check. Report source checks, JVM tests, Android
  builds, emulator evidence, and physical-device evidence separately.
- Don't claim a verification layer that you didn't run.
