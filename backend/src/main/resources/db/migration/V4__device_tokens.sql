CREATE TABLE device_tokens (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  token_hash VARCHAR(128) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  last_used_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_device_tokens_hash ON device_tokens (token_hash);
CREATE INDEX idx_device_tokens_user ON device_tokens (user_id);
