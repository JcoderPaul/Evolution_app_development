package me.oldboy.cwapp.input.repository;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.input.entity.Place;
import me.oldboy.cwapp.input.entity.Species;
import me.oldboy.cwapp.input.repository.crud.PlaceRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class PlaceRepositoryImpl implements PlaceRepository {

    private final Connection connection;

    /* SQL запросы на - создание, обновление и удаление / SQL queries for - create, update and delete */
    private static final String CREATE_PLACE_SQL = """
            INSERT INTO coworking.places (species, place_number)
            VALUES (?, ?);
            """;

    private static final String UPDATE_PLACE_SQL = """
            UPDATE coworking.places
            SET species = ?,
                place_number = ?
            WHERE place_id = ?
            """;

    private static final String DELETE_PLACE_BY_ID_SQL = """
            DELETE FROM coworking.places
            WHERE place_id = ?
            """;

    /* SQL запросы на - чтение общее, всего сразу без фильтра / SQL queries for - general reading, all at once without a filter */
    private static final String FIND_ALL_PLACES_SQL = """
            SELECT *
            FROM coworking.places
            """;
    /*
    SQL зависимые запросы на - чтение с определенным фильтром
    SQL dependent queries on - reading with a specific filter

    Находим место (зал/рабочее место) по ID, дополняя запрос "найти все"
    Find place (hall/workplace) by ID, supplementing the “find all” query
    */
    private static final String FIND_PLACE_BY_ID_SQL = FIND_ALL_PLACES_SQL + """
            WHERE place_id = ?
            """;
    /*
    Находим все места (залы/рабочие места) по их виду (Hall/Workplace), дополняя запрос "найти все"
    Find all places (halls/workplaces) by their type (Hall/Workplace), supplementing the “find all” query
    */
    private static final String FIND_ALL_PLACES_BY_SPECIES_SQL = FIND_ALL_PLACES_SQL + """
            WHERE species = ?
            """;
    /*
    Находим место (зал/рабочее месте) по его виду (Hall/Workplace) и номеру, дополняя запрос "найти все"
    Find place (hall/workplace) by type (Hall/Workplace) and number, supplementing the “find all” query
    */
    private static final String FIND_PLACE_BY_SPECIES_AND_NUMBER_SQL = FIND_ALL_PLACES_SQL + """
            WHERE species = ? AND place_number = ?
            """;

    @Override
    public Optional<Place> createPlace(Place place) {
        Place placeToBase = null;
        /*
        Для нас важно получить сгенерированный базой ID записи, поэтому мы передаем константу,
        которая указывает, что сгенерированные ключи должны быть доступны для извлечения. Акцент
        на данной особенности текущего PreparedStatement сделан именно потому, что в других методах
        нам не понадобится возвращать/получать сгенерированный базой данных ID
        */
        try(PreparedStatement prepareStatement =
                    connection.prepareStatement(CREATE_PLACE_SQL, Statement.RETURN_GENERATED_KEYS))
        {
            prepareStatement.setString(1, place.getSpecies().name());
            prepareStatement.setInt(2, place.getPlaceNumber());
            /*
            Выполняет оператор SQL для переданного объекта Statement, который должен быть оператором SQL
            языка манипулирования данными (DML), например: INSERT, UPDATE или DELETE; или оператор SQL,
            который ничего не возвращает, например оператор DDL.
            */
            prepareStatement.executeUpdate();
            ResultSet generatedAutoId = prepareStatement.getGeneratedKeys();
            if(generatedAutoId.next())
            {
                long id = generatedAutoId.getLong("place_id");
                placeToBase = new Place(id, place.getSpecies(), place.getPlaceNumber());
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(placeToBase);
    }

    @Override
    public Optional<Place> findPlaceById(Long placeId){
        Place mayBePlace = null;
        try(PreparedStatement preparedStatement = connection.prepareStatement(FIND_PLACE_BY_ID_SQL)) {
            preparedStatement.setLong(1, placeId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                mayBePlace = buildPlace(resultSet);
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(mayBePlace);
    }

    @Override
    public List<Place> findAllPlaces(){
        List<Place> listPlace = new ArrayList<>();
        try(PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_PLACES_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                listPlace.add(buildPlace(resultSet));
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return listPlace;
    }

    @Override
    public boolean updatePlace(Place place) {
        Boolean isUpdateCorrect = false;
        try(PreparedStatement prepareStatement = connection.prepareStatement(UPDATE_PLACE_SQL)) {
            prepareStatement.setString(1, place.getSpecies().name());
            prepareStatement.setInt(2, place.getPlaceNumber());
            prepareStatement.setLong(3, place.getPlaceId());
            isUpdateCorrect = prepareStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return isUpdateCorrect;
    }

    @Override
    public boolean deletePlace(Long placeId) {
        Boolean isDeleteCorrect = false;
        try(PreparedStatement prepareStatement = connection.prepareStatement(DELETE_PLACE_BY_ID_SQL)) {
            prepareStatement.setLong(1, placeId);
            isDeleteCorrect = prepareStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return isDeleteCorrect;
    }

    @Override
    public Optional<Place> findPlaceBySpeciesAndNumber(Species species, Integer placeNumber){
        Place mayBePlace = null;
        try(PreparedStatement preparedStatement = connection.prepareStatement(FIND_PLACE_BY_SPECIES_AND_NUMBER_SQL)) {
            preparedStatement.setString(1, species.name());
            preparedStatement.setInt(2, placeNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                mayBePlace = buildPlace(resultSet);
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(mayBePlace);
    }

    @Override
    public Optional<List<Place>> findAllPlacesBySpecies(Species species){
        List<Place> mayBeListPlace = new ArrayList<>();
        try(PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_PLACES_BY_SPECIES_SQL)) {
            preparedStatement.setString(1, species.name());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                mayBeListPlace.add(buildPlace(resultSet));
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return Optional.ofNullable(mayBeListPlace);
    }

    private Place buildPlace(ResultSet resultSet) throws SQLException {
        return new Place(
                resultSet.getLong("place_id"),
                Species.valueOf(resultSet.getString("species")),
                resultSet.getInt("place_number")
        );
    }
}
