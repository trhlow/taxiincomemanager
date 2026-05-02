ALTER TABLE device_tokens ADD COLUMN expires_at TIMESTAMPTZ;
ALTER TABLE device_tokens ADD COLUMN revoked_at TIMESTAMPTZ;

UPDATE device_tokens
SET expires_at = created_at + INTERVAL '90 days'
WHERE expires_at IS NULL;

ALTER TABLE device_tokens ALTER COLUMN expires_at SET NOT NULL;

CREATE INDEX idx_device_tokens_expires_at ON device_tokens (expires_at);
CREATE INDEX idx_device_tokens_revoked_at ON device_tokens (revoked_at);
