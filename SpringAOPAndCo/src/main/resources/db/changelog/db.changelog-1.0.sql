--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS coworking.users
(
    user_id BIGSERIAL PRIMARY KEY ,
    login VARCHAR(64) NOT NULL UNIQUE ,
    user_pass VARCHAR(128) NOT NULL ,
    role VARCHAR(32) NOT NULL
);
--rollback drop table coworking.users