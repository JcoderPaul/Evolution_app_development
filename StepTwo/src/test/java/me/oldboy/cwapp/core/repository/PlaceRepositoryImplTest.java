package me.oldboy.cwapp.core.repository;

import me.oldboy.cwapp.config.connection.ConnectionManager;
import me.oldboy.cwapp.config.liquibase.LiquibaseManager;
import me.oldboy.cwapp.core.entity.Place;
import me.oldboy.cwapp.core.repository.crud.PlaceRepository;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static me.oldboy.cwapp.core.entity.Species.HALL;
import static me.oldboy.cwapp.core.entity.Species.WORKPLACE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/* Аннотация говорит, что тестирование методов класса идет через Docker тест-контейнер */
@Testcontainers
class PlaceRepositoryImplTest {

    private PlaceRepository placeRepository;
    private Connection connection;
    private LiquibaseManager liquibaseManager = LiquibaseManager.getInstance();

    /* Делаем тестовый контейнер */

    @Container
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:latest").withDatabaseName("test_db")
                                                                       .withUsername("test")
                                                                       .withPassword("test");

    /* Настраиваем состояние контейнера и БД, до и после каждого / всех тестов */

    @BeforeAll
    public static void startTestContainer() {
        postgresContainer.start();
    }

    @BeforeEach
    public void getConnectionToTestBaseAndInitIt(){
        connection = ConnectionManager.getTestBaseConnection(postgresContainer.getJdbcUrl(),
                                                             postgresContainer.getUsername(),
                                                             postgresContainer.getPassword()
        );
        liquibaseManager.migrationsStart(connection);
        placeRepository = new PlaceRepositoryImpl(connection);
    }

    @AfterEach
    public void resetTestBase(){
        liquibaseManager.rollbackCreatedTables(connection);
    }

    @AfterAll
    public static void stopTestContainer(){
        postgresContainer.stop();
    }

    /* Основные тесты для реализаций методов PlaceRepository */

    @Test
    @DisplayName("1 - Should return creation optional place")
    void shouldReturnOptionalPlace_createPlaceTest() {
        Place createNewHall = new Place(HALL, 6);
        Optional<Place> mayBeCreate =
                placeRepository.createPlace(createNewHall);

        assertThat(mayBeCreate.isPresent()).isTrue();

        assertAll(
                () -> assertThat(createNewHall.getSpecies())
                        .isEqualTo(mayBeCreate.get().getSpecies()),
                () -> assertThat(createNewHall.getPlaceNumber())
                        .isEqualTo(mayBeCreate.get().getPlaceNumber())
        );
    }

    /* Тесты метода *.findPlaceById() */

