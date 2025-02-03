--liquibase formatted sql

--changeset oldboy:1
INSERT INTO coworking.users (user_id, login, user_pass, role)
VALUES (1, 'Admin', '1234', 'ADMIN'),
       (2, 'User', '1234', 'USER'),
       (3, 'UserThree', '4321', 'USER'),
       (4, 'UserTwo', '1234', 'USER');
--rollback delete from coworking.users

--changeset oldboy:2
SELECT SETVAL('coworking.users_user_id_seq', (SELECT MAX(user_id) FROM coworking.users));
/* liquibase rollback
empty
*/

--changeset oldboy:3
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

--changeset oldboy:4
SELECT SETVAL('coworking.places_place_id_seq', (SELECT MAX(place_id) FROM coworking.places));
/* liquibase rollback
empty
*/

--changeset oldboy:5
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

--changeset oldboy:6
SELECT SETVAL('coworking.slots_slot_id_seq', (SELECT MAX(slot_id) FROM coworking.slots));
/* liquibase rollback
empty
*/

--changeset oldboy:7
INSERT INTO coworking.all_reserves (reserve_id, reserve_date, user_id, place_id, slot_id)
VALUES (1, '2029-07-28', (SELECT user_id FROM coworking.users WHERE user_id = 1), (SELECT place_id FROM coworking.places WHERE place_id = 1), (SELECT slot_id FROM coworking.slots WHERE slot_id = 1)),
       (2, '2029-07-28', (SELECT user_id FROM coworking.users WHERE user_id = 1), (SELECT place_id FROM coworking.places WHERE place_id = 2), (SELECT slot_id FROM coworking.slots WHERE slot_id = 1)),
       (3, '2029-07-28', (SELECT user_id FROM coworking.users WHERE user_id = 1), (SELECT place_id FROM coworking.places WHERE place_id = 1), (SELECT slot_id FROM coworking.slots WHERE slot_id = 3)),
       (4, '2029-07-28', (SELECT user_id FROM coworking.users WHERE user_id = 2), (SELECT place_id FROM coworking.places WHERE place_id = 5), (SELECT slot_id FROM coworking.slots WHERE slot_id = 6)),
       (5, '2029-07-29', (SELECT user_id FROM coworking.users WHERE user_id = 2), (SELECT place_id FROM coworking.places WHERE place_id = 5), (SELECT slot_id FROM coworking.slots WHERE slot_id = 6)),
       (6, '2029-07-29', (SELECT user_id FROM coworking.users WHERE user_id = 3), (SELECT place_id FROM coworking.places WHERE place_id = 1), (SELECT slot_id FROM coworking.slots WHERE slot_id = 6)),
       (7, '2029-07-29', (SELECT user_id FROM coworking.users WHERE user_id = 3), (SELECT place_id FROM coworking.places WHERE place_id = 2), (SELECT slot_id FROM coworking.slots WHERE slot_id = 6)),
       (8, '2029-07-29', (SELECT user_id FROM coworking.users WHERE user_id = 3), (SELECT place_id FROM coworking.places WHERE place_id = 9), (SELECT slot_id FROM coworking.slots WHERE slot_id = 6));
-- rollback delete from coworking.all_reserves

--changeset oldboy:8
SELECT SETVAL('coworking.all_reserves_reserve_id_seq', (SELECT MAX(reserve_id) FROM coworking.all_reserves));
/* liquibase rollback
empty
*/

--changeset oldboy:9
INSERT INTO coworking.audit_cw (aud_id, creator, aud_result, aud_operation, aud_entity, aud_time)
VALUES (1, 'nameOf', 'SUCCESS', 'CREATE_RESERVATION', 'ReservationCreate {reservationDate: 2033-08-11, userId = 3, placeId = 8, slotId = 1}', '2024-11-17T16:28:29'),
       (2, 'nameOf', 'SUCCESS', 'DELETE_RESERVATION', 'ReservationUpdateDeleteDto {reservationId: 10, reservationDate = 2027-06-12, userId = 3, placeId = 8, slotId = 4}', '2025-05-08T13:29:38'),
       (3, 'nameOf', 'FAIL', 'CREATE_PLACE', 'PlaceCreateDeleteDto { species = HALL, placeNumber = 6}',' 2027-12-03T10:15:30');
-- rollback delete from coworking.audit_cw

--changeset oldboy:10
SELECT SETVAL('coworking.audit_cw_aud_id_seq', (SELECT MAX(aud_id) FROM coworking.audit_cw));
/* liquibase rollback
empty
*/