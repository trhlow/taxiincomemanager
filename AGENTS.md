# AGENTS.md

## Project Context

This project is a personal mobile application with:

- Backend: Java + Spring Boot
- Mobile: Flutter / Dart
- Database: PostgreSQL with Flyway migrations where applicable
- API style: REST JSON
- Purpose: personal income/order management, authentication or MVP identity gating, daily records, fee cycle calculation, PDF import/OCR/parse features, and secure private user data handling.

The agent must treat this as a real production-style project, not a toy demo.

---

## Working Principles

- Do not rewrite the whole project unless explicitly asked.
- Prefer small, safe, reviewable changes.
- Before editing, inspect the existing structure and follow current conventions.
- Do not invent files, APIs, classes, packages, database fields, or dependencies without checking the project first.
- If a requirement is ambiguous, make the safest reasonable assumption and document it.
- Never remove existing logic without explaining why.
- Never hardcode secrets, tokens, passwords, API keys, or private user data.
- Never weaken authentication, authorization, validation, or data privacy.
- Do not add unnecessary frameworks just because they look fancy.
- Preserve existing project architecture unless the task explicitly asks for a larger redesign.

---

## Backend Rules: Java Spring Boot

### Architecture

Use the existing package structure. Prefer a layered structure:

- controller: HTTP layer only
- service: business logic
- repository: database access
- entity/model: persistence model
- dto/request/response: API payloads
- mapper: object conversion when needed
- config: Spring configuration
- security: authentication and authorization
- exception: global exception handling

Controllers must not contain business rules.

Services must not return raw entities directly to external clients unless the project already uses that pattern and changing it would be too large.

---

## Backend Code Style

- Use clear class and method names.
- Prefer constructor injection.
- Avoid field injection.
- Validate request DTOs using Jakarta Validation where appropriate.
- Use transactions for multi-step writes.
- Keep methods small and testable.
- Do not catch generic Exception unless there is a clear recovery path.
- Use meaningful domain exceptions and global error handling.
- Use BigDecimal for money.
- Use LocalDate for business dates.
- Do not use double or float for currency calculations.

---

## Security Rules

- All endpoints that expose user data must require the project-defined authentication or identity gate unless explicitly public.
- Public endpoints must be listed and justified.
- This project may use MVP identity headers such as X-Api-Key and X-User-Id. Treat that as a local/MVP gate, not as full production authentication.
- Do not create endpoints that issue tokens without strong ownership proof.
- Do not trust client-side userId when a current authenticated or gated user can be derived from the request context.
- Never allow access to user-owned data by guessing or changing userId.
- Never log passwords, API keys, tokens, refresh tokens, OTPs, or private financial data.
- Passwords must be hashed with a strong password encoder if password authentication exists.
- Token expiration and refresh behavior must be explicit if tokens are introduced.
- Validate file uploads: size, type, extension, and content assumptions.

---

## Mobile Rules: Flutter / Dart

- Keep UI, state, service, and model layers separated.
- Do not put API calls directly inside large widget build methods.
- Use typed models for API responses.
- Handle loading, empty, error, and success states.
- Never store access tokens in plain SharedPreferences if secure storage is available.
- API base URL must be environment-configurable.
- UI must work on small Android screens.
- Forms must validate user input before sending to backend.
- Date input should use date picker, not fragile manual strings.

---

## Domain Rules

Important domain behavior:

- Daily orders/income records must be grouped by actual working date.
- Fee cycles are:
  - Day 01 to day 10
  - Day 11 to day 20
  - Day 21 to the end of the month
- For months with 28, 29, 30, or 31 days, the last cycle ends on the actual final day of that month.
- Backend calculations are the source of truth.
- One-driver order: current user receives full calculated net amount.
- Two-driver order: received amount must be divided correctly based on the project rule.
- Tip/bonus must be included in final received amount.
- Fee calculation must be deterministic and tested.

Do not change these rules casually.

---

## Database Rules

- Use migrations if the project uses Flyway/Liquibase.
- Do not modify schema silently.
- Avoid destructive migrations.
- Add indexes for frequently queried fields such as userId, workingDate, cycle range, and createdAt.
- Monetary values should not use floating point types.
- Prefer BigDecimal in Java and decimal-compatible DB columns.

---

## Testing Rules

For backend changes:

- Add or update unit tests for business logic.
- Add integration tests for critical API/security behavior where practical.
- Test fee cycle boundaries:
  - day 01
  - day 10
  - day 11
  - day 20
  - day 21
  - month end: 28/29/30/31
- Test unauthorized access.
- Test user isolation.

For Flutter changes:

- Add widget tests or unit tests for important calculations/state logic where practical.
- Manually verify small-screen layout assumptions if tests are not available.

---

## Commands

Before finalizing backend changes, prefer running:

```bash
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```

If there is no Maven wrapper, use:

```bash
mvn test
```

For Flutter:

```bash
flutter analyze
flutter test
```

If commands fail because the environment is missing tools, report that clearly and do not pretend tests passed.

---

## Review Checklist Before Final Answer

Before saying the task is done, check:

- Does the code compile?
- Are tests updated or is the reason for not adding tests explained?
- Is authentication or identity gating still safe?
- Is user data isolated?
- Are money/date calculations deterministic?
- Are edge cases handled?
- Did you avoid unrelated refactors?
- Did you document risky assumptions?
