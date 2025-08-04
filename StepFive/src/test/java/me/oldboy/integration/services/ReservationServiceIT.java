package me.oldboy.integration.services;

import me.oldboy.dto.reservations.ReservationCreateDto;
import me.oldboy.dto.reservations.ReservationReadDto;
import me.oldboy.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.exception.reservation_exception.ReservationServiceException;
import me.oldboy.integration.ITBaseStarter;
import me.oldboy.services.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationServiceIT extends ITBaseStarter {

    @Autowired
    private ReservationService reservationService;

    private ReservationCreateDto reservationCreateNewDto, reservationCreateExistDto;
    private ReservationUpdateDeleteDto reservationUpdateDto, reservationUpdateDuplicationDto,
            reservationDeleteDto, reservationDeleteNonExistDto;
    private LocalDate existDate, anotherExistDate, nonExistentDate;
    private Long existId, nonExistentId;

    @BeforeEach
    void setUp() {
        existId = 1L;
        nonExistentId = 100L;

        existDate = LocalDate.of(2029, 7, 28);
        anotherExistDate = LocalDate.of(2029, 7, 29);
        nonExistentDate = LocalDate.of(2043, 12, 18);

        reservationCreateNewDto = ReservationCreateDto.builder()
                .reservationDate(nonExistentDate)
                .userId(existId)
                .slotId(existId + 1)
                .placeId(existId + 2)
                .build();
        reservationCreateExistDto = ReservationCreateDto.builder()
                .reservationDate(existDate)
                .userId(existId)
                .slotId(existId)
                .placeId(existId)
                .build();

        reservationUpdateDto = ReservationUpdateDeleteDto.builder()
                .reservationId(existId)
                .reservationDate(nonExistentDate)
                .userId(existId)
                .placeId(existId + 1)
                .slotId(existId + 2)
                .build();
        reservationUpdateDuplicationDto = ReservationUpdateDeleteDto.builder()
                .reservationId(existId)
                .reservationDate(anotherExistDate)
                .userId(existId + 1)
                .placeId(existId + 4)
                .slotId(existId + 5)
                .build();

        reservationDeleteDto = ReservationUpdateDeleteDto.builder()
                .reservationId(existId)
                .reservationDate(existDate)
                .userId(existId)
                .placeId(existId)
                .slotId(existId)
                .build();
        reservationDeleteNonExistDto = ReservationUpdateDeleteDto.builder()
                .reservationId(existId)
                .reservationDate(nonExistentDate)
                .userId(nonExistentId)
                .placeId(nonExistentId)
                .slotId(nonExistentId)
                .build();
    }

    @Test
    void create_shouldReturnGeneratedId_Test() {
        assertThat(reservationService.create(reservationCreateNewDto)).isGreaterThan(8);
    }

    @Test
    void create_shouldReturnException_Test() {
        assertThatThrownBy(() -> reservationService.create(reservationCreateExistDto))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Резервирование не возможно, измените дату,место или время.");
    }

    @Test
    void update_shouldReturnTrue_afterUpdate_Test() {
        assertThat(reservationService.update(reservationUpdateDto)).isTrue();
    }

    @Test
    void update_shouldReturnException_duplicateReservationAfterUpdate_Test() {
        assertThatThrownBy(() -> reservationService.update(reservationUpdateDuplicationDto))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Обновление не возможно, дата, место или слот заняты.");
    }

    @Test
    void delete_shouldReturnTrue_afterDelete_Test() {
        assertThat(reservationService.delete(reservationDeleteDto)).isTrue();
    }

    @Test
    void delete_shouldReturnException_removeNonExistReservation_Test() {
        assertThatThrownBy(() -> reservationService.delete(reservationDeleteNonExistDto))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронь для удаления не найдена!");
    }

    @Test
    void findById_shouldReturnReadDto_Test() {
        Optional<ReservationReadDto> mayBeFound = reservationService.findById(existId);
        if (mayBeFound.isPresent()) {
            assertThat(mayBeFound.get().reservationId()).isEqualTo(existId);
        }
    }

    @Test
    void findById_shouldReturnOptionalEmpty_notFound_Test() {
        Optional<ReservationReadDto> mayBeFound = reservationService.findById(nonExistentId);
        assertThat(mayBeFound.isEmpty()).isTrue();
    }

    @Test
    void findAll() {
        assertThat(reservationService.findAll().size()).isEqualTo(8);
    }

    @Test
    void findByDatePlaceAndSlot_shouldReturnFoundReservation_andTrue_Test() {
        assertThat(reservationService.findByDatePlaceAndSlot(existDate, existId, existId).isPresent()).isTrue();
    }

    @Test
    void findByDatePlaceAndSlot_shouldReturnOptionalEmpty_andFalse_Test() {
        assertThat(reservationService.findByDatePlaceAndSlot(nonExistentDate, existId, existId).isPresent()).isFalse();
    }

    @Test
    void findByUserId_shouldReturnRecordList_forUserWithReservation_Test() {
        Optional<List<ReservationReadDto>> mayBeFoundList = reservationService.findByUserId(existId);
        if (mayBeFoundList.isPresent()) {
            assertThat(mayBeFoundList.get().size()).isEqualTo(3);
        }
    }

    @Test
    void findByUserId_shouldReturnException_forUserWithOutReservation_Test() {
        assertThatThrownBy(() -> reservationService.findByUserId(existId + 3))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронирования для пользователя с ID - " + (existId + 3) + " отсутствуют!");
    }

    @Test
    void findByUserId_shouldReturnException_forNonExistUser_Test() {
        assertThatThrownBy(() -> reservationService.findByUserId(nonExistentId))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронирования для пользователя с ID - " + nonExistentId + " отсутствуют!");
    }

    @Test
    void findBySlotId_shouldReturnReservationRecordList_Test() {
        Optional<List<ReservationReadDto>> mayBeFoundList = reservationService.findBySlotId(existId);
        if (mayBeFoundList.isPresent()) {
            assertThat(mayBeFoundList.get().size()).isEqualTo(2);
        }
    }

    @Test
    void findBySlotId_shouldReturnException_forUserWithOutReservation_Test() {
        assertThatThrownBy(() -> reservationService.findBySlotId(existId + 3))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронирования для " + (existId + 3) + " отсутствуют!");
    }

    @Test
    void findBySlotId_shouldReturnException_forNonExistSlot_Test() {
        assertThatThrownBy(() -> reservationService.findBySlotId(nonExistentId))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронирования для " + nonExistentId + " отсутствуют!");
    }

    @Test
    void findByPlaceId_shouldReturnReservationRecordList_Test() {
        Optional<List<ReservationReadDto>> mayBeFoundList = reservationService.findByPlaceId(existId);
        if (mayBeFoundList.isPresent()) {
            assertThat(mayBeFoundList.get().size()).isEqualTo(3);
        }
    }

    @Test
    void findByPlaceId_shouldReturnException_noRecordForExistPlace_Test() {
        assertThatThrownBy(() -> reservationService.findByPlaceId(existId + 2))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронирования для " + (existId + 2) + " отсутствуют!");

    }

    @Test
    void findByPlaceId_shouldReturnException_noRecordForNonExistentPlace_Test() {
        assertThatThrownBy(() -> reservationService.findByPlaceId(nonExistentId))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронирования для " + nonExistentId + " отсутствуют!");

    }

    @Test
    void findByDate_shouldReturnRecordList_Test() {
        Optional<List<ReservationReadDto>> mayBeFound = reservationService.findByDate(existDate);
        if (mayBeFound.isPresent()) {
            assertThat(mayBeFound.get().size()).isEqualTo(4);
        }
    }

    @Test
    void findByDate_shouldReturnException_haveNoRecord_Test() {
        assertThatThrownBy(() -> reservationService.findByDate(nonExistentDate))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронирования на " + nonExistentDate + " отсутствуют!");
    }

    @Test
    void findAllFreeSlotsByDate_shouldReturnFreeSlotCount_Test() {
        Map<Long, List<Long>> freeSlotByDate = reservationService.findAllFreeSlotsByDate(existDate);

        /*
        Можно использовать классику:
            int sum = 0;

            for (long i = 0; i < freeSlotByDate.size(); i++){
                List list = freeSlotByDate.get(i + 1);
                sum += list.size();
            }

            assertThat(sum).isEqualTo(77);

        а можно так:
        */

        AtomicInteger sum = new AtomicInteger();
        freeSlotByDate.forEach((k, v) -> sum.addAndGet(v.size()));

        assertThat(sum.get()).isEqualTo(77);
    }

    @Test
    void findAllFreeSlotsByDate_shouldReturnException_haveNoReservationByDate_Test() {
        assertThatThrownBy(() -> reservationService.findAllFreeSlotsByDate(nonExistentDate))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронирования на " + nonExistentDate + " отсутствуют!");
    }
}