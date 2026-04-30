---
name: security-hardening
description: Use this skill for authentication, authorization, JWT/session handling, endpoint protection, user data privacy, file upload safety, and security reviews.
---

# Security Hardening Skill

## 1. Threat Model

For every change, consider:

- Who is the current user?
- Can another user access this data?
- Can an unauthenticated client call this endpoint?
- Can a token be issued or refreshed incorrectly?
- Can client input override ownership?
- Can uploaded files break parsing or leak data?

## 2. Authentication Rules

- Token issuing requires strong proof.
- No endpoint should issue tokens based only on public/static API keys.
- Refresh token behavior must be explicit.
- Logout/revocation should be considered when refresh tokens exist.
- Passwords must never be stored or logged in plain text.
- If the current repo uses X-Api-Key and X-User-Id for an MVP identity model, keep it consistent but clearly treat it as weaker than production auth.

## 3. Authorization Rules

- Check ownership server-side.
- Do not accept userId as trusted ownership proof.
- Derive user from security context or the project-approved identity context.
- Admin endpoints require explicit admin authorization.

## 4. Logging Rules

Never log:

- JWT
- refresh token
- password
- OTP
- full uploaded PDF content
- personal financial records

## 5. File Upload Rules

For PDF/import:

- Validate size.
- Validate content type.
- Fail safely on malformed files.
- Do not assume parsed values are correct.
- Require user confirmation or validation for extracted money/date data when needed.

## 6. Output Format

```md
## Security Review

### P0 / Critical
- ...

### P1 / High
- ...

### P2 / Medium
- ...

### Recommended Patch
- ...

### Tests Required
- ...
```
