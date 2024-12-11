--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS coworking.audit_cw
(
    aud_id BIGSERIAL PRIMARY KEY ,
    creator VARCHAR(64) NOT NULL ,
    aud_result VARCHAR(32) NOT NULL ,
    aud_operation VARCHAR(32) NOT NULL ,
    aud_entity VARCHAR(256) NOT NULL ,
    aud_time TIMESTAMP NOT NULL
);
--rollback drop table coworking.audit_cw