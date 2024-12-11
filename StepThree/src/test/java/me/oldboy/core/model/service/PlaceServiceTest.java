package me.oldboy.core.model.service;

import me.oldboy.config.connection.TestsConnectionManager;
import me.oldboy.config.liquibase.LiquibaseManager;
import me.oldboy.config.util.TestsHibernateUtil;
import me.oldboy.core.dto.places.PlaceCreateDeleteDto;
import me.oldboy.core.dto.places.PlaceReadUpdateDto;
import me.oldboy.exception.PlaceServiceException;
import me.oldboy.core.model.database.entity.Place;
import me.oldboy.core.model.database.entity.options.Species;
import me.oldboy.core.model.database.repository.PlaceRepository;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class PlaceServiceTest {

    private PlaceRepository placeRepository;
    private PlaceService placeService;
    private static SessionFactory sessionFactory;
    private static Connection connection;
    private static LiquibaseManager liquibaseManager;
    private PlaceCreateDeleteDto nonExistentPlaceDto;
    private Place existPlace;

    /*
    Начинаем тесты.

    Повторим материал:
    - создаем тестовый контейнер;
    - передаем в него параметры доступа к тестовой БД;
    */
    @Container
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    /*
    Перед запуском всего пакета тестов:
    - запускаем тестовый контейнер;
    - получаем экземпляр менеджера миграций;
    - запускаем миграцию в тестовом контейнере;
    - получаем доступ к фабрике сессий для связи с тестовой БД;
    - запускаем миграцию, чтобы провалидировать БД средствами Hibernate см. hibernate.cfg.xml
    */
    @BeforeAll
    public static void startTestContainer(){
        liquibaseManager = LiquibaseManager.getInstance();
        postgresContainer.start();

        connection = TestsConnectionManager.getTestBaseConnection(postgresContainer.getJdbcUrl(),
                                                             postgresContainer.getUsername(),
                                                             postgresContainer.getPassword());

        liquibaseManager.migrationsStart(connection);
        sessionFactory = TestsHibernateUtil.buildSessionFactory(postgresContainer);
    }

    /*
    Перед каждым тестом мы:
    - запускаем миграцию, чтобы прогрузить в тестовую БД все (таблицы и) данные для тестов;
    - получаем сессию для работы с БД;
    - получаем экземпляры классов сервиса и репозитория для работы с таблицей places БД;
    - открываем транзакцию;
    */
    @BeforeEach
    public void getConnectionToTestBaseAndInitIt(){
        liquibaseManager.migrationsStart(connection);

        placeRepository = new PlaceRepository(sessionFactory);
        placeService = new PlaceService(placeRepository);

        placeRepository.getEntityManager().getTransaction().begin();

        nonExistentPlaceDto = new PlaceCreateDeleteDto(Species.HALL, 4);

        existPlace = Place.builder()
                .placeId(1L)
                .species(Species.HALL)
                .placeNumber(1)
                .build();
    }

    /*
    После каждого теста мы:
    - коммитим транзакцию (вносим изменения в тестовую БД);
    - откатываем тестовую БД в состояние чистые таблицы (с сохранением таковых);

    Метод отката .rollbackDB() имеет параметр глубины отката, он определяется исходя из структуры и содержания
    'changelog' файлов, при текущей реализации проекта у нас было произведено 6-накатов (схема и 5-ть таблиц)
    + 10-накатов (заполнение БД и установка SETVAL для каждой таблицы). Экономим время и ресурсы - откатываем
    только заполнение данных. Накаты таблиц и данных идут последовательно от файла с меньшим индексом к файлу
    с большим (предполагается, что разработчик человек последовательный, и соблюдает некий порядок при загрузке
    миграционных скриптов в db.changelog-master.yaml), соответственно откаты идут в обратном порядка, как в
    стеке - LIFO - посчитать не сложно.
    */
    @AfterEach
    public void resetTestBase(){
        placeRepository.getEntityManager().getTransaction().commit();
        liquibaseManager.rollbackDB(connection, 10);
    }

    /*
    После окончания всего пакета тестов:
    - закрываем фабрику сессий;
    - останавливаем тестовый контейнер;

    Тесты закончены.
    */
    @AfterAll
    public static void stopTestContainer(){
        sessionFactory.close();
        postgresContainer.stop();
    }

    /* Блок тестов - выделим каждую группу тестов, если это необходимо, в отдельный вложенный класс, для удобства */

    /* Тестируем *.create() */

    @Test
    @DisplayName("1 - PlaceService class *.create method test")
    void shouldReturnNewPlaceId_createPlaceTest() {
        Long generateId = placeService.create(nonExistentPlaceDto);

        assertThat(generateId).isNotZero();

        Optional<PlaceReadUpdateDto> mayBePlace = placeService.findById(generateId);

        assertThat(mayBePlace).isPresent();

        assertAll(
                () -> assertThat(mayBePlace.get().placeId()).isEqualTo(generateId),
                () -> assertThat(mayBePlace.get().species()).isEqualTo(nonExistentPlaceDto.species()),
                () -> assertThat(mayBePlace.get().placeNumber()).isEqualTo(nonExistentPlaceDto.placeNumber())
        );
    }

    /* Тестируем *.findById() */

    @Nested
    @DisplayName("2 - PlaceService class *.findById method tests")
    class FindByIdMethodTests {

        @Test
        void shouldReturnPlaceReadDto_findByIdTest() {
            Optional<PlaceReadUpdateDto> mayBePlace = placeService.findById(existPlace.getPlaceId());

            assertThat(mayBePlace).isPresent();

            assertAll(
                    () -> assertThat(mayBePlace.get().placeId()).isEqualTo(existPlace.getPlaceId()),
                    () -> assertThat(mayBePlace.get().species()).isEqualTo(existPlace.getSpecies()),
                    () -> assertThat(mayBePlace.get().placeNumber()).isEqualTo(existPlace.getPlaceNumber())
            );
        }

        @Test
        void shouldReturnOptionalEmptyNonExistentPlace_findByIdTest() {
            Optional<PlaceReadUpdateDto> mayBePlace = placeService.findById(30L);
            assertThat(mayBePlace.isPresent()).isFalse();
        }
    }

    /* Тестируем *.findAll() */

    @Test
    @DisplayName("3 - PlaceService class *.findAll method test")
    void shouldReturnListOfPlaceReadDto_findAllTest() {
        List<PlaceReadUpdateDto> placeReadDtoList = placeService.findAll();
        assertThat(placeReadDtoList.size()).isEqualTo(9);
    }

    /* Тестируем *.findPlaceBySpeciesAndNumber() */

    @Nested
    @DisplayName("4 - PlaceService class *.findPlaceBySpeciesAndNumber method tests")
    class FindPlaceBySpeciesAndNumberMethodTests {
        @Test
        void shouldReturnPlaceReadDto_findPlaceBySpeciesAndNumberTest() {
            Optional<PlaceReadUpdateDto> placeReadDto =
                    placeService.findPlaceBySpeciesAndNumber(existPlace.getSpecies(), existPlace.getPlaceNumber());

            assertThat(placeReadDto).isPresent();
        }

        @Test
        void shouldThrowExceptionNonExistentPlace_findPlaceBySpeciesAndNumberTest() {
            Species species = Species.HALL;
            Integer placeNumber = 30;

            assertThatThrownBy(() -> placeService.findPlaceBySpeciesAndNumber(species, placeNumber))
                    .isInstanceOf(PlaceServiceException.class)
                    .hasMessageContaining("'" + species.name() + "' - с номером '" +
                            placeNumber + "' не существует!");
        }
    }

    /* Тестируем *.findAllPlacesBySpecies() */

    @Nested
    @DisplayName("5 - PlaceService class *.findPlaceBySpecies method tests")
    class FindPlaceBySpeciesMethodTests {

        @Test
        void shouldReturnListOfPlaceReadDto_findPlaceBySpeciesTest() {
            Species testSpecies = Species.WORKPLACE;
            List<PlaceReadUpdateDto> placeReadDtoList =
                    placeService.findAllPlacesBySpecies(testSpecies);
            assertThat(placeReadDtoList.size()).isEqualTo(6);
        }

        @Test
        void shouldThrowExceptionNonExistentPlaceList_findPlaceBySpeciesTest() {
        /*
        Поскольку наша БД уже настроена и заполнена заранее оговоренными 'местами' и 'залами' описанными
        в enum Species, как WORKPLACE и HALL, то мы добавили некую разновидность арендуемого помещения,
        как STUDIO (студия), которая в программе не используется, в текущей реализации, но используется в
        тестах и возможно в будущем...

        Плох тот Коворкинг-центр, который не мечтает занять всю территорию Москва-сити!
        */
            Species species = Species.STUDIO;

            assertThatThrownBy(() -> placeService.findAllPlacesBySpecies(species))
                    .isInstanceOf(PlaceServiceException.class)
                    .hasMessageContaining("Списка для " + "'" + species.name() + "' не существует!");
        }
    }

    /* Тестируем *.isPlaceExist() */

    @Nested
    @DisplayName("6 - PlaceService class *.isPlaceExist methods tests")
    class IsPlaceExistMethodsTests {

        @Test
        void shouldReturnTrueIfPlaceIsExistByOneParameter_isPlaceExistTest() {
            assertThat(placeService.isPlaceExist(existPlace.getPlaceId())).isTrue();
        }

        @Test
        void shouldReturnTrueIfPlaceIsExistByTwoParameters_isPlaceExistTest() {
            assertThat(placeService.isPlaceExist(existPlace.getSpecies(), existPlace.getPlaceNumber())).isTrue();
        }

        @Test
        void shouldReturnFalseIfPlaceIsNonExistentByOneParameter_isPlaceExistTest() {
            assertThat(placeService.isPlaceExist(40L)).isFalse();
        }

        @Test
        void shouldReturnFalseIfPlaceIsNonExistentByTwoParameters_isPlaceExistTest() {
            assertThat(placeService.isPlaceExist(Species.STUDIO, 70)).isFalse();
        }
    }

    /* Тестируем *.update() */

    @Nested
    @DisplayName("7 - PlaceService class *.update method tests")
    class UpdateMethodsTests {

        @Test
        void shouldReturnTrueUpdateSuccess_updateTest() {
            PlaceReadUpdateDto placeUpdateDto =
                    new PlaceReadUpdateDto(existPlace.getPlaceId(), Species.STUDIO, 15);
            assertThat(placeService.update(placeUpdateDto)).isTrue();
        }

        @Test
        void shouldReturnFalseUpdateNotExistPlace_updateTest() {
            PlaceReadUpdateDto placeUpdateDto =
                    new PlaceReadUpdateDto(100L, Species.STUDIO, 15);
            assertThat(placeService.update(placeUpdateDto)).isFalse();
        }
    }

    /* Тестируем *.delete() */

    @Nested
    @DisplayName("8 - PlaceService class *.delete method tests")
    class DeleteMethodsTests {

        @Test
        void shouldReturnTrueDeleteExistPlace_deleteTest() {
            assertThat(placeService.delete(existPlace.getPlaceId())).isTrue();
            assertThat(placeService.findById(existPlace.getPlaceId())).isEmpty();
        }

        @Test
        void shouldReturnFalseDeleteNotExistPlace_deleteTest() {
            assertThat(placeService.delete(40L)).isFalse();
        }
    }
}