CREATE INDEX IF NOT EXISTS idx_orders_user_date_time
ON orders(user_id, order_date, order_time);
