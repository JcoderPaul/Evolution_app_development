--liquibase formatted sql

--changeset oldboy:1
CREATE SCHEMA IF NOT EXISTS coworking;
--rollback drop schema coworking