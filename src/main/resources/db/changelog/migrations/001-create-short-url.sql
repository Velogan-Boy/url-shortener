-- liquibase formatted sql

-- changeset author:001-create-short-url
CREATE TABLE short_url (
    id           BIGSERIAL    PRIMARY KEY,
    short_code   VARCHAR(10)  NOT NULL UNIQUE,
    original_url TEXT         NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- rollback DROP TABLE short_url;