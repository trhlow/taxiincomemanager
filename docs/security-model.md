# Security Model

This application is designed as a private single-user income tracking tool.

The backend currently uses one primary user per deployment. Device tokens authenticate access to that user's data.

This model is acceptable for private deployment but is not designed for public multi-tenant SaaS.

## Current Protections

- API key gate for backend access
- One-time setup secret for initial user creation
- Bearer access token stored as a hash in the database
- Token stored in Flutter secure storage
- Server-side user resolution from token
- No client-provided user id is trusted
- Database constraint for a single primary user

## Known Limitations

- One global user per deployment
- No rate limiting yet
- No token rotation policy yet
- No multi-tenant account isolation
