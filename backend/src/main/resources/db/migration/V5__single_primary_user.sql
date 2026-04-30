ALTER TABLE users ADD COLUMN singleton_key VARCHAR(32);

WITH ranked_users AS (
  SELECT id, ROW_NUMBER() OVER (ORDER BY created_at ASC, id ASC) AS row_number
  FROM users
)
UPDATE users
SET singleton_key = CASE
  WHEN ranked_users.row_number = 1 THEN 'PRIMARY'
  ELSE 'LEGACY_' || LEFT(users.id::text, 24)
END
FROM ranked_users
WHERE users.id = ranked_users.id;

ALTER TABLE users ALTER COLUMN singleton_key SET NOT NULL;

CREATE UNIQUE INDEX ux_users_singleton_key ON users(singleton_key);
