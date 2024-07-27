package me.oldboy.cwapp.input.repository;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.exceptions.repositorys.SlotRepositoryException;
import me.oldboy.cwapp.input.entity.Slot;
import me.oldboy.cwapp.input.repository.crud.SlotRepository;

import java.sql.*;
import java.util.*;

@RequiredArgsConstructor
public class SlotRepositoryImpl implements SlotRepository {

    private final Connection connection;

    /* SQL запросы на - создание, обновление и удаление / SQL queries for - create, update and delete */
    private static final String CREATE_SLOT_SQL = """
            INSERT INTO coworking.slots (slot_number, time_start, time_finish)
            VALUES (?, ?, ?);
            """;

    private static final String UPDATE_SLOT_SQL = """
            UPDATE coworking.slots
            SET slot_number = ?,
                time_start = ?,
                time_finish = ?
            WHERE slot_id = ?
            """;

    private static final String DELETE_SLOT_SQL = """
            DELETE FROM coworking.slots
            WHERE slot_id = ?
            """;

    /* SQL запрос на - чтение всего, без фильтра / SQL queries for - all reading, without a filter */
    private static final String FIND_ALL_SLOTS_SQL = """ 
            SELECT slot_id,
                   slot_number,
                   time_start,
                   time_finish
            FROM coworking.slots
            """;

    /* SQL запрос на - получение слота по ID / SQL queries for - read slot by ID */
    private static final String FIND_SLOT_BY_ID_SQL = FIND_ALL_SLOTS_SQL + """
            WHERE slot_id = ?
            """;

    /* SQL запрос на - получение слота по его номеру / SQL queries for - read slot by Number */
    private static final String FIND_SLOT_BY_NUMBER_SQL = FIND_ALL_SLOTS_SQL + """
            WHERE slot_number = ?
            """;

    @Override
    public Optional<Slot> createSlot(Slot slot) {
        /*
        Для нас важно получить сгенерированный базой ID записи, поэтому мы передаем константу,
        которая указывает, что сгенерированные ключи должны быть доступны для извлечения. Акцент
        на данной особенности текущего PreparedStatement сделан именно потому, что в других методах
        нам не понадобится возвращать/получать сгенерированный базой данных ID
        */
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(CREATE_SLOT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, slot.getSlotNumber());
            preparedStatement.setTime(2, Time.valueOf(slot.getTimeStart()));
            preparedStatement.setTime(3, Time.valueOf(slot.getTimeFinish()));
            /*
            Выполняет оператор SQL для переданного объекта Statement, который должен быть оператором SQL
            языка манипулирования данными (DML), например: INSERT, UPDATE или DELETE, или оператором SQL,
            который ничего не возвращает, например оператор DDL.
            */
            preparedStatement.executeUpdate();
            ResultSet generatedAutoId = preparedStatement.getGeneratedKeys();
            Slot mayBeCreateSlot = null;
            if(generatedAutoId.next())
            {
                long id = generatedAutoId.getLong("slot_id");
                mayBeCreateSlot = new Slot(id, slot.getSlotNumber(), slot.getTimeStart(), slot.getTimeFinish());
            }
            return Optional.ofNullable(mayBeCreateSlot);
        } catch (SQLException sqlException) {
            throw new SlotRepositoryException(sqlException);
        }
    }

    @Override
    public List<Slot> findAllSlots() {
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_SLOTS_SQL)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            List<Slot> newAllSlots = new ArrayList<>();
            while (resultSet.next()) {
                newAllSlots.add(buildSlot(resultSet));
            }
            return newAllSlots;
        } catch (SQLException sqlException) {
            throw new SlotRepositoryException(sqlException);
        }
    }

    @Override
    public Optional<Slot> findSlotById(Long slotId){
        try(PreparedStatement preparedStatement = connection.prepareStatement(FIND_SLOT_BY_ID_SQL)) {

            preparedStatement.setLong(1, slotId);

            ResultSet resultSet = preparedStatement.executeQuery();
            Slot mayBeSlot = null;
            if (resultSet.next()) {
                mayBeSlot = buildSlot(resultSet);
            }
            return Optional.ofNullable(mayBeSlot);
        } catch (SQLException sqlException) {
            throw new SlotRepositoryException(sqlException);
        }
    }

    @Override
    public Optional<Slot> findSlotByNumber(Integer slotNumber){
        try(PreparedStatement preparedStatement = connection.prepareStatement(FIND_SLOT_BY_NUMBER_SQL)) {

            preparedStatement.setInt(1, slotNumber);

            ResultSet resultSet = preparedStatement.executeQuery();
            Slot mayBeSlot = null;
            if (resultSet.next()) {
                mayBeSlot = buildSlot(resultSet);
            }
            return Optional.ofNullable(mayBeSlot);
        } catch (SQLException sqlException) {
            throw new SlotRepositoryException(sqlException);
        }
    }

    @Override
    public boolean updateSlot(Slot slot) {
        try(PreparedStatement prepareStatement = connection.prepareStatement(UPDATE_SLOT_SQL)) {

            prepareStatement.setInt(1, slot.getSlotNumber());
            prepareStatement.setTime(2, Time.valueOf(slot.getTimeStart()));
            prepareStatement.setTime(3, Time.valueOf(slot.getTimeFinish()));
            prepareStatement.setLong(4, slot.getSlotId());

            return prepareStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            throw new SlotRepositoryException(sqlException);
        }
    }

    @Override
    public boolean deleteSlot(Long slotId) {
        try(PreparedStatement prepareStatement = connection.prepareStatement(DELETE_SLOT_SQL)) {

            prepareStatement.setLong(1, slotId);

            return prepareStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            throw new SlotRepositoryException(sqlException);
        }
    }

    private Slot buildSlot(ResultSet resultSet) throws SQLException {
        return new Slot(
                resultSet.getLong("slot_id"),
                resultSet.getInt("slot_number"),
                resultSet.getTime("time_start").toLocalTime(),
                resultSet.getTime("time_finish").toLocalTime()
        );
    }
}