---
name: test-quality-gate
description: Use this skill before finishing coding tasks to run or define backend/mobile checks, tests, static analysis, and quality gates.
---

# Test Quality Gate Skill

## 1. Backend Checks

Prefer running:

```bash
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```

Fallback:

```bash
mvn test
```

Also check if available:

```bash
./mvnw verify
```

## 2. Flutter Checks

Prefer running:

```bash
flutter analyze
flutter test
```

## 3. Required Test Areas

For this project, prioritize tests for:

- fee cycle calculation
- day 01/10/11/20/21 boundaries
- month-end 28/29/30/31
- money calculation
- one-driver vs two-driver order
- unauthorized access
- user data isolation
- invalid input validation
- PDF import failure handling

## 4. If Tests Cannot Run

Do not claim success.

Report:

- command attempted
- error summary
- likely cause
- what should be run locally

## 5. Final Output Format

```md
## Quality Gate

### Commands Run
- ...

### Result
- Pass/Fail/Not run

### Tests Added or Updated
- ...

### Risks
- ...

### Next Fixes
- ...
```