    @Test
    @DisplayName("2 - Should return optional place find by ID")
    void shouldReturnOptionalPlaceIfExist_findPlaceByIdTest() {
        Long testId = 1L;
        Optional<Place> findPlace = placeRepository.findPlaceById(testId);

        assertThat(findPlace.isPresent()).isTrue();

        assertAll(
                () -> assertThat(findPlace.get().getPlaceId()).isEqualTo(testId),
                () -> assertThat(findPlace.get().getSpecies()).isEqualTo(HALL),
                () -> assertThat(findPlace.get().getPlaceNumber()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("3 - Should return Optional null/false to try find non existent place by ID")
    void shouldReturnOptionalNull_findNonExistentPlaceByIdTest() {
        Optional<Place> findPlace = placeRepository.findPlaceById(10L);

        assertThat(findPlace.isPresent()).isFalse();
    }

    /* Тест метода *.findAllPlaces() */

    @Test
    @DisplayName("4 - Should return list of place")
    void shouldReturnListOfPlace_findAllPlacesTest() {
        List<Place> placeList = placeRepository.findAllPlaces();

        assertThat(placeList.size()).isEqualTo(9);

        assertAll(
                () -> assertThat(placeList.get(0).getSpecies()).isEqualTo(HALL),
                () -> assertThat(placeList.get(8).getSpecies()).isEqualTo(WORKPLACE)
        );
    }

    /* Тест метода *.updatePlace() */

    @Test
    @DisplayName("5 - Should return true if update place is success")
    void shouldReturnTrueIfUpdateSuccess_updatePlaceTest() {
        Place placeForUpdate = new Place(1L, HALL, 7);
        Boolean isUpdateGood = placeRepository.updatePlace(placeForUpdate);

        assertThat(isUpdateGood).isTrue();
        assertAll(
                () -> assertThat(placeForUpdate.getPlaceId())
                        .isEqualTo(placeRepository.findPlaceById(1L).get().getPlaceId()),
                () -> assertThat(placeForUpdate.getSpecies())
                        .isEqualTo(placeRepository.findPlaceById(1L).get().getSpecies()),
                () -> assertThat(placeForUpdate.getPlaceNumber())
                        .isEqualTo(placeRepository.findPlaceById(1L).get().getPlaceNumber())
        );
    }

    @Test
    @DisplayName("6 - Should return false if update place is not existed")
    void shouldReturnFalseIfUpdateFail_updatePlaceTest() {
        Place placeForUpdate = new Place(10L, HALL, 7);
        Boolean isUpdateGood = placeRepository.updatePlace(placeForUpdate);

        assertThat(isUpdateGood).isFalse();
    }

    /* Тест метода *.deletePlace() */

    @Test
    @DisplayName("7 - Should return true if place is deleted")
    void shouldReturnTrueIfDeleteIsSuccess_deletePlaceTest() {
        Integer sizeOfListBeforeDeletePlace = placeRepository.findAllPlaces().size();
        boolean isDeleteGood = placeRepository.deletePlace(3L);
        Integer sizeOfListAfterDeletePlace = placeRepository.findAllPlaces().size();

        assertThat(isDeleteGood).isTrue();
        assertThat(sizeOfListBeforeDeletePlace).isGreaterThan(sizeOfListAfterDeletePlace);
    }

    @Test
    @DisplayName("8 - Should return false if place is not deleted/non existent")
    void shouldReturnFalseIfDeleteFail_deletePlaceTest() {
        Integer sizeOfListBeforeDeletePlace = placeRepository.findAllPlaces().size();
        boolean isDeleteGood = placeRepository.deletePlace(32L);
        Integer sizeOfListAfterDeletePlace = placeRepository.findAllPlaces().size();

        assertThat(isDeleteGood).isFalse();
        assertThat(sizeOfListBeforeDeletePlace).isEqualTo(sizeOfListAfterDeletePlace);
    }

    /* Тест метода *.findPlaceBySpeciesAndNumber() */

    @Test
    @DisplayName("9 - Should return optional place is find it")
    void shouldReturnOptionalPlaceIfFindPlace_findPlaceBySpeciesAndNumberTest() {
        Optional<Place> findPlace = placeRepository.findPlaceBySpeciesAndNumber(HALL, 1);

        assertThat(findPlace.isPresent()).isTrue();

        assertAll(
                () -> findPlace.get().getSpecies().equals(HALL),
                () -> findPlace.get().getPlaceNumber().equals(1)
        );
    }

    @Test
    @DisplayName("10 - Should return optional null if not find place by species and number")
    void shouldReturnOptionalNullIfPlaceNonExist_findPlaceBySpeciesAndNumberTest() {
        Optional<Place> findPlace = placeRepository.findPlaceBySpeciesAndNumber(HALL, 6);

        assertThat(findPlace.isPresent()).isFalse();
    }

    /* Тест метода *.findPlaceBySpecies() */

    @Test
    @DisplayName("11 - Should return optional place list")
    void shouldReturnOptionalListOfPlaces_findPlaceBySpeciesTest() {
        Optional<List<Place>> findPlace = placeRepository.findAllPlacesBySpecies(HALL);

        assertThat(findPlace.isPresent()).isTrue();
        assertThat(findPlace.get().size()).isEqualTo(3);

        assertAll(
                () -> findPlace.get().get(1).getSpecies().equals(HALL),
                () -> findPlace.get().get(findPlace.get().size() - 1).getSpecies().equals(HALL)
        );
    }
}