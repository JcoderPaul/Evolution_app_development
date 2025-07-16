package me.oldboy.integration.repository;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.entity.Reservation;
import me.oldboy.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@IT
class ReservationRepositoryIT extends TestContainerInit {

    @Autowired
    private ReservationRepository reservationRepository;
    private LocalDate existReserveDate, withNoReserveDate;
    private Long existId, nonExistingId;

    @BeforeEach
    void setUp(){
        existReserveDate = LocalDate.of(2029, 07, 28);
        withNoReserveDate = LocalDate.of(2032, 02, 18);

        existId = 1L;
        nonExistingId = 200L;
    }

    @Test
    void findByDate_shouldReturnGivenListSize_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findByDate(existReserveDate);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get()).isNotEmpty();
            assertThat(mayBeList.get().size()).isEqualTo(4);
        }
    }

    @Test
    void findByDate_shouldReturnZeroListSize_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findByDate(withNoReserveDate);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get().size()).isEqualTo(0);
        }
    }

    @Test
    void findByPlaceId_shouldReturnReservationList_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findByPlaceId(existId);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get()).isNotEmpty();
            assertThat(mayBeList.get().size()).isGreaterThan(1);
        }
    }

    @Test
    void findByPlaceId_shouldReturnZeroListSize_WithNonExistentId_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findByPlaceId(nonExistingId);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get().size()).isEqualTo(0);
        }
    }

    @Test
    void findBySlotId_shouldReturnReservationList_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findBySlotId(existId);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get()).isNotEmpty();
            assertThat(mayBeList.get().size()).isGreaterThan(1);
        }
    }

    @Test
    void findBySlotId_shouldReturnZeroListSize_WithNonExistentId_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findBySlotId(nonExistingId);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get().size()).isEqualTo(0);
        }
    }

    @Test
    void findByUserId_shouldReturnReservationList_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findByUserId(existId);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get()).isNotEmpty();
            assertThat(mayBeList.get().size()).isGreaterThan(1);
        }
    }

    @Test
    void findByUserId_shouldReturnZeroListSize_WithNonExistentId_Test() {
        Optional<List<Reservation>> mayBeList = reservationRepository.findByUserId(nonExistingId);
        if(mayBeList.isPresent()){
            assertThat(mayBeList.get().size()).isEqualTo(0);
        }
    }

    @Test
    void findByDatePlaceAndSlot_shouldReturnUniqueRecord_Test() {
        Optional<Reservation> mayBeReservation =
                reservationRepository.findByDatePlaceAndSlot(existReserveDate, existId, existId);
        if(mayBeReservation.isPresent()){
            assertThat(mayBeReservation.get()).isNotNull();
            assertThat(mayBeReservation.get().getReservationDate()).isEqualTo(existReserveDate);
        }
    }

    @Test
    void findByDatePlaceAndSlot_shouldReturnNoRecord_ForAnyOneParam_Test() {
        Optional<Reservation> mayBeReservation =
                reservationRepository.findByDatePlaceAndSlot(existReserveDate, nonExistingId, existId);
        assertThat(mayBeReservation.isEmpty()).isTrue();
    }
}