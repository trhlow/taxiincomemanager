# Security design

## Current model (production-oriented baseline)

| Layer | Mechanism | Notes |
|-------|-----------|--------|
| Transport | HTTPS assumed in real deployments; local dev often uses plain HTTP. | Terminate TLS at a reverse proxy or platform ingress. |
| Service authentication | Shared secret `X-Api-Key` matches server `app.api-key` / mobile `TAXI_API_KEY`. | Protects anonymous abuse of public endpoints; not per-user. |
| User authentication | **Opaque device token** in `Authorization: Bearer`. | Raw token shown once from `POST /api/users/init`; DB stores **SHA-256** of token only. |
| Identity | Resolved server-side from `device_tokens` → `user_id`. | **Deprecated:** trusting `X-User-Id` from the client (removed from the API path). |

### Threat model (brief)

- **Honest but curious client:** cannot mint another user’s token without the server-generated secret.
- **API key leak:** an attacker can call init and flood data; rate limiting and key rotation are follow-ups.
- **Token theft on device:** equivalent to session cookie theft; mitigations are OS-level (screen lock, remote wipe) and optional future token revocation.

## Roadmap

- Short-lived access tokens + refresh tokens or push-based rotation.
- Optional **token revocation** list and `last_used_at` hygiene for stolen-device scenarios.
- Stricter **CORS** and **rate limiting** on `/api/users/init` if exposed on the public internet.

## Limitations

See [known-limitations.md](known-limitations.md) for operational caveats (single API key, no OAuth, etc.).
