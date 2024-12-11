package me.oldboy.core.controllers;

import me.oldboy.core.dto.places.PlaceCreateDeleteDto;
import me.oldboy.core.dto.places.PlaceReadUpdateDto;
import me.oldboy.core.dto.reservations.ReservationCreateDto;
import me.oldboy.core.dto.reservations.ReservationReadDto;
import me.oldboy.core.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.core.dto.slots.SlotCreateDeleteDto;
import me.oldboy.core.dto.slots.SlotReadUpdateDto;
import me.oldboy.core.dto.users.UserReadDto;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.core.model.database.entity.options.Species;
import me.oldboy.core.model.service.PlaceService;
import me.oldboy.core.model.service.ReservationService;
import me.oldboy.core.model.service.SlotService;
import me.oldboy.core.model.service.UserService;
import me.oldboy.exception.NotValidArgumentException;
import me.oldboy.exception.ReservationControllerException;
import me.oldboy.exception.ReservationServiceException;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;
    @Mock
    private PlaceService placeService;
    @Mock
    private SlotService slotService;
    @Mock
    private UserService userService;
    @InjectMocks
    private ReservationController reservationController;

    private static ReservationCreateDto reservationCreateDto;
    private static ReservationReadDto reservationReadDto;
    private static ReservationUpdateDeleteDto reservationUpdateDto, reservationDeleteDto;

    private static PlaceReadUpdateDto placeUpdateDto, placeReadDto;
    private static PlaceCreateDeleteDto placeCreateDto, placeDeleteDto;

    private static SlotReadUpdateDto slotReadDto, slotUpdateDto;
    private static SlotCreateDeleteDto slotCreateDto, slotDeleteDto;
    private static String testAdminName, testUserName;
    private static Long existId, nonExistentId, anotherId;
    private static Boolean isAdmin, isNotAdmin;
    private static UserReadDto adminUserReadDto, simpleUserReadDto, ownerUserReadDto;
    private static LocalDate testReservationDate, testReservationDateTwo;
    private static LocalTime testStartTime, testFinishTime;
    private static Integer testPlaceNumber, testSlotNumber;
    private static List<ReservationReadDto> reservationList;

    @BeforeAll
    public static void initParam(){
        testReservationDate = LocalDate.parse("2028-06-15");
        testReservationDateTwo = LocalDate.parse("2134-12-31");
        testStartTime = LocalTime.of(22,00);
        testFinishTime = LocalTime.of(23, 00);
        testPlaceNumber = 1;
        testSlotNumber = 10;
        isAdmin = true;
        isNotAdmin = false;
        existId = 1L;
        anotherId = 2L;
        nonExistentId = 45L;
        testAdminName = "Admin";
        testUserName = "SimpleUser";

        adminUserReadDto = new UserReadDto(existId, testAdminName, Role.ADMIN);
        simpleUserReadDto = new UserReadDto(nonExistentId, testUserName, Role.USER);
        ownerUserReadDto = new UserReadDto(existId, testUserName, Role.USER);

        reservationCreateDto = ReservationCreateDto.builder()
                                                   .reservationDate(testReservationDate)
                                                   .userId(existId)
                                                   .placeId(existId)
                                                   .slotId(existId)
                                                   .build();
        reservationReadDto = ReservationReadDto.builder()
                                               .reservationId(existId)
                                               .reservationDate(testReservationDate)
                                               .userId(existId)
                                               .placeId(existId)
                                               .slotId(existId)
                                               .build();
        reservationUpdateDto = ReservationUpdateDeleteDto.builder()
                                                         .reservationId(existId)
                                                         .reservationDate(testReservationDate)
                                                         .userId(existId)
                                                         .placeId(existId)
                                                         .slotId(existId)
                                                         .build();
        reservationDeleteDto = reservationUpdateDto;

        placeReadDto = PlaceReadUpdateDto.builder()
                                         .placeId(existId)
                                         .placeNumber(testPlaceNumber)
                                         .species(Species.HALL)
                                         .build();

        slotReadDto = SlotReadUpdateDto.builder()
                                       .slotId(existId)
                                       .slotNumber(testSlotNumber)
                                       .timeStart(testStartTime)
                                       .timeFinish(testFinishTime)
                                       .build();
        reservationList = List.of(ReservationReadDto.builder()
                                                    .reservationId(2L)
                                                    .reservationDate(testReservationDateTwo)
                                                    .slotId(2L)
                                                    .placeId(anotherId)
                                                    .userId(anotherId)
                                                    .build(),
                                   ReservationReadDto.builder()
                                                     .reservationId(3L)
                                                     .reservationDate(testReservationDateTwo)
                                                     .slotId(3L)
                                                     .placeId(anotherId)
                                                     .userId(anotherId)
                                                     .build(),
                                   reservationReadDto);
    }

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    /* Блок тестов - выделим каждую группу тестов в отдельный вложенный класс, для удобства и наглядности */

    /* Тесты метода *.createReservation() */

    @Nested
    @DisplayName("1 - ReservationController class *.createReservation method tests")
    class CreateReservationMethodTests {

        @Test
        void shouldReturnReadReservationDto_createReservationTest() {
            /* Mock-aем внутренние методы */
            when(userService.findByUserName(adminUserReadDto.userName())).thenReturn(Optional.of(adminUserReadDto));
            when(reservationService.create(reservationCreateDto)).thenReturn(existId);
            when(reservationService.findById(existId)).thenReturn(Optional.of(reservationReadDto));

            /* Mock-aем проверочные private методы */
            mockTrueIsCorrectMethods();

            /* Проверяем утверждение */
            try {
                assertThat(reservationController.createReservation(testAdminName, reservationCreateDto)).isEqualTo(reservationReadDto);
            } catch (ReservationControllerException e) {
                throw new RuntimeException(e);
            }

            /* Верифицируем процесс использования за-Mock-анных методов */
            verify(userService, times(1)).findByUserName(anyString());
            verify(reservationService, times(1)).create(any(ReservationCreateDto.class));
            verify(reservationService, times(1)).findById(anyLong());
            verify(placeService, times(1)).findById(anyLong());
            verify(slotService, times(1)).findById(anyLong());
            verify(reservationService, times(1)).findByDatePlaceAndSlot(any(LocalDate.class), anyLong(), anyLong());
        }

        @Test
        void shouldThrowExceptionHaveNoPlaceForReservation_createReservationTest() {
            /* Mock-aем проверочные private методы */
            mockExceptionIsNotCorrectPlace();

            /* Проверяем на бросок исключения */
            assertThatThrownBy(() -> reservationController.createReservation(testAdminName, reservationCreateDto))
                    .isInstanceOf(ReservationControllerException.class)
                    .hasMessageContaining("Try to use non-existent place! " +
                                                    "Попытка использовать несуществующее место/зал!");

            /* Верифицируем процесс использования за-Mock-анных объектов, или их не использование */
            verify(placeService, times(1)).findById(anyLong());
            verifyNoInteractions(userService, slotService, reservationService);
        }

        @Test
        void shouldThrowExceptionHaveNoSlotForReservation_createReservationTest() {
            /* Mock-aем проверочные private методы */
            mockExceptionIsNotCorrectSlotMethod();

            /* Проверяем на бросок исключения */
            assertThatThrownBy(() -> reservationController.createReservation(testAdminName, reservationCreateDto))
                    .isInstanceOf(ReservationControllerException.class)
                    .hasMessageContaining("Try to use non-existent slot! " +
                            "Попытка использовать несуществующий слот времени!");

            /* Верифицируем процесс использования за-Mock-анных классов */
            verify(placeService, times(1)).findById(anyLong());
            verify(slotService, times(1)).findById(anyLong());
        }

        @Test
        void shouldThrowExceptionDuplicateReservation_createReservationTest() {
            /* Mock-aем проверочные private методы */
            mockExceptionIsDuplicateReservation();

            /* Проверяем на бросок исключения */
            assertThatThrownBy(() -> reservationController.createReservation(testAdminName, reservationCreateDto))
                    .isInstanceOf(ReservationControllerException.class)
                    .hasMessageContaining("Duplicate reservation! Дублирование брони!");

            /* Верифицируем процесс использования за-Mock-анных объектов */
            verify(placeService, times(1)).findById(anyLong());
            verify(slotService, times(1)).findById(anyLong());
            verify(reservationService, times(1)).findByDatePlaceAndSlot(any(LocalDate.class), anyLong(), anyLong());
        }
    }

    /* Тесты метода *.updateReservation() */

    @Nested
    @DisplayName("2 - ReservationController class *.updateReservation method tests")
    class UpdateReservationMethodTests {

        @Test
        void shouldReturnTrueUpdateSuccess_updateReservationTest() {
            /* Mock-аем внутренние методы */
            when(reservationService.update(reservationUpdateDto)).thenReturn(true);

            /* Mock-аем проверочные методы */
            mockTrueIsUserCorrectMethod();
            mockTrueIsCorrectMethods();
            mockCanUpdateOrDelete();

            /* Проверяем утверждение */
            try {
                assertThat(reservationController.updateReservation(testAdminName, isAdmin, reservationUpdateDto)).isTrue();
            } catch (ReservationControllerException e) {
                throw new RuntimeException(e);
            }

            /* Верифицируем процесс использования за-Mock-анных классов */
            verify(reservationService, times(1)).update(any(ReservationUpdateDeleteDto.class));
            verify(userService, times(1)).findById(anyLong());
            verify(placeService, times(1)).findById(anyLong());
            verify(slotService, times(1)).findById(anyLong());
            verify(reservationService, times(1)).findByDatePlaceAndSlot(any(LocalDate.class), anyLong(), anyLong());
            verify(reservationService, times(1)).findById(anyLong());
            verify(userService, times(1)).findByUserName(anyString());
        }

        @Test
        void shouldThrowExceptionNotCorrectResponseUserId_updateReservationTest() {
            /* Mock-аем проверочные методы */
            mockExceptionIsNotCorrectUserId();

            /* Проверяем утверждение */
            assertThatThrownBy(() -> reservationController.updateReservation(testAdminName, isAdmin, reservationUpdateDto))
                    .isInstanceOf(ReservationControllerException.class)
                    .hasMessageContaining("Try to use non-existent userId! Применен несуществующий идентификатор пользователя!");

            /* Верифицируем процесс использования за-Mock-анных классов или пропуск в связи с выброшенным исключением */
            verify(userService, times(1)).findById(anyLong());
            verifyNoInteractions(placeService, slotService, reservationService);
        }

        @Test
        void shouldThrowExceptionNotCorrectPlaceId_updateReservationTest() {
            /* Mock-аем проверочные методы */
            mockTrueIsUserCorrectMethod();
            mockExceptionIsNotCorrectPlace();

            /* Проверяем утверждение */
            assertThatThrownBy(() -> reservationController.updateReservation(testAdminName, isAdmin, reservationUpdateDto))
                    .isInstanceOf(ReservationControllerException.class)
                    .hasMessageContaining("Try to use non-existent place! Попытка использовать несуществующее место/зал!");

            /* Верифицируем процесс использования за-Mock-анных классов или пропуск в связи с выброшенным исключением */
            verify(userService, times(1)).findById(anyLong());
            verify(placeService, times(1)).findById(anyLong());
            verifyNoInteractions(slotService, reservationService);
        }

        @Test
        void shouldThrowExceptionNotCorrectSlotId_updateReservationTest() {
            /* Mock-аем проверочные методы */
            mockTrueIsUserCorrectMethod();
            mockExceptionIsNotCorrectSlotMethod();

            /* Проверяем утверждение */
            assertThatThrownBy(() -> reservationController.updateReservation(testAdminName, isAdmin, reservationUpdateDto))
                    .isInstanceOf(ReservationControllerException.class)
                    .hasMessageContaining("Try to use non-existent slot! Попытка использовать несуществующий слот времени!");

            /* Верифицируем процесс использования за-Mock-анных классов или пропуск в связи с выброшенным исключением */
            verify(userService, times(1)).findById(anyLong());
            verify(placeService, times(1)).findById(anyLong());
            verify(slotService, times(1)).findById(anyLong());
            verifyNoInteractions(reservationService);
        }

        @Test
        void shouldThrowExceptionDuplicateReservation_updateReservationTest() {
            /* Mock-аем проверочные методы */
            mockTrueIsUserCorrectMethod();
            mockExceptionIsDuplicateReservation();

            /* Проверяем утверждение */
            assertThatThrownBy(() -> reservationController.updateReservation(testAdminName, isAdmin, reservationUpdateDto))
                    .isInstanceOf(ReservationControllerException.class)
                    .hasMessageContaining("Duplicate reservation! Дублирование брони!");

            /* Верифицируем процесс использования за-Mock-анных классов */
            verify(userService, times(1)).findById(anyLong());
            verify(placeService, times(1)).findById(anyLong());
            verify(slotService, times(1)).findById(anyLong());
            verify(reservationService, times(1)).findByDatePlaceAndSlot(testReservationDate, existId, existId);
        }

        @Test
        void shouldReturnExceptionHaveNoReservation_updateReservationTest() {
            /* Mock-аем проверочные методы */
            mockTrueIsUserCorrectMethod();
            mockTrueIsCorrectMethods();
            mockException_UnExpectedReservationId_CanUpdateOrDelete();

            /* Проверяем утверждение */
            assertThatThrownBy(() -> reservationController.updateReservation(testAdminName, isAdmin, reservationUpdateDto))
                    .isInstanceOf(ReservationControllerException.class)
                    .hasMessageContaining("Have no reservation for update or delete! Бронь для обновления или удаления не найдена!");

            /* Верифицируем процесс использования за-Mock-анных объектов */
            verify(userService, times(1)).findById(anyLong());
            verify(placeService, times(1)).findById(anyLong());
            verify(slotService, times(1)).findById(anyLong());
            verify(reservationService, times(1)).findByDatePlaceAndSlot(testReservationDate, existId, existId);
            verify(reservationService, times(1)).findById(anyLong());
        }

        @Test
        void shouldThrowExceptionHaveNoPermissionToUpdate_updateReservationTest() {
            /* Mock-аем проверочные методы */
            mockTrueIsUserCorrectMethod();
            mockTrueIsCorrectMethods();
            mockException_NotOwnerNotAdmin_CanUpdateOrDelete();

            /* Проверяем утверждение */
            assertThatThrownBy(() -> reservationController.updateReservation(testUserName, isNotAdmin, reservationUpdateDto))
                    .isInstanceOf(ReservationControllerException.class)
                    .hasMessageContaining("Have no permission to update or delete reservation! " +
                            "Недостаточно прав на обновление или удаление брони!");

            /* Верифицируем процесс использования за-Mock-анных классов */
            verify(userService, times(1)).findById(anyLong());
            verify(placeService, times(1)).findById(anyLong());
            verify(slotService, times(1)).findById(anyLong());
            verify(reservationService, times(1)).findByDatePlaceAndSlot(testReservationDate, existId, existId);
            verify(reservationService, times(1)).findById(anyLong());
            verify(userService, times(1)).findByUserName(anyString());
        }
    }

    /* Тесты метода *.deleteReservation() */

    @Nested
    @DisplayName("3 - ReservationController class *.deleteReservation method tests")
    class DeleteReservationMethodTests {

        @Test
        void shouldReturnTrueDeleteExistReservation_deleteReservationTest() {
            mockTrueIsUserCorrectMethod();
            when(reservationService.findById(existId)).thenReturn(Optional.of(reservationReadDto));
            mockCanUpdateOrDelete();
            when(reservationService.delete(reservationDeleteDto)).thenReturn(true);

            try {
                assertThat(reservationController.deleteReservation(testUserName, isNotAdmin, reservationDeleteDto)).isTrue();
            } catch (ReservationControllerException e) {
                throw new RuntimeException(e);
            }

            verify(userService, times(1)).findById(anyLong());
            verify(reservationService, times(1)).findById(anyLong());
            verify(userService, times(1)).findByUserName(anyString());
            verify(reservationService, times(1)).delete(any(ReservationUpdateDeleteDto.class));
        }

        @Test
        void shouldThrowExceptionUnExpectedUserId_deleteReservationTest() {
            mockExceptionIsNotCorrectUserId();

            assertThatThrownBy(() -> reservationController.deleteReservation(testAdminName, isAdmin, reservationDeleteDto))
                    .isInstanceOf(ReservationControllerException.class)
                    .hasMessageContaining("Try to use non-existent userId! " +
                            "Применен несуществующий идентификатор пользователя!");

            verify(userService, times(1)).findById(anyLong());
            verifyNoInteractions(reservationService);
        }

        @Test
        void shouldThrowExceptionHaveNoReservationId_deleteReservationTest() {
            mockTrueIsUserCorrectMethod();
            mockException_UnExpectedReservationId_CanUpdateOrDelete();

            assertThatThrownBy(() -> reservationController.deleteReservation(testAdminName, isAdmin, reservationDeleteDto))
                    .isInstanceOf(ReservationControllerException.class)
                    .hasMessageContaining("Have no reservation for update or delete! " +
                            "Бронь для обновления или удаления не найдена!");

            verify(userService, times(1)).findById(anyLong());
            verify(reservationService, times(1)).findById(anyLong());
        }

        @Test
        void shouldThrowExceptionHaveNoPermission_deleteReservationTest() {
            mockTrueIsUserCorrectMethod();
            mockException_NotOwnerNotAdmin_CanUpdateOrDelete();

            assertThatThrownBy(() -> reservationController.deleteReservation(testUserName, isNotAdmin, reservationDeleteDto))
                    .isInstanceOf(ReservationControllerException.class)
                    .hasMessageContaining("Have no permission to update or delete reservation! " +
                            "Недостаточно прав на обновление или удаление брони!");

            verify(userService, times(1)).findById(anyLong());
            verify(reservationService, times(1)).findById(anyLong());
            verify(userService, times(1)).findByUserName(anyString());
        }
    }

    /* Тесты метода *.readAllReservation() */

    @Test
    @DisplayName("4 - ReservationController class *.readAllReservation method test")
    void shouldReturnSizeOfList_readAllReservationTest() {
        try {
            when(reservationService.findAll()).thenReturn(reservationList);
            assertThat(reservationController.readAllReservation().size()).isEqualTo(reservationList.size());
        } catch (ReservationServiceException e) {
            throw new RuntimeException(e);
        }
    }

    /* Тесты метода *.getReservationByParam() */

    @Nested
    @DisplayName("5 - ReservationController class *.getReservationByParam method tests")
    class GetReservationByParamMethodTests {

        @Test
        void shouldReturnListSizeForDateParameter_getReservationByParamTest() {
            List<ReservationReadDto> filteredByDateList =
                    reservationList.stream().filter(r -> r.reservationDate().equals(testReservationDateTwo)).toList();
            try {
                when(reservationService.findByDate(testReservationDateTwo))
                        .thenReturn(Optional.of(filteredByDateList));
                assertThat(reservationController.getReservationByParam(testReservationDateTwo.toString(), null, null).size())
                        .isEqualTo(filteredByDateList.size());
            } catch (ReservationServiceException | NotValidArgumentException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        void shouldReturnListSizeForUserIdParameter_getReservationByParamTest() {
            List<ReservationReadDto> filteredByDateList =
                    reservationList.stream().filter(r -> r.userId().equals(anotherId)).toList();
            try {
                when(reservationService.findByUserId(anotherId))
                        .thenReturn(Optional.of(filteredByDateList));
                assertThat(reservationController.getReservationByParam(null, String.valueOf(anotherId), null).size())
                        .isEqualTo(filteredByDateList.size());
            } catch (ReservationServiceException | NotValidArgumentException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        void shouldReturnListSizeForPlaceIdParameter_getReservationByParamTest() {
            List<ReservationReadDto> filteredByDateList =
                    reservationList.stream().filter(r -> r.placeId().equals(anotherId)).toList();
            try {
                when(reservationService.findByPlaceId(anotherId))
                        .thenReturn(Optional.of(filteredByDateList));
                assertThat(reservationController.getReservationByParam(null, null, String.valueOf(anotherId)).size())
                        .isEqualTo(filteredByDateList.size());
            } catch (ReservationServiceException | NotValidArgumentException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        void shouldThrowExceptionWrongUserId_getReservationByParamTest() {
            assertThatThrownBy(() -> reservationController.getReservationByParam(null, String.valueOf(-20L), null))
                    .isInstanceOf(NotValidArgumentException.class)
                    .hasMessageContaining("Check parameter - must be positive! " +
                            "Проверьте введенный параметр - не может быть отрицательным!");
        }

        @Test
        void shouldReturnExceptionWrongPlaceId_getReservationByParamTest() {
            assertThatThrownBy(() -> reservationController.getReservationByParam(null, null, String.valueOf(-20L)))
                    .isInstanceOf(NotValidArgumentException.class)
                    .hasMessageContaining("Check parameter - must be positive! " +
                            "Проверьте введенный параметр - не может быть отрицательным!");
        }

        @Test
        void shouldReturnExceptionInvalidCombinationParameter_getReservationByParamTest() {
            assertThatThrownBy(() -> reservationController.getReservationByParam(testReservationDateTwo.toString(), null, String.valueOf(20L)))
                    .isInstanceOf(NotValidArgumentException.class)
                    .hasMessageContaining("Invalid combination of parameters (need only reservationDate or placeId or placeSpecies, not combination)! " +
                            "Неверное сочетание параметров (достаточно одного параметра, комбинация не принимается)!");

            assertThatThrownBy(() -> reservationController.getReservationByParam(null, String.valueOf(25L), String.valueOf(20L)))
                    .isInstanceOf(NotValidArgumentException.class)
                    .hasMessageContaining("Invalid combination of parameters (need only reservationDate or placeId or placeSpecies, not combination)! " +
                            "Неверное сочетание параметров (достаточно одного параметра, комбинация не принимается)!");

            assertThatThrownBy(() -> reservationController.getReservationByParam(testReservationDateTwo.toString(), String.valueOf(25L), null))
                    .isInstanceOf(NotValidArgumentException.class)
                    .hasMessageContaining("Invalid combination of parameters (need only reservationDate or placeId or placeSpecies, not combination)! " +
                            "Неверное сочетание параметров (достаточно одного параметра, комбинация не принимается)!");

            assertThatThrownBy(() -> reservationController.getReservationByParam(testReservationDateTwo.toString(), String.valueOf(25L), String.valueOf(25L)))
                    .isInstanceOf(NotValidArgumentException.class)
                    .hasMessageContaining("Invalid combination of parameters (need only reservationDate or placeId or placeSpecies, not combination)! " +
                            "Неверное сочетание параметров (достаточно одного параметра, комбинация не принимается)!");
        }

        @Test
        void shouldThrowExceptionParseError_getReservationByParamTest() {
            assertThatThrownBy(() -> reservationController.getReservationByParam(String.valueOf(nonExistentId), null, null))
                    .isInstanceOf(NotValidArgumentException.class)
                    .hasMessageContaining("Date value is empty or invalid, expected, for example - 'YYYY-MM-DD'! " +
                            "Значение даты пустое или не верно, ожидается, например - '2007-12-03' !");

            assertThatThrownBy(() -> reservationController.getReservationByParam(null, "-20Res", null))
                    .isInstanceOf(NotValidArgumentException.class)
                    .hasMessageContaining("Parse or unexpected error (check the entered parameters): For input string: \"-20Res\"");
        }
    }

    /* Тесты метода *.getFreeSlotsByDate() */

    @Test
    @DisplayName("6 - ReservationController class *.getFreeSlotsByDate method test")
    void getFreeSlotsByDateTest() {
        Map<Long, List<Long>> freeSlotByPlace = Map.of(existId, List.of(existId, anotherId),
                                                       anotherId, List.of(existId, anotherId));
        try {
            when(reservationService.findAllFreeSlotsByDate(testReservationDate)).thenReturn(freeSlotByPlace);
            assertThat(reservationController.getFreeSlotsByDate(testReservationDate.toString()).size()).isEqualTo(freeSlotByPlace.size());
        } catch (ReservationServiceException | ReservationControllerException e) {
            throw new RuntimeException(e);
        }
    }

    /* Вспомогательные методы */

    /*
    Еще раз, поскольку мы передаем в работу некий ReservationUpdateDeleteDto, который составлен и
    отправлен, нашему приложению, пользователем (сервисом) по определенному шаблону и содержит:
    - reservationId - ID брони;
    - reservationDate - Дату брони;
    - userId - ID пользователя, который создал эту бронь (или она ему принадлежит);
    - placeId - ID забронированного места;
    - slotId - ID временного слота;
    то может возникнуть ситуация, что в переданном DTO прилетели данные "взятые с потолка", т.е. никак не
    связанные с реальными записями в БД. Да, вроде бы все поля переданного DTO присутствуют, но по любому
    из переданных ID (частично или по всем сразу) записей в БД нет - без проверки хапнем ошибку в момент
    передачи SQL запроса в БД.
    */

    private void mockTrueIsCorrectMethods() {
        when(placeService.findById(existId)).thenReturn(Optional.of(placeReadDto));
        when(slotService.findById(existId)).thenReturn(Optional.of(slotReadDto));
        when(reservationService.findByDatePlaceAndSlot(testReservationDate, existId, existId)).thenReturn(Optional.empty());
    }

    private void mockExceptionIsNotCorrectPlace() {
        when(placeService.findById(existId)).thenReturn(Optional.empty());
    }

    private void mockExceptionIsNotCorrectSlotMethod() {
        when(placeService.findById(existId)).thenReturn(Optional.of(placeReadDto));
        when(slotService.findById(existId)).thenReturn(Optional.empty());
    }

    private void mockExceptionIsDuplicateReservation() {
        when(placeService.findById(existId)).thenReturn(Optional.of(placeReadDto));
        when(slotService.findById(existId)).thenReturn(Optional.of(slotReadDto));
        when(reservationService.findByDatePlaceAndSlot(testReservationDate, existId, existId)).thenReturn(Optional.of(reservationReadDto));
    }

    private void mockTrueIsUserCorrectMethod() {
        when(userService.findById(existId)).thenReturn(Optional.of(adminUserReadDto));
    }

    private void mockExceptionIsNotCorrectUserId() {
        when(userService.findById(nonExistentId)).thenReturn(Optional.empty());
    }

    private void mockCanUpdateOrDelete(){
        when(reservationService.findById(existId)).thenReturn(Optional.of(reservationReadDto));
        when(userService.findByUserName(testAdminName)).thenReturn(Optional.of(adminUserReadDto));
        when(userService.findByUserName(testUserName)).thenReturn(Optional.of(ownerUserReadDto));
    }

    private void mockException_UnExpectedReservationId_CanUpdateOrDelete(){
        when(reservationService.findById(nonExistentId)).thenReturn(Optional.empty());
    }

    private void mockException_NotOwnerNotAdmin_CanUpdateOrDelete(){
        when(reservationService.findById(existId)).thenReturn(Optional.of(reservationReadDto));
        when(userService.findByUserName(testUserName)).thenReturn(Optional.of(simpleUserReadDto));
    }
}