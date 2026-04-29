# Known limitations

- **Single shared API key** for all clients — convenient for a personal tool, inadequate for multi-tenant SaaS without additional controls.
- **Bearer tokens do not expire** automatically in the current schema; compromise requires manual revocation (future work).
- **No OAuth / social login** — identity is anonymous display name plus server-issued opaque token after init.
- **Integration tests require Docker** (Testcontainers); local builds without Docker may not execute `*IT` classes.
- **Flutter** targets recent stable SDKs; older devices are best-effort only.
- **Monetary values** use integer cents (VNĐ đồng) in APIs — clients must remain consistent when formatting for display.

For security direction, see [security-design.md](security-design.md).
