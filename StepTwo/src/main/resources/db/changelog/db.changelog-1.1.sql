--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS coworking.places
(
    place_id BIGSERIAL PRIMARY KEY ,
    Species VARCHAR(64) NOT NULL ,
    place_number INTEGER NOT NULL ,
    UNIQUE (species, place_number)
);
--rollback drop table coworking.places;