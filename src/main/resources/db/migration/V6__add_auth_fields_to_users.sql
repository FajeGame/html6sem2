ALTER TABLE users ADD COLUMN password VARCHAR(255);
ALTER TABLE users ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'USER';

-- BCrypt-хеш для временного пароля "password" (существующие записи из предыдущих лаб)
UPDATE users
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE password IS NULL;

ALTER TABLE users ALTER COLUMN password SET NOT NULL;
