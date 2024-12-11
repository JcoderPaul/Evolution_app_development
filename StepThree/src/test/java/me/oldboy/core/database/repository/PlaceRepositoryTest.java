package me.oldboy.core.database.repository;

import me.oldboy.config.connection.TestsConnectionManager;
import me.oldboy.config.liquibase.LiquibaseManager;
import me.oldboy.config.util.TestsHibernateUtil;
import me.oldboy.core.model.database.entity.Place;
import me.oldboy.core.model.database.entity.options.Species;
import me.oldboy.core.model.database.repository.PlaceRepository;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Testcontainers
class PlaceRepositoryTest {

    private PlaceRepository placeRepository; // Тестируем методы данного класса
    private static SessionFactory sessionFactory;  // Связь Hibernate с БД
    private static Connection connection;  // Связь с БД (тестовой)
    private static LiquibaseManager liquibaseManager; // Подключаем стороннее управление БД
    private Place notExistPlace;
    private Place existPlace;

    /* Делаем тестовый контейнер */

    @Container
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    /* Настраиваем состояние контейнера и БД, до и после каждого / всех тестов */

    @BeforeAll
    public static void startTestContainer() {
        liquibaseManager = LiquibaseManager.getInstance();
        postgresContainer.start();
        connection = TestsConnectionManager.getTestBaseConnection(postgresContainer.getJdbcUrl(),
                                                             postgresContainer.getUsername(),
                                                             postgresContainer.getPassword()
        );
        liquibaseManager.migrationsStart(connection);

        sessionFactory = TestsHibernateUtil.buildSessionFactory(postgresContainer);
    }

    @BeforeEach
    public void getConnectionToTestBaseAndInitIt(){
        liquibaseManager.migrationsStart(connection);

        placeRepository = new PlaceRepository(sessionFactory);
        placeRepository.getEntityManager().getTransaction().begin();

        existPlace = Place.builder()
                .placeId(2L)
                .species(Species.HALL)
                .placeNumber(2)
                .build();

        notExistPlace = Place.builder()
                .species(Species.HALL)
                .placeNumber(4)
                .build();
    }

    @AfterEach
    public void resetTestBase(){
        placeRepository.getEntityManager().getTransaction().commit();
        /*
        rollbackDepth = 10 - очистка таблиц с их сохранением
        rollbackDepth = 16 - полная очистка базы, удаление таблиц
        */
        liquibaseManager.rollbackDB(connection, 10);

    }

    @AfterAll
    public static void stopTestContainer(){
        sessionFactory.close();
        postgresContainer.stop();
    }

    /* Блок тестов */

    @Test
    @DisplayName("1 - create Place - Should return generated place ID")
    void shouldReturnCreatedPlace_createTest() {
        Long generateId = placeRepository.create(notExistPlace).getPlaceId();
        assertThat(generateId).isNotNull();
    }

    @Test
    @DisplayName("2 - findById Place - Should return existent place")
    void shouldReturnOptionalPlace_findByIdPlaceTest() {
        Long existingPlaceId = existPlace.getPlaceId();
        Optional<Place> mayBePlace = placeRepository.findById(existingPlaceId);

        assertThat(mayBePlace.isPresent()).isTrue();
        assertAll(
                () -> assertThat(mayBePlace.get().getPlaceId()).isEqualTo(existingPlaceId),
                () -> assertThat(mayBePlace.get().getPlaceNumber()).isEqualTo(existPlace.getPlaceNumber()),
                () -> assertThat(mayBePlace.get().getSpecies()).isEqualTo(existPlace.getSpecies())
        );
    }

    @Test
    @DisplayName("3 - findById Place - Should return empty Optional")
    void shouldReturnOptionalEmpty_findByIdNonExistentPlaceTest() {
        Long nonExistentPlaceId = 25L;
        Optional<Place> mayBePlace = placeRepository.findById(nonExistentPlaceId);

        assertThat(mayBePlace).isEmpty();
    }

    @Test
    @DisplayName("4 - update Place - Should return update place")
    void shouldReturnEqualsTrue_updateExistPlaceTest() {
        Long PlaceId = existPlace.getPlaceId();
        Integer PlaceNumber = 15;
        existPlace.setPlaceNumber(PlaceNumber);
        Species changeSpecies = Species.WORKPLACE;
        existPlace.setSpecies(changeSpecies);

        placeRepository.update(existPlace);
        Place isUpdatePlace = placeRepository.findById(PlaceId).get();

        assertAll(
                () -> assertThat(isUpdatePlace.getPlaceNumber()).isEqualTo(existPlace.getPlaceNumber()),
                () -> assertThat(isUpdatePlace.getPlaceId()).isEqualTo(existPlace.getPlaceId()),
                () -> assertThat(isUpdatePlace.getSpecies()).isEqualTo(existPlace.getSpecies())
        );
    }

    @Test
    @DisplayName("5 - delete Place - Should return true if place is deleted")
    void shouldReturnTrue_deletePlaceTest() {
        Long existPlaceId = existPlace.getPlaceId();
        placeRepository.delete(existPlaceId);

        assertThat(placeRepository.findById(existPlaceId)).isEmpty();
    }

    @Test
    @DisplayName("6 - findAll Places - Should return size of places list")
    void shouldReturnListOfPlaces_findAllPlaceTest() {
        Integer listSize = placeRepository.findAll().size();
        assertThat(listSize).isEqualTo(9);
    }

    @Test
    @DisplayName("7 - findPlaceBySpeciesAndNumber - Should return true to find existent place")
    void shouldReturnPlace_findPlaceBySpeciesAndNumberTest() {
        Optional<Place> mayBePlace=
                placeRepository.findPlaceBySpeciesAndNumber(existPlace.getSpecies(), existPlace.getPlaceNumber());
        assertThat(mayBePlace).isPresent();
    }

    @Test
    @DisplayName("8 - findNoPlaceBySpeciesAndNumber - Should return false to find non existent place")
    void shouldReturnOptionalEmpty_findPlaceBySpeciesAndNumberTest() {
        Optional<Place> mayBePlace=
                placeRepository.findPlaceBySpeciesAndNumber(Species.HALL, 40);
        assertThat(mayBePlace).isEmpty();
    }

    @Test
    @DisplayName("9 - findAllPlacesBySpecies - Should return list size of places by concrete species")
    void shouldReturnListSize_findAllPlacesBySpeciesTest() {
        Optional<List<Place>> mayBePlaceList = placeRepository.findAllPlacesBySpecies(Species.HALL);
        assertThat(mayBePlaceList).isPresent();
        assertThat(mayBePlaceList.get().size()).isEqualTo(3);
    }
}