--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS coworking.all_reserves
(
    reserve_id BIGSERIAL PRIMARY KEY ,
    reserve_date DATE NOT NULL CONSTRAINT right_data CHECK (reserve_date >= NOW()) ,
    user_id BIGINT REFERENCES coworking.users (user_id) ,
    place_id BIGINT REFERENCES coworking.places (place_id) ,
    slot_id BIGINT REFERENCES coworking.slots (slot_id) ,
    UNIQUE (reserve_date, user_id, place_id, slot_id)
);
--rollback drop table coworking.all_reserves