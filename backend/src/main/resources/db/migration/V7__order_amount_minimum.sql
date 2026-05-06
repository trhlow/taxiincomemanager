-- Application requires order_amount >= 1 (CreateOrderRequest @Min(1), OrderCalculationService).
-- V1 added an inline CHECK (order_amount >= 0); PostgreSQL names that constraint inconsistently,
-- so we locate it via pg_get_constraintdef instead of hard-coding orders_order_amount_check.
-- Does NOT drop chk_orders_order_amount_max (V3): its definition uses "<=", not ">= 0".
-- Fails if any existing row has order_amount = 0 when ADD CONSTRAINT runs; fix data before migrate.

DO $$
DECLARE
  r RECORD;
BEGIN
  FOR r IN (
      SELECT c.conname
      FROM pg_constraint c
               JOIN pg_class t ON c.conrelid = t.oid
               JOIN pg_namespace n ON n.oid = t.relnamespace
      WHERE n.nspname = 'public'
        AND t.relname = 'orders'
        AND c.contype = 'c'
        AND pg_get_constraintdef(c.oid) ~ 'order_amount\s*>=\s*0'
  )
      LOOP
          EXECUTE format('ALTER TABLE orders DROP CONSTRAINT %I', r.conname);
      END LOOP;
END;
$$;

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_order_amount_min
        CHECK (order_amount >= 1);
