-- Optional client idempotency for POST /api/orders: duplicate Idempotency-Key returns the same row (retry-safe).
ALTER TABLE orders
    ADD COLUMN idempotency_key_hash VARCHAR(64) NULL;

CREATE UNIQUE INDEX ux_orders_user_idempotency
    ON orders (user_id, idempotency_key_hash)
    WHERE idempotency_key_hash IS NOT NULL;
