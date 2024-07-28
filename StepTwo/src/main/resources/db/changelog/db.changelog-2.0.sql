--liquibase formatted sql

--changeset oldboy:1
INSERT INTO coworking.users (login, user_pass, role)
VALUES ('Admin', '1234', 'ADMIN'),
       ('User', '1234', 'USER'),
       ('UserTwo', '1234', 'USER');
--rollback delete from coworking.users

--changeset oldboy:2
INSERT INTO coworking.places (place_id, species, place_number)
VALUES (1, 'HALL', 1),
       (2, 'HALL', 2),
       (3, 'HALL', 3),
       (4, 'WORKPLACE', 1),
       (5, 'WORKPLACE', 2),
       (6, 'WORKPLACE', 3),
       (7, 'WORKPLACE', 4),
       (8, 'WORKPLACE', 5),
       (9, 'WORKPLACE', 6);

--rollback delete from coworking.places

--changeset oldboy:3
SELECT SETVAL('coworking.places_place_id_seq', (SELECT MAX(place_id) FROM coworking.places));
/* liquibase rollback
empty
*/

--changeset oldboy:4
INSERT INTO coworking.slots (slot_id, slot_number, time_start, time_finish)
VALUES (1, 10, '10:00', '11:00'),
       (2, 11, '11:00', '12:00'),
       (3, 12, '12:00', '13:00'),
       (4, 13, '13:00', '14:00'),
       (5, 14, '14:00', '15:00'),
       (6, 15, '15:00', '16:00'),
       (7, 16, '16:00', '17:00'),
       (8, 17, '17:00', '18:00'),
       (9, 18, '18:00', '19:00');
--rollback delete from coworking.slots

--changeset oldboy:5
SELECT SETVAL('coworking.slots_slot_id_seq', (SELECT MAX(slot_id) FROM coworking.slots));
/* liquibase rollback
empty
*/

--changeset oldboy:6
INSERT INTO coworking.all_reserves (reserve_id, reserve_date, user_id, place_id, slot_id)
VALUES (1, '2029-07-28', 1, 1, 1),
       (2, '2029-07-28', 1, 2, 1),
       (3, '2029-07-28', 1, 4, 3),
       (4, '2029-07-28', 2, 5, 6),
       (5, '2029-07-29', 2, 5, 6),
       (6, '2029-07-29', 3, 1, 6),
       (7, '2029-07-29', 3, 2, 6),
       (8, '2029-07-29', 3, 9, 6);
-- rollback delete from coworking.all_reserves

--changeset oldboy:7
SELECT SETVAL('coworking.all_reserves_reserve_id_seq', (SELECT MAX(reserve_id) FROM coworking.all_reserves));
/* liquibase rollback
empty
*/