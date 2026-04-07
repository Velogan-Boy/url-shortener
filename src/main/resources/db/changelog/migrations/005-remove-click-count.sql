-- liquibase formatted sql

-- changeset author:005-remove-click-count
ALTER TABLE short_url DROP COLUMN click_count;

-- rollback ALTER TABLE short_url ADD COLUMN click_count BIGINT NOT NULL DEFAULT 0;