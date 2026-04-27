-- Align DB rules with application validation (defense in depth).
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_order_amount_max
        CHECK (order_amount <= 100000000);

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_tip_amount_max
        CHECK (tip_amount <= 20000000);

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_fee_rate_range
        CHECK (fee_rate >= 0 AND fee_rate <= 1);

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_subtotal_non_negative
        CHECK (subtotal >= 0);

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_net_amount_non_negative
        CHECK (net_amount >= 0);
