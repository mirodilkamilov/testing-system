---
name: git-commit
description: >
  Commits staged git changes with a structured conventional commit message.
  Use whenever the user asks to commit, "git commit", "make a commit",
  "commit my changes", or "commit staged files". Runs safety checks for
  empty staging area and sensitive data before committing.
allowed-tools: Bash(git status:*), Bash(git diff --staged), Bash(git commit:*), Bash(git log --oneline -1)
---

## Context

- Current git status: !`git status`
- Current git diff: !`git diff --staged`

# Git Commit with Conventional Message

Follow these steps in order. Each step is a gate — do not skip ahead.
Do not output anything other than what is explicitly asked here.

## Step 1: Check for staged changes

If nothing staged, stop and tell the user:

> Nothing is staged. Use `git add <files>` to stage changes first.

Do not proceed further.

## Step 2: Scan for sensitive data

Flag and stop if you find any of the following:

| Pattern | Examples |
|---------|---------|
| Private key / cert headers | `BEGIN RSA PRIVATE KEY`, `BEGIN OPENSSH PRIVATE KEY`, `BEGIN EC PRIVATE KEY`, `BEGIN PGP PRIVATE KEY` |
| Secret-named variables with real values | variable names containing `password`, `passwd`, `pwd`, `secret`, `api_key`, `apikey`, `auth_token`, `access_token`, `private_key` — only flag when the value looks like a real credential, not a placeholder like `changeme`, `your-secret-here`, `<YOUR_KEY>`, or `example` |
| Known token prefixes | values starting with `sk-`, `AKIA`, `ghp_`, `gho_`, `ghs_`, `xoxb-`, `xoxp-`, `ya29.`, `AIza` |
| Hardcoded credentials in URIs | connection strings with an inline password, e.g. `postgres://user:realpassword@host` |
| `.env` files being staged | any file named `.env`, `.env.local`, `.env.production`, `.env.staging`, etc. |

If anything suspicious is found, **stop immediately**. Report:
- What was found (pattern matched)
- Which file and approximate line number
- Recommendation: remove or rotate the credential, then re-stage

Do not commit under any circumstances until the diff is clean.

## Step 3: Draft the commit message

- Which files changed and what layer/module they belong to
- Whether this is a new capability, a bug fix, a refactor, a docs change, a config change, etc.
- The primary intent — what problem does this solve?

Write a message in **Conventional Commits** format:

```
<type>(<scope>): <short description>

[body — explain the WHY, not the what; omit if obvious]

[footer — e.g. Closes #42, BREAKING CHANGE: ... <if applies>]
```

### Type guide

| Type | Use when |
|------|----------|
| `feat` | New endpoint, feature, or user-facing capability |
| `fix` | Bug fix |
| `docs` | Documentation only (CLAUDE.md, README, Swagger descriptions, comments) |
| `refactor` | Internal restructuring with no behaviour change |
| `style` | Formatting, whitespace, import ordering |
| `test` | Adding or updating tests |
| `chore` | Dependency updates, build config, tooling |
| `perf` | Performance improvement |
| `ci` | CI/CD pipeline changes |
| `revert` | Reverting a previous commit |

### Scope guide (project-specific)

Use the closest match; omit scope for broad cross-cutting changes.

| Scope | Covers |
|-------|--------|
| `auth` | JWT, login/logout, token blacklist, Spring Security config |
| `users` | User management endpoints and service |
| `tests` | Test entity CRUD endpoints and service |
| `events` | Test event endpoints and service |
| `pagination` | Filter/pagination framework (`PageWithFilterRequest`, `GenericRepository*`, `DataUtil`) |
| `db` | SQL schema, migrations, seed data |
| `redis` | Redis config, autosave, blacklist |
| `config` | Spring config, CORS, OpenAPI/Swagger, application properties |
| `dto` | Request/response DTOs, validation annotations |
| `exceptions` | Exception classes and handlers |
| `docker` | Docker / Docker Compose files |
| `deps` | Dependency version bumps (pom.xml only) |

### Subject line rules

- 72 characters max
- Lowercase, no trailing period
- Imperative mood: "add endpoint" not "added endpoint" or "adds endpoint"

### Body rules

- Wrap at 72 characters per line
- Explain *why*, not *what* — the diff already shows what changed
- Skip the body if the subject line is self-explanatory

## Step 4: Propose and confirm

Show the message clearly:

```
Proposed commit message:

feat(auth): add JWT token blacklist on logout

Tokens remain valid until expiry without an explicit blacklist,
meaning logout does not truly invalidate sessions. Redis TTL is
set to match token expiry to prevent unbounded key growth.
```

Ask: "Does this look right, or would you like to adjust it?"

- If the user confirms ("yes", "looks good", "commit it", "lgtm"), proceed to Step 5.
- If they request changes, revise and confirm once more before committing.

## Step 5: Commit

Use the following format for making the commit message:

```bash
git commit -m "<type>(<scope>): <short description>

[body — explain the WHY, not the what; omit if obvious]

[footer — e.g. Closes #42, BREAKING CHANGE: ... <if applies>]"
```

After a successful commit, show the one-line summary:

```bash
git log --oneline -1
```
