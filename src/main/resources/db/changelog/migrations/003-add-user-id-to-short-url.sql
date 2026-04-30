-- liquibase formatted sql

-- changeset author:003-add-user-id-to-short-url
ALTER TABLE short_url
    ADD COLUMN user_id BIGINT,
    ADD CONSTRAINT fk_short_url_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE;

-- rollback ALTER TABLE short_url DROP CONSTRAINT fk_short_url_user; ALTER TABLE short_url DROP COLUMN user_id;