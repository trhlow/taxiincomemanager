---
name: backend-springboot-review
description: Use this skill when reviewing or modifying Java Spring Boot backend code, especially controllers, services, repositories, DTOs, security, transactions, and tests.
---

# Backend Spring Boot Review Skill

When this skill is active, follow this workflow:

## 1. Inspect First

Before editing:

- Identify package structure.
- Locate controller/service/repository/entity/dto layers.
- Check existing validation style.
- Check existing exception handling.
- Check existing security configuration.
- Check test patterns.

Do not assume architecture.

## 2. Review Priorities

Review in this order:

1. Security/authentication
2. Data ownership/user isolation
3. Business correctness
4. Transaction correctness
5. Validation/error handling
6. Test coverage
7. Maintainability
8. Performance

## 3. Backend Rules

- Controllers must be thin.
- Services own business logic.
- Repositories must not contain business decisions unless using explicit query methods.
- DTOs should protect API boundaries.
- Do not expose internal entities unnecessarily.
- Use BigDecimal for money.
- Use LocalDate for business dates.
- Use @Transactional for atomic write flows.
- Do not trust userId from request body when current user can be derived from request/auth context.
- If the repo currently uses MVP headers such as X-Api-Key and X-User-Id, preserve the pattern unless asked to migrate auth, but do not treat it as production-grade authentication.

## 4. Required Checks for This Project

Always check:

- Can one user access another user's data?
- Can a token be issued without proper credentials?
- Are private endpoints protected?
- Are fee cycles correct?
- Are month-end cases handled?
- Are money calculations using safe types?
- Are invalid inputs rejected?

## 5. Output Format

When reviewing, output:

```md
## Backend Review

### Critical Issues
- ...

### Important Issues
- ...

### Improvements
- ...

### Suggested Tests
- ...

### Safe Patch Plan
1. ...
2. ...
3. ...
```

When editing, make the smallest safe patch.
