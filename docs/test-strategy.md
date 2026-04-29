# Test strategy

## Backend

| Layer | Tooling | Command | Scope |
|-------|---------|---------|--------|
| Unit | Maven Surefire | `mvn test` (from `backend/`) | Fast tests including `OrderCalculationServiceTest` and other unit tests. |
| Integration | Maven Failsafe + Testcontainers | `mvn verify -Pintegration` | Classes named `*IT` (e.g. `SecurityFilterIT`) spin up real PostgreSQL in Docker. |

**CI:** GitHub Actions (`.github/workflows/backend-ci.yml`) runs both `mvn test` and `mvn verify -Pintegration` on Ubuntu with Docker available so integration tests are not silently skipped.

On a developer machine **without** Docker, Testcontainers may skip (`disabledWithoutDocker = true`); CI remains the source of truth for full integration coverage.

## Mobile

- `flutter test` — widget/unit tests (e.g. money math, JSON date parsing).
- Manual smoke: onboarding → dashboard ping from **Cá nhân** screen, create order, schedule toggle.

## What is not automated here

- End-to-end UI tests (Patrol / integration tests on device) are out of scope for this repository snapshot.
