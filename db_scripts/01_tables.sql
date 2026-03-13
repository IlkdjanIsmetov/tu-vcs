-- ПРИМЕРНА ТАБЛИЦА (ЗА СЕГА ЩЕ Я ЗАКОМЕНТИРАМ
-- CREATE TABLE IF NOT EXISTS commit_record
-- (
--     id                  SERIAL PRIMARY KEY,
--     repository_name     VARCHAR(100) NOT NULL,
--     commit_hash         VARCHAR(40)  NOT NULL,
--     author              VARCHAR(50)  NOT NULL,
--     vulnerability_found BOOLEAN DEFAULT FALSE
-- );
--
-- INSERT INTO commit_record (repository_name, commit_hash, author, vulnerability_found)
-- VALUES ('core-auth', 'a1b2c3d4e5f6', 'djani', FALSE),
--        ('api-gateway', 'f9e8d7c6b5a4', 'admin', TRUE),
--        ('tu-vcs-backend', '1a2b3c4d5e6f', 'djani', FALSE);


CREATE TABLE app_user
(
    id         UUID PRIMARY KEY,
    username   VARCHAR(255) UNIQUE NOT NULL,
    email      VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);