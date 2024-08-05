package me.oldboy.cwapp.core.repository;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.core.entity.Place;
import me.oldboy.cwapp.core.entity.Reservation;
import me.oldboy.cwapp.core.entity.Slot;
import me.oldboy.cwapp.core.entity.User;
import me.oldboy.cwapp.core.repository.crud.PlaceRepository;
import me.oldboy.cwapp.core.repository.crud.ReservationRepository;
import me.oldboy.cwapp.core.repository.crud.SlotRepository;
import me.oldboy.cwapp.core.repository.crud.UserRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ReserveRepositoryImp implements ReservationRepository {

    private final Connection connection;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;

    /*
    SQL запросы на - создание, обновление и удаление
    SQL queries for - create, update and delete
    */
    private static final String CREATE_RESERVE_SQL = """
            INSERT INTO coworking.all_reserves (reserve_date, user_id, place_id, slot_id)
            VALUES (?, ?, ?, ?);
            """;
    private static final String UPDATE_RESERVE_SQL = """
            UPDATE coworking.all_reserves
            SET reserve_date = ?,
                user_id = ?,
                place_id = ?,
                slot_id = ?
            WHERE reserve_id = ?
            """;
    private static final String DELETE_RESERVE_BY_ID_SQL = """
            DELETE FROM coworking.all_reserves
            WHERE reserve_id = ?
            """;

    /*
    SQL запросы на - чтение общее, всего сразу без фильтра
    SQL queries for - general reading, all at once without a filter
    */
    private static final String FIND_ALL_RESERVE_SQL = """
            SELECT reserve_id,
                   reserve_date,
                   user_id,
                   place_id,
                   slot_id
            FROM coworking.all_reserves
            """;
    /*
    SQL зависимые запросы на - чтение с определенным фильтром
    SQL dependent queries on - reading with a specific filter

    Находим бронь по ID, дополняя запрос "найти все"
    Find reservation by ID, supplementing the “find all” query
    */
    private static final String FIND_RESERVE_BY_ID_SQL = FIND_ALL_RESERVE_SQL + """
            WHERE reserve_id = ?
            """;
    /*
    Находим все брони по дате, дополняя запрос "найти все"
    Find all reservations by Date, supplementing the “find all” query
    */
    private static final String FIND_RESERVE_BY_DATE_SQL = FIND_ALL_RESERVE_SQL + """
            WHERE reserve_date = ?
            """;
    /*
    Находим все брони по месту, дополняя запрос "найти все"
    Find all reservations by Places, supplementing the “find all” query
    */
    private static final String FIND_RESERVE_BY_PLACE_SQL = FIND_ALL_RESERVE_SQL + """
            WHERE place_id = ?
            """;
    /*
    Находим все брони по слоту, дополняя запрос "найти все"
    Find all reservations by Slot Id, supplementing the “find all” query
    */
    private static final String FIND_RESERVE_BY_SLOT_SQL = FIND_ALL_RESERVE_SQL + """
            WHERE slot_id = ?
            """;
    /*
    Находим все брони по ID пользователя, дополняя запрос "найти все"
    Find all reservations by user Id, supplementing the “find all” query
    */
    private static final String FIND_RESERVE_BY_USER_ID_SQL = FIND_ALL_RESERVE_SQL + """
            WHERE user_id = ?
            """;
    /*
    Находим бронь по дате, месту и слоту дополняя запрос "найти все"
    Find reservation by Date, Place and Slot, supplementing the “find all” query
    */
    private static final String FIND_RESERVE_BY_DATE_PLACE_SLOT_SQL = FIND_ALL_RESERVE_SQL + """
            WHERE reserve_date = ? AND place_id = ? AND slot_id = ?  
            """;

    @Override
    public Optional<Reservation> createReservation(Reservation newReservation) {
        Reservation newReserve = null;
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(CREATE_RESERVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setDate(1, Date.valueOf(newReservation.getReserveDate()));
            preparedStatement.setLong(2, newReservation.getUser().getUserId());
            preparedStatement.setLong(3, newReservation.getPlace().getPlaceId());
            preparedStatement.setLong(4, newReservation.getSlot().getSlotId());
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                newReserve = reserveBuilder(generatedKeys);
            }
            connection.commit();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(newReserve);
    }

    @Override
    public Optional<Reservation> findReservationById(Long reserveId) {
        Reservation findReserve = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_RESERVE_BY_ID_SQL)) {
            preparedStatement.setLong(1, reserveId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                findReserve = reserveBuilder(resultSet);
            }
            connection.commit();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(findReserve);
    }

    @Override
    public Optional<List<Reservation>> findAllReservation() {
        List<Reservation> newAllReserve = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_RESERVE_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                newAllReserve.add(reserveBuilder(resultSet));
            }
            connection.commit();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(newAllReserve);
    }

    @Override
    public Optional<List<Reservation>> findReservationByDate(LocalDate date) {
        List<Reservation> newAllReserve = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_RESERVE_BY_DATE_SQL)) {
            preparedStatement.setDate(1, Date.valueOf(date));
            ResultSet resultSet = preparedStatement.executeQuery();
            /*
            В отличие от метода *.findReservationById(), где результатом может быть только одна запись в БД,
            тут мы ищем брони по дате, а значит, можем получить серьезный список таковых и поэтому применяется
            цикл 'while', а не проверка на наличие следующей записи 'if', как в *.findReservationById()
            */
            while (resultSet.next()) {
                newAllReserve.add(reserveBuilder(resultSet));
            }
            connection.commit();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(newAllReserve);
    }

    @Override
    public Optional<List<Reservation>> findReservationByPlaceId(Long placeId) {
        List<Reservation> newAllReserve = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_RESERVE_BY_PLACE_SQL)) {
            preparedStatement.setLong(1, placeId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                newAllReserve.add(reserveBuilder(resultSet));
            }
            connection.commit();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(newAllReserve);
    }

    @Override
    public Optional<List<Reservation>> findReservationBySlotId(Long slotId) {
        List<Reservation> newAllReserve = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_RESERVE_BY_SLOT_SQL)) {
            preparedStatement.setLong(1, slotId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                newAllReserve.add(reserveBuilder(resultSet));
            }
            connection.commit();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(newAllReserve);
    }

    @Override
    public Optional<Reservation> findReservationByDatePlaceAndSlot(LocalDate date,
                                                                   Long placeId,
                                                                   Long slotId) {
        Reservation findReserve = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_RESERVE_BY_DATE_PLACE_SLOT_SQL)) {
            preparedStatement.setDate(1, Date.valueOf(date));
            preparedStatement.setLong(2, placeId);
            preparedStatement.setLong(3, slotId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                findReserve = reserveBuilder(resultSet);
            }
            connection.commit();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(findReserve);
    }

    @Override
    public Optional<List<Reservation>> findReservationByUserId(Long userId) {
        List<Reservation> findReserve = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_RESERVE_BY_USER_ID_SQL)) {
            preparedStatement.setLong(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                findReserve.add(reserveBuilder(resultSet));
            }
            connection.commit();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(findReserve);
    }

    @Override
    public boolean updateReservation(Reservation reservation){
        Boolean isReservationUpdated = false;
        try(PreparedStatement prepareStatement = connection.prepareStatement(UPDATE_RESERVE_SQL)) {
            prepareStatement.setDate(1, Date.valueOf(reservation.getReserveDate()));
            prepareStatement.setLong(2, reservation.getUser().getUserId());
            prepareStatement.setLong(3, reservation.getPlace().getPlaceId());
            prepareStatement.setLong(4, reservation.getSlot().getSlotId());
            prepareStatement.setLong(5, reservation.getReserveId());
            isReservationUpdated = prepareStatement.executeUpdate() > 0;
            connection.commit();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return isReservationUpdated;
    }

    @Override
    public boolean deleteReservation(Long reserve_id) {
        Boolean isReservationDeleted = false;
        try(PreparedStatement prepareStatement = connection.prepareStatement(DELETE_RESERVE_BY_ID_SQL)) {
            prepareStatement.setLong(1, reserve_id);
            isReservationDeleted = prepareStatement.executeUpdate() > 0;
            connection.commit();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return isReservationDeleted;
    }

    private Reservation reserveBuilder(ResultSet resultSet) throws SQLException {
        User findUser = userRepository.findUserById(resultSet.getLong("user_id")).get();
        Slot findSlot = slotRepository.findSlotById(resultSet.getLong("slot_id")).get();
        Place findPlace = placeRepository.findPlaceById(resultSet.getLong("place_id")).get();
        return new Reservation(
                resultSet.getLong("reserve_id"),
                resultSet.getDate("reserve_date").toLocalDate(),
                findUser,
                findPlace,
                findSlot
        );
    }
}
