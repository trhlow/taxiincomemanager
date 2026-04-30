---
name: flutter-mobile-review
description: Use this skill when reviewing or modifying Flutter/Dart mobile code, UI screens, API services, state handling, forms, date pickers, token storage, and mobile UX.
---

# Flutter Mobile Review Skill

## 1. Inspect First

Before editing:

- Identify state management approach.
- Locate API service/client.
- Locate models.
- Locate screens/widgets.
- Check error/loading handling.
- Check token storage.
- Check environment configuration.

Do not introduce a new state management library unless requested.

## 2. Mobile Priorities

Review in this order:

1. Correctness of user flow
2. Secure token handling
3. API error handling
4. Form validation
5. Small-screen layout
6. State consistency
7. Maintainability
8. Tests

## 3. UI Rules

- Avoid overflow on small screens.
- Use date picker for date input.
- Use proper keyboard types for money/numbers.
- Show loading state when submitting.
- Prevent double-submit.
- Show useful error messages.
- Keep widgets reasonably small.

## 4. API Rules

- Do not make raw HTTP calls everywhere.
- Centralize API base URL.
- Centralize token or identity header injection.
- Handle 401/403 explicitly.
- Use typed models.
- Avoid storing tokens in plain storage if secure storage exists.

## 5. Output Format

```md
## Flutter Review

### Bugs
- ...

### UX Problems
- ...

### Security Problems
- ...

### Refactor Suggestions
- ...

### Suggested Tests
- ...
```
