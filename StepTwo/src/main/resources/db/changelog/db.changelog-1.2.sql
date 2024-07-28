--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS coworking.slots
(
    slot_id BIGSERIAL PRIMARY KEY ,
    slot_number INTEGER NOT NULL UNIQUE ,
    time_start TIME NOT NULL UNIQUE,
	time_finish TIME NOT NULL UNIQUE CONSTRAINT right_time CHECK (time_finish > time_start),
    UNIQUE (slot_number, time_start, time_finish)
);
--rollback drop table coworking.slots;