-- Detect accidental reuse of an Idempotency-Key with a different create-order payload.
ALTER TABLE orders
    ADD COLUMN idempotency_payload_hash VARCHAR(64) NULL;
