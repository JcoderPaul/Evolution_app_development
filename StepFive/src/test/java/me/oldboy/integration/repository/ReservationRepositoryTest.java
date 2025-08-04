package me.oldboy.integration.repository;

import me.oldboy.integration.ITBaseStarter;
import me.oldboy.models.entity.Reservation;
import me.oldboy.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationRepositoryTest extends ITBaseStarter {

    @Autowired
    private ReservationRepository reservationRepository;

    private Long existId, nonExistId;
    private LocalDate existDate, nonExistentDate;

    @BeforeEach
    void setUp() {
        existId = 1L;
        nonExistId = 100L;

        existDate = LocalDate.of(2029, 7, 28);
        nonExistentDate = LocalDate.of(1905, 9, 13);
    }

    @Test
    void findByDate_shouldReturnTestBaseRecordCount_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findByDate(existDate);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get().size()).isEqualTo(4);
        }
    }

    @Test
    void findByDate_shouldReturnOptionalEmptyList_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findByDate(nonExistentDate);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get().size()).isEqualTo(0);
        }
    }

    @Test
    void findByPlaceId_shouldReturnCurrentPlaceIdRecordCount_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findByPlaceId(existId);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get().size()).isEqualTo(3);
        }
    }

    @Test
    void findByPlaceId_shouldReturnOptionalEmptyList_noPlaceNoReservation_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findByPlaceId(nonExistId);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get().size()).isEqualTo(0);
        }
    }

    @Test
    void findBySlotId_shouldReturnReservationRecordList_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findBySlotId(existId);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get().size()).isEqualTo(2);
        }
    }

    @Test
    void findBySlotId_shouldReturnOptionalEmptyList_noSlotNoRecord_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findBySlotId(nonExistId);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get().size()).isEqualTo(0);
        }
    }

    @Test
    void findByUserId_shouldReturnListOfRecord_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findByUserId(existId);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get().size()).isEqualTo(3);
        }
    }

    @Test
    void findByUserId_shouldReturnOptionalEmptyList_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findByUserId(nonExistId);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get().size()).isEqualTo(0);
        }
    }

    @Test
    void findByDatePlaceAndSlot_shouldReturnFoundRecord_Test() {
        Optional<Reservation> mayBeReservation =
                reservationRepository.findByDatePlaceAndSlot(existDate, existId, existId);
        if(mayBeReservation.isPresent()){
            assertThat(mayBeReservation.get().getReservationDate()).isEqualTo(existDate);
            assertThat(mayBeReservation.get().getPlace().getPlaceId()).isEqualTo(existId);
            assertThat(mayBeReservation.get().getSlot().getSlotId()).isEqualTo(existId);
        }
    }

    @Test
    void findByDatePlaceAndSlot_shouldReturnOptionalEmpty_Test() {
        Optional<Reservation> mayBeReservation =
                reservationRepository.findByDatePlaceAndSlot(nonExistentDate, existId, existId);
        if(mayBeReservation.isEmpty()){
            assertThat(mayBeReservation.isPresent()).isFalse();
        }
    }
}