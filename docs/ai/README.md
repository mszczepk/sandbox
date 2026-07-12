# AI Agent Instructions

## Quick start

Copy `.copilot/` to your `~/.copilot/`, overwriting existing files and leaving any extra local files untouched:

```bash
cp -r 'docs/ai/.copilot/.' ~/.copilot/
```

## How it works

GitHub Copilot reads custom instruction files to apply consistent rules across code generation, tests, and style.

The idea:

* Create instructions for coding conventions the team has agreed on.
* Keep the instructions files in the git repository and review.
* Sync them locally and GitHub Copilot applies them consistently across all projects.

| Directory               | Purpose                                                           |
|-------------------------|-------------------------------------------------------------------|
| `~/.copilot/`           | User-global instructions, applied to all projects on the machine. |
| `$PROJECT_ROOT/.github` | Project-scoped instructions, applied only within that repository. |

| File                                             | Purpose                                                           |
|--------------------------------------------------|-------------------------------------------------------------------|
| `~/.copilot/copilot-instructions.md`             | General instructions for all generated code and file types.       |
| `~/.copilot/instructions/{type}.instructions.md` | Instructions for specific file types, e.g. `.md`, `.kt`, `.java`. |
| `~/.copilot/agents/{name}.agent.md`              | Custom reusable agent definitions.                                |
