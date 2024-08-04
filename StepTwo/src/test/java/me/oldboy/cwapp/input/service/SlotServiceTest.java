package me.oldboy.cwapp.input.service;

import me.oldboy.cwapp.exceptions.services.SlotServiceException;
import me.oldboy.cwapp.input.entity.Reservation;
import me.oldboy.cwapp.input.entity.Slot;

import me.oldboy.cwapp.input.repository.crud.ReservationRepository;
import me.oldboy.cwapp.input.repository.crud.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SlotServiceTest {

    @Mock
    private SlotRepository slotRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private SlotService slotService;
    private Slot testSlotWithThreeParam;
    private Slot testSlotWithId;
    private Slot testSlotWithTimeRangeConflict;
    private Long testSlotId;
    private Integer testSlotNumber;
    private LocalTime testStartTime;
    private LocalTime testFinishTime;
    private List<Slot> testSlotList;
    private List<Slot> emptyTestSlotBase;

    @BeforeEach
    public void setUp(){
        testSlotId = 10L;
        testSlotNumber = 19;
        testStartTime = LocalTime.parse("19:00");
        testFinishTime = LocalTime.parse("20:00");
        testSlotList = List.of(
                new Slot(9L, 18, LocalTime.parse("18:00"), LocalTime.parse("19:00")),
                new Slot(11L, 20, LocalTime.parse("20:00"), LocalTime.parse("21:00")),
                new Slot(8L, 17, LocalTime.parse("17:00"), LocalTime.parse("18:00")));
        emptyTestSlotBase = List.of();
        testSlotWithThreeParam = new Slot(testSlotNumber, testStartTime, testFinishTime);
        testSlotWithId = new Slot(testSlotId, testSlotNumber, testStartTime, testFinishTime);
        testSlotWithTimeRangeConflict =
                new Slot(10L, 19, LocalTime.parse("18:58"), LocalTime.parse("20:02"));

        MockitoAnnotations.openMocks(this);
    }

    /* Тестируем метод *.createSlot() условного уровня сервисов */

    @Test
    void shouldReturnSlotId_createSlotTest() {
        when(slotRepository.findAllSlots()).thenReturn(testSlotList);
        when(slotRepository.findSlotByNumber(testSlotNumber)).thenReturn(Optional.empty());
        when(slotRepository.createSlot(testSlotWithThreeParam)).thenReturn(Optional.of(testSlotWithId));

        assertThat(slotService.createSlot(testSlotWithThreeParam))
                .isEqualTo(testSlotId);
    }

    @Test
    void shouldReturnExceptionDuplicateNumber_createSlotTest() {
        when(slotRepository.findAllSlots())
                .thenReturn(testSlotList);
        when(slotRepository.findSlotByNumber(testSlotWithThreeParam.getSlotNumber()))
                .thenReturn(Optional.of(testSlotWithId));

        assertThatThrownBy(() -> slotService.createSlot(testSlotWithThreeParam))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Возможно слот с номером " + "'" +
                        testSlotWithThreeParam.getSlotNumber() + "' и временным интервалом: " +
                        testSlotWithThreeParam.getTimeStart() + " - " +
                        testSlotWithThreeParam.getTimeFinish() + " уже существует!");
    }

    @Test
    void shouldReturnExceptionTimeRangeConflict_createSlotTest() {
        when(slotRepository.findAllSlots()).thenReturn(testSlotList);
        when(slotRepository.findSlotByNumber(testSlotWithThreeParam.getSlotNumber()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.createSlot(testSlotWithTimeRangeConflict))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Конфликт временного диапазона слота бронирования!");
    }

    /* Тестируем метод *.findAllSlots() условного уровня сервисов */

    @Test
    void shouldReturnSlotList_findAllSlotsTest() {
        when(slotRepository.findAllSlots()).thenReturn(testSlotList);
        assertThat(slotService.findAllSlots().size()).isEqualTo(3);
    }

    @Test
    void shouldReturnException_findAllSlotsTest() {
        when(slotRepository.findAllSlots()).thenReturn(emptyTestSlotBase);
        assertThatThrownBy(() -> slotService.findAllSlots())
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("База слотов для бронирования пуста");
    }
    /* Тестируем метод *.findSlotById() условного уровня сервисов */

    @Test
    void shouldReturnSlot_findSlotByIdTest() {
        when(slotRepository.findSlotById(testSlotId)).thenReturn(Optional.of(testSlotWithId));
        assertThat(slotService.findSlotById(testSlotId)).isEqualTo(testSlotWithId);
    }

    @Test
    void shouldReturnExceptionIfSlotIdNotFind_findSlotByIdTest() {
        when(slotRepository.findSlotById(testSlotId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.findSlotById(testSlotId))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Слот с ID - " + testSlotId + " не найден!");
    }

    /* Тестируем метод *.testFindSlotById() условного уровня сервисов */

    @Test
    void shouldReturnSlotNumber_findSlotByNumberTest() {
        when(slotRepository.findSlotByNumber(testSlotNumber))
                .thenReturn(Optional.of(testSlotWithThreeParam));
        assertThat(slotService.findSlotByNumber(testSlotNumber))
                .isEqualTo(testSlotWithThreeParam);
    }

    @Test
    void shouldReturnExceptionIfHaveNoSlotWithDefiniteNumber_findSlotByNumberTest() {
        when(slotRepository.findSlotByNumber(testSlotNumber))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.findSlotByNumber(testSlotNumber))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Слот с номером - " + testSlotNumber + " не найден!");
    }

    /* Тестируем метод *.updateSlot() условного уровня сервисов */

    @Test
    void shouldReturnTrue_updateSlotTest() {
        Integer newSlotNumber = 193;
        String newSlotTimeStart = "19:01";
        String newSlotTimeFinish = "19:30";
        testSlotWithId.setSlotNumber(newSlotNumber);
        testSlotWithId.setTimeStart(LocalTime.parse(newSlotTimeStart));
        testSlotWithId.setTimeFinish(LocalTime.parse(newSlotTimeFinish));
        when(slotRepository.findSlotById(testSlotId)).thenReturn(Optional.of(testSlotWithId));
        when(slotRepository.updateSlot(testSlotWithId)).thenReturn(true);

        assertAll(
                () -> assertThat(slotService.updateSlot(testSlotWithId))
                        .isTrue(),
                () -> assertThat(slotService.findSlotById(testSlotId).getSlotNumber())
                        .isEqualTo(newSlotNumber),
                () -> assertThat(slotService.findSlotById(testSlotId).getTimeStart())
                        .isEqualTo(newSlotTimeStart),
                () -> assertThat(slotService.findSlotById(testSlotId).getTimeFinish())
                        .isEqualTo(newSlotTimeFinish)
        );
    }

    @Test
    void shouldReturnExceptionHaveNoSlotId_updateSlotTest() {
        when(slotRepository.findSlotById(testSlotId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.updateSlot(testSlotWithId))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Слот - '" + testSlotWithId.getSlotNumber() +
                                                "': " + testSlotWithId.getTimeStart() +
                                                " - " + testSlotWithId.getTimeFinish() +
                                                " нельзя обновить, т.к. слот не существует!");
    }

    @Test
    void shouldReturnExceptionTimeRangeConflict_updateSlotTest() {
        when(slotRepository.findSlotById(testSlotWithTimeRangeConflict.getSlotId()))
                .thenReturn(Optional.of(testSlotWithTimeRangeConflict));
        when(slotRepository.findAllSlots()).thenReturn(testSlotList);
        assertThatThrownBy(() -> slotService.updateSlot(testSlotWithTimeRangeConflict))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Конфликт временного диапазона при обновлении слота!");
    }

    /* Тестируем метод *.deleteSlot() условного уровня сервисов */

    @Test
    void shouldReturnTrue_deleteSlotTest() {
        when(reservationRepository.findReservationBySlotId(testSlotId)).thenReturn(Optional.empty());
        when(slotRepository.findSlotById(testSlotId)).thenReturn(Optional.of(testSlotWithId));
        when(slotRepository.deleteSlot(testSlotId)).thenReturn(true);
        assertThat(slotService.deleteSlot(testSlotWithId.getSlotId())).isTrue();
    }

    @Test
    void shouldReturnExceptionDeletingNonExistentSlot_deleteSlotTest() {
        when(reservationRepository.findReservationBySlotId(testSlotId)).thenReturn(Optional.empty());
        when(slotRepository.findSlotById(testSlotId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.deleteSlot(testSlotId))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Удаление несуществующего слота невозможно!");
    }

    @Test
    void shouldReturnExceptionDeletingReservationSlot_deleteSlotTest() {
        when(reservationRepository.findReservationBySlotId(testSlotId))
                .thenReturn(Optional.of(List.of(new Reservation(), new Reservation())));
        when(slotRepository.findSlotById(testSlotId))
                .thenReturn(Optional.of(testSlotWithId));
        assertThatThrownBy(() -> slotService.deleteSlot(testSlotId))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Удаление зарезервированного слота невозможно!");
    }
}