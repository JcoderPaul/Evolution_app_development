package me.oldboy.unit.services;

import me.oldboy.dto.reservations.ReservationCreateDto;
import me.oldboy.dto.reservations.ReservationReadDto;
import me.oldboy.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.exception.reservation_exception.ReservationServiceException;
import me.oldboy.models.entity.Place;
import me.oldboy.models.entity.Reservation;
import me.oldboy.models.entity.Slot;
import me.oldboy.models.entity.User;
import me.oldboy.repository.PlaceRepository;
import me.oldboy.repository.ReservationRepository;
import me.oldboy.repository.SlotRepository;
import me.oldboy.repository.UserRepository;
import me.oldboy.services.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.shaded.org.checkerframework.framework.qual.DefaultQualifier;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ReservationServiceTest {

    @Mock
    private SlotRepository slotRepository;
    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private ReservationService reservationService;

    private Reservation createdReservation, reservationExist;
    private ReservationCreateDto reservationNewCreateDto;
    private ReservationReadDto reservationExistReadDto;
    private ReservationUpdateDeleteDto reservationUpdateDto, reservationDeleteDto;
    private Long existId, nonExistId;
    private LocalDate existDate, nonExistentDate;
    private User existUser;
    private Place existPlace;
    private Slot existSlot;
    private List<Reservation> testReservationList, reservationByDateList;
    private List<Place> testPlaceList;
    private List<Slot> testSlotList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existId = 1L;
        nonExistId = 100L;

        existDate = LocalDate.of(2029, 12, 12);
        nonExistentDate = LocalDate.of(2031, 02, 04);

        reservationNewCreateDto = ReservationCreateDto.builder()
                .reservationDate(nonExistentDate)
                .userId(existId)
                .placeId(existId)
                .slotId(existId)
                .build();

        reservationExistReadDto = ReservationReadDto.builder()
                .reservationId(existId)
                .reservationDate(existDate)
                .userId(existId)
                .placeId(existId)
                .slotId(existId)
                .build();

        existUser = User.builder().userId(existId).build();
        existPlace = Place.builder().placeId(existId).build();
        existSlot = Slot.builder().slotId(existId).build();

        when(userRepository.findById(existId)).thenReturn(Optional.of(existUser));
        when(placeRepository.findById(existId)).thenReturn(Optional.of(existPlace));
        when(slotRepository.findById(existId)).thenReturn(Optional.of(existSlot));

        createdReservation = Reservation.builder()
                .reservationId(nonExistId)
                .reservationDate(nonExistentDate)
                .user(existUser)
                .place(existPlace)
                .slot(existSlot)
                .build();

        reservationExist = Reservation.builder()
                .reservationId(existId)
                .reservationDate(existDate)
                .user(existUser)
                .slot(existSlot)
                .place(existPlace)
                .build();

        testReservationList = new ArrayList<>();
        testReservationList.add(reservationExist);
        testReservationList.add(createdReservation);

        reservationUpdateDto = ReservationUpdateDeleteDto.builder()
                .reservationId(existId)
                .reservationDate(nonExistentDate)
                .userId(existId)
                .slotId(existId)
                .placeId(existId)
                .build();
        reservationDeleteDto = reservationUpdateDto;
    }

    @Test
    void create_shouldReturnCreatedReservationId_Test() {
        when(reservationRepository.findByDatePlaceAndSlot(reservationNewCreateDto.getReservationDate(),
                reservationNewCreateDto.getPlaceId(),
                reservationNewCreateDto.getSlotId())).thenReturn(Optional.empty());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(createdReservation);

        assertThat(reservationService.create(reservationNewCreateDto)).isEqualTo(createdReservation.getReservationId());
    }

    @Test
    void create_shouldReturnException_Test() {
        when(reservationRepository.findByDatePlaceAndSlot(reservationNewCreateDto.getReservationDate(),
                reservationNewCreateDto.getPlaceId(),
                reservationNewCreateDto.getSlotId())).thenReturn(Optional.of(reservationExist));

        assertThatThrownBy(() -> reservationService.create(reservationNewCreateDto))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Резервирование не возможно, измените дату,место или время.");
    }

    @Test
    void update_shouldReturnTrueAfterUpdate_Test() {
        when(reservationRepository.findByDatePlaceAndSlot(reservationUpdateDto.reservationDate(),
                reservationUpdateDto.placeId(),
                reservationUpdateDto.slotId())).thenReturn(Optional.empty());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(createdReservation);

        assertThat(reservationService.update(reservationUpdateDto)).isTrue();
    }

    @Test
    void update_shouldReturnException_duplicateRecord_Test() {
        when(reservationRepository.findByDatePlaceAndSlot(reservationUpdateDto.reservationDate(),
                reservationUpdateDto.placeId(),
                reservationUpdateDto.slotId())).thenReturn(Optional.of(reservationExist));

        assertThatThrownBy(() -> reservationService.update(reservationUpdateDto))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Обновление не возможно, дата, место или слот заняты.");
    }

    @Test
    void delete_shouldReturnTrue_afterDeleteReservation_Test() {
        when(reservationRepository.findByDatePlaceAndSlot(reservationDeleteDto.reservationDate(),
                reservationDeleteDto.placeId(),
                reservationDeleteDto.slotId())).thenReturn(Optional.of(reservationExist));
        when(reservationRepository.findById(reservationDeleteDto.placeId())).thenReturn(Optional.empty());

        assertThat(reservationService.delete(reservationDeleteDto)).isTrue();
    }

    @Test
    void delete_shouldReturnException_haveNoReservationForDelete_Test() {
        when(reservationRepository.findByDatePlaceAndSlot(reservationDeleteDto.reservationDate(),
                reservationDeleteDto.placeId(),
                reservationDeleteDto.slotId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.delete(reservationDeleteDto))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронь для удаления не найдена!");
    }

    @Test
    void findById_shouldReturnOptionalReservation_Test() {
        when(reservationRepository.findById(existId)).thenReturn(Optional.of(reservationExist));
        assertThat(reservationService.findById(existId).isPresent()).isTrue();
    }

    @Test
    void findById_shouldReturnOptionalEmpty_Test() {
        when(reservationRepository.findById(existId)).thenReturn(Optional.empty());
        assertThat(reservationService.findById(existId).isPresent()).isFalse();
    }

    @Test
    void findAll_shouldReturnDtoList_Test() {
        when(reservationRepository.findAll()).thenReturn(testReservationList);
        assertThat(reservationService.findAll().size()).isEqualTo(testReservationList.size());
    }

    @Test
    void findAll_shouldReturnException_Test() {
        when(reservationRepository.findAll()).thenReturn(List.of());
        assertThatThrownBy(() -> reservationService.findAll())
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("База броней пуста!");
    }

    @Test
    void findByDatePlaceAndSlot_shouldReturnTrue_andOptionalDto_Test() {
        when(reservationRepository.findByDatePlaceAndSlot(reservationExist.getReservationDate(),
                reservationExist.getPlace().getPlaceId(),
                reservationExist.getSlot().getSlotId())).thenReturn(Optional.of(reservationExist));

        assertThat(reservationService.findByDatePlaceAndSlot(reservationExist.getReservationDate(),
                reservationExist.getPlace().getPlaceId(),
                reservationExist.getSlot().getSlotId()).isPresent()).isTrue();
    }

    @Test
    void findByDatePlaceAndSlot_shouldReturnFalse_andOptionalEmpty_Test() {
        when(reservationRepository.findByDatePlaceAndSlot(reservationExist.getReservationDate(),
                reservationExist.getPlace().getPlaceId(),
                reservationExist.getSlot().getSlotId())).thenReturn(Optional.empty());

        assertThat(reservationService.findByDatePlaceAndSlot(reservationExist.getReservationDate(),
                reservationExist.getPlace().getPlaceId(),
                reservationExist.getSlot().getSlotId()).isPresent()).isFalse();
    }

    @Test
    void findByUserId_shouldReturnReservationList_Test() {
        when(reservationRepository.findByUserId(existId)).thenReturn(Optional.of(testReservationList));
        Optional<List<ReservationReadDto>> mayBeList = reservationService.findByUserId(existId);
        if (mayBeList.isPresent()) {
            assertThat(mayBeList.get().size()).isEqualTo(testReservationList.size());
        }
    }

    @Test
    void findByUserId_shouldReturnException_Test() {
        when(reservationRepository.findByUserId(existId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.findByUserId(existId))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронирования для пользователя с ID - " + existId + " отсутствуют!");
    }

    @Test
    void findBySlotId_shouldReturnReservationList_Test() {
        when(reservationRepository.findBySlotId(existId)).thenReturn(Optional.of(testReservationList));
        Optional<List<ReservationReadDto>> mayBeList = reservationService.findBySlotId(existId);
        if (mayBeList.isPresent()) {
            assertThat(mayBeList.get().size()).isEqualTo(testReservationList.size());
        }
    }

    @Test
    void findBySlotId_shouldReturnException_Test() {
        when(reservationRepository.findBySlotId(existId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.findBySlotId(existId))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронирования для " + existId + " отсутствуют!");
    }

    @Test
    void findByPlaceId_shouldReturnReservationList_Test() {
        when(reservationRepository.findByPlaceId(existId)).thenReturn(Optional.of(testReservationList));
        Optional<List<ReservationReadDto>> mayBeList = reservationService.findByPlaceId(existId);
        if (mayBeList.isPresent()) {
            assertThat(mayBeList.get().size()).isEqualTo(testReservationList.size());
        }
    }

    @Test
    void findByPlaceId_shouldReturnException_Test() {
        when(reservationRepository.findByPlaceId(existId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.findByPlaceId(existId))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронирования для " + existId + " отсутствуют!");
    }

    @Test
    void findByDate_shouldReturnReservationList_Test() {
        when(reservationRepository.findByDate(existDate)).thenReturn(Optional.of(testReservationList));
        Optional<List<ReservationReadDto>> mayBeList = reservationService.findByDate(existDate);
        if (mayBeList.isPresent()) {
            assertThat(mayBeList.get().size()).isEqualTo(testReservationList.size());
        }
    }

    @Test
    void findByDate_shouldReturnException_Test() {
        when(reservationRepository.findByDate(existDate)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reservationService.findByDate(existDate))
                .isInstanceOf(ReservationServiceException.class)
                .hasMessageContaining("Бронирования на " + existDate + " отсутствуют!");
    }

    @Test
    void findAllFreeSlotsByDate_shouldReturnFreeSlotCount_Test() {
        /* Готовим тестовые данные */
        testPlaceList = List.of(Place.builder().placeId(1L).build(), Place.builder().placeId(2L).build());
        testSlotList = List.of(Slot.builder().slotId(1L).build(), Slot.builder().slotId(2L).build());
        reservationByDateList = List.of(reservationExist);

        /* Мокаем зависимости */
        when(reservationRepository.findByDate(existDate)).thenReturn(Optional.of(reservationByDateList));
        when(slotRepository.findAll()).thenReturn(testSlotList);
        when(placeRepository.findAll()).thenReturn(testPlaceList);

        /* Запрашиваем тестируемый метод */
        Map<Long, List<Long>> allFreeSlotsByDateAndPlace = reservationService.findAllFreeSlotsByDate(existDate);

        /* Сначала получаем Map соответственно рабочим местам - их в тесте 2 */
        assertThat(allFreeSlotsByDateAndPlace.size()).isEqualTo(2);

        /* Теперь извлекаем списки свободных слотов для каждого конкретного рабочего места из Map по ключу */
        int freeSlotCount = 0;
        for(Long placeId: allFreeSlotsByDateAndPlace.keySet()){
            freeSlotCount = freeSlotCount + allFreeSlotsByDateAndPlace.get(placeId).size();
        }

        /*
            Рабочих тестовых мест 2-а, слотов тестовых 2-а, т.е. если нет броней на конкретную
            дату, то свободных слотов 4-и. В нашем случае на тестируемую дату есть одна бронь,
            т.е. свободных слотов должно остаться 3-и - проверяем.
        */
        assertThat(freeSlotCount).isEqualTo(3);
    }
}