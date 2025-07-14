package me.oldboy.integration.services;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.reservations.ReservationCreateDto;
import me.oldboy.dto.reservations.ReservationReadDto;
import me.oldboy.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.exception.reservation_exception.ReservationServiceException;
import me.oldboy.integration.annotation.IT;
import me.oldboy.repository.UserRepository;
import me.oldboy.services.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IT
class ReservationServiceIT extends TestContainerInit {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private UserRepository userRepository;

    private ReservationCreateDto correctCreateDto, badCreateReservationDto;
    private ReservationUpdateDeleteDto correctUpdateDto, notCorrectUpdateDto, forDeleteDto, canNotDeleteDto;
    private Long existId, nonExistentId;
    private LocalDate notExistentDate, existentDate;

    @BeforeEach
    void setUp() {
        existId = 1L;
        nonExistentId = 100L;

        notExistentDate = LocalDate.of(2030, 04, 15);
        existentDate = LocalDate.of(2029, 07, 28);

        correctCreateDto = ReservationCreateDto.builder()
                .reservationDate(notExistentDate)
                .userId(existId)
                .placeId(existId)
                .slotId(existId)
                .build();
        badCreateReservationDto = ReservationCreateDto.builder()
                .reservationDate(existentDate)
                .userId(nonExistentId)
                .placeId(existId)
                .slotId(existId)
                .build();

        correctUpdateDto = ReservationUpdateDeleteDto.builder() // Назначаем новую дату и неиспользуемые слот и время
                .reservationId(existId)
                .reservationDate(notExistentDate)
                .userId(existId)
                .placeId(existId + 2)
                .slotId(existId + 4)
                .build();
        notCorrectUpdateDto = ReservationUpdateDeleteDto.builder()  // Попытка дублировать первую запись из БД
                .reservationId(existId + 3)
                .reservationDate(existentDate)
                .userId(existId)
                .placeId(existId)
                .slotId(existId)
                .build();

        forDeleteDto = notCorrectUpdateDto;
        canNotDeleteDto = correctUpdateDto;
    }

    /*
        Важный метод, который определяет существует ли бронь на конкретную дату, время
        и место, т.е. может использоваться для проверки создания дубликата бронирования.
        Мы не можем зарезервировать конкретное место на конкретную дату и временной
        диапазон - слот, если оно уже занято, кто занял уже на важно. Тестируем первым.
    */
    @Test
    void findByDatePlaceAndSlot_shouldReturnOptionalEmpty_Test() {
        Optional<ReservationReadDto> mayBeReservation = reservationService.findByDatePlaceAndSlot(notExistentDate, existId, existId);
        assertThat(mayBeReservation.isEmpty()).isTrue();
    }

    @Test
    void findByDatePlaceAndSlot_shouldReturnFoundRecord_Test() {
        Optional<ReservationReadDto> mayBeReservation = reservationService.findByDatePlaceAndSlot(existentDate, existId, existId);
        if (mayBeReservation.isPresent()) {
            assertThat(mayBeReservation.get()).isNotNull();
            assertThat(mayBeReservation.get().reservationDate()).isEqualTo(existentDate);
        }
    }

    @Test
    void create_shouldReturnReservationId_Test() {
        Long createdId = reservationService.create(correctCreateDto);
        assertThat(createdId).isGreaterThan(8);
    }

    @Test
    void create_shouldReturnException_DuplicateReservation_Test() {
        assertThatThrownBy(() -> reservationService.create(badCreateReservationDto))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Резервирование не возможно, измените дату,место или время.");
    }

    @Test
    void update_shouldReturnTrue_afterSuccessUpdate_Test() {
        boolean isUpdateSuccess = reservationService.update(correctUpdateDto);
        assertThat(isUpdateSuccess).isTrue();
    }

    @Test
    void update_shouldReturnException_HaveNoCorrectDataForUpdate_Test() {
        assertThatThrownBy(() -> reservationService.update(notCorrectUpdateDto))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Обновление не возможно, дата, место или слот заняты.");
    }

    @Test
    void delete_shouldReturnTrue_afterDelete_Test() {
        boolean isDeleteSuccess = reservationService.delete(forDeleteDto);
        assertThat(isDeleteSuccess).isTrue();
    }

    @Test
    void delete_shouldReturnException_HaveNoReservationForDelete_Test() {
        assertThatThrownBy(() -> reservationService.delete(canNotDeleteDto))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронь для удаления не найдена!");
    }

    @Test
    void findById_shouldReturnFoundDto_Test() {
        Optional<ReservationReadDto> mayBeReservation = reservationService.findById(existId);
        if (mayBeReservation.isPresent()) {
            assertThat(mayBeReservation.get().reservationId()).isEqualTo(existId);
        }
    }

