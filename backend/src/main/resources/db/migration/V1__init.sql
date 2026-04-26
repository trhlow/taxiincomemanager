CREATE TABLE users (
  id            UUID PRIMARY KEY,
  display_name  VARCHAR(100) NOT NULL,
  name_locked   BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE orders (
  id            UUID PRIMARY KEY,
  user_id       UUID         NOT NULL REFERENCES users(id),
  order_amount  BIGINT       NOT NULL CHECK (order_amount >= 0),
  fee_rate      NUMERIC(4,3) NOT NULL,
  fee_amount    BIGINT       NOT NULL CHECK (fee_amount >= 0),
  tip_amount    BIGINT       NOT NULL DEFAULT 0 CHECK (tip_amount >= 0),
  taxi_count    SMALLINT     NOT NULL DEFAULT 1 CHECK (taxi_count IN (1, 2)),
  subtotal      BIGINT       NOT NULL,
  net_amount    BIGINT       NOT NULL,
  order_date    DATE         NOT NULL,
  order_time    TIME         NOT NULL,
  source_type   VARCHAR(16)  NOT NULL,
  note          TEXT,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_orders_user_date ON orders(user_id, order_date);

CREATE TABLE work_schedules (
  id          UUID PRIMARY KEY,
  user_id     UUID         NOT NULL REFERENCES users(id),
  work_date   DATE         NOT NULL,
  shift_type  VARCHAR(16)  NOT NULL,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  UNIQUE (user_id, work_date, shift_type)
);

CREATE TABLE pdf_imports (
  id              UUID PRIMARY KEY,
  user_id         UUID         NOT NULL REFERENCES users(id),
  file_name       VARCHAR(255) NOT NULL,
  status          VARCHAR(32)  NOT NULL,
  extracted_text  TEXT,
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
