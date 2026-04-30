-- liquibase formatted sql

-- changeset author:004-add-click-count-to-short-url
ALTER TABLE short_url
    ADD COLUMN click_count BIGINT NOT NULL DEFAULT 0;

-- rollback ALTER TABLE short_url DROP COLUMN click_count;