    @Test
    void findById_shouldReturnOptionalEmpty_notFoundReservation_Test() {
        Optional<ReservationReadDto> mayBeReservation = reservationService.findById(nonExistentId);
        assertThat(mayBeReservation.isEmpty()).isTrue();
    }

    @Test
    void findAll_shouldReturnReservationList_Test() {
        List<ReservationReadDto> listFoundDto = reservationService.findAll();
        assertThat(listFoundDto.size()).isGreaterThan(7);
    }

    @Test
    void findByUserId_shouldReturnDtoList_forCurrentUserId_Test() {
        Optional<List<ReservationReadDto>> mayBeListDto = reservationService.findByUserId(existId);
        if (mayBeListDto.isPresent()) {
            assertThat(mayBeListDto.get()).isNotNull();
            assertThat(mayBeListDto.get().size()).isGreaterThan(1);
        }
    }

    @Test
    void findByUserId_shouldReturnException_haveNoUserId_Test() {
        assertThatThrownBy(() -> reservationService.findByUserId(nonExistentId))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("There are no reservations for user with ID - " + nonExistentId + "! " +
                        "Бронирования для пользователя с ID - " + nonExistentId + " отсутствуют!");
    }

    @Test
    void findBySlotId_shouldReturnDtoList_forCurrentSlotId_Test() {
        Optional<List<ReservationReadDto>> mayBeListDto = reservationService.findBySlotId(existId);
        if (mayBeListDto.isPresent()) {
            assertThat(mayBeListDto.get()).isNotNull();
            assertThat(mayBeListDto.get().size()).isGreaterThan(1);
        }
    }

    @Test
    void findBySlotId_shouldReturnException_haveNoSlotId_Test() {
        assertThatThrownBy(() -> reservationService.findBySlotId(nonExistentId))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("There are no reservations for slot ID " + nonExistentId + "! " +
                        "Бронирования для " + nonExistentId + " отсутствуют!");
    }

    @Test
    void findByPlaceId_shouldReturnDtoList_forCurrentPlaceId_Test() {
        Optional<List<ReservationReadDto>> mayBeListDto = reservationService.findByPlaceId(existId);
        if (mayBeListDto.isPresent()) {
            assertThat(mayBeListDto.get()).isNotNull();
            assertThat(mayBeListDto.get().size()).isGreaterThan(2);
        }
    }

    @Test
    void findByPlaceId_shouldReturnException_haveNoPlaceId_Test() {
        assertThatThrownBy(() -> reservationService.findByPlaceId(nonExistentId))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("There are no reservations for place ID " + nonExistentId + "! " +
                        "Бронирования для " + nonExistentId + " отсутствуют!");
    }

    @Test
    void findByDate_shouldReturnDtoList_forCurrentDate_Test() {
        Optional<List<ReservationReadDto>> mayBeListDto = reservationService.findByDate(existentDate);
        if (mayBeListDto.isPresent()) {
            assertThat(mayBeListDto.get()).isNotNull();
            assertThat(mayBeListDto.get().size()).isGreaterThan(3);
        }
    }

    @Test
    void findByDate_shouldReturnException_haveNoDate_Test() {
        assertThatThrownBy(() -> reservationService.findByDate(notExistentDate))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("There are no reservations for date " + notExistentDate + "! " +
                        "Бронирования на " + notExistentDate + " отсутствуют!");
    }

    @Test
    void findAllFreeSlotsByDate_shouldReturnFreeSlotMap_forCurrentDate_Test() {
        /* На заданную дату у нас есть 4-и брони */
        Map<Long, List<Long>> freeReservationMap = reservationService.findAllFreeSlotsByDate(existentDate);

        assertThat(freeReservationMap).isNotNull();
        /* У нас 9-ть мест и 9-ть слотов, т.е. на конкретную дату можно "застолбить" 81 слот */
        assertThat(freeReservationMap.size()).isEqualTo(9); // Эквивалентно количеству мест

        int sum = 0;
        for (long i = 0; i < freeReservationMap.size(); i++){
            List list = freeReservationMap.get(i + 1);
            sum += list.size();
        }

        /* На заданную дату у нас есть 4-и брони или 77 свободных "ячеек" (сочетаний место и время) резервирования */
        assertThat(sum).isEqualTo(77);
    }

    @Test
    void findAllFreeSlotsByDate_shouldReturnException_haveNoReservation_Test() {
        assertThatThrownBy(() -> reservationService.findAllFreeSlotsByDate(notExistentDate))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("There are no reservations for date " + notExistentDate + "! " +
                        "Бронирования на " + notExistentDate + " отсутствуют!");
    }
}