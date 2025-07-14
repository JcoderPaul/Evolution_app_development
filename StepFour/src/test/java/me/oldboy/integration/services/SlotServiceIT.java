package me.oldboy.integration.services;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.slots.SlotCreateDeleteDto;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.exception.slot_exception.SlotServiceException;
import me.oldboy.integration.annotation.IT;
import me.oldboy.services.SlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IT
class SlotServiceIT extends TestContainerInit {

    @Autowired
    private SlotService slotService;

    private SlotCreateDeleteDto createCorrectDto, createOverlapTimeProblemDto, createExistNumberDto, createWrongTimeRangeDto;
    private SlotReadUpdateDto updateSlotDto, updateTimeProblemDto, updateExistNumberDto, updateWrongTimeRangeDto;
    private Long existId, notExistentId;
    private Integer existNumber, nonExistentNumber;

    @BeforeEach
    void setUp(){
        existId = 1L;
        notExistentId = 100L;

        existNumber = 10;
        nonExistentNumber = 1010;

        /* Данные для тестирования создания слота */
        createCorrectDto = SlotCreateDeleteDto.builder()    // Правильный формат
                .slotNumber(19)
                .timeStart(LocalTime.of(19,00,00))
                .timeFinish(LocalTime.of(20,00,00))
                .build();
        createOverlapTimeProblemDto = SlotCreateDeleteDto.builder() // Перехлест времени с уже существующим диапазоном
                .slotNumber(1830)
                .timeStart(LocalTime.of(18,30,00))
                .timeFinish(LocalTime.of(20,00,00))
                .build();
        createExistNumberDto = SlotCreateDeleteDto.builder()    // Слот с заданным номером существует в БД
                .slotNumber(10)
                .timeStart(LocalTime.of(9,15,00))
                .timeFinish(LocalTime.of(9,45,00))
                .build();
        createWrongTimeRangeDto = SlotCreateDeleteDto.builder() // Временной диапазон начинается позже чем заканчивается
                .slotNumber(9)
                .timeStart(LocalTime.of(9,45,00))
                .timeFinish(LocalTime.of(9,15,00))
                .build();

        /* Данные для тестирования обновление слотов */
        updateSlotDto = updateSlotDto.builder()// Правильный формат
                .slotId(9L)
                .slotNumber(1815)
                .timeStart(LocalTime.of(18,15,00))
                .timeFinish(LocalTime.of(18,45,00))
                .build();
        updateTimeProblemDto = updateSlotDto.builder()    // Выход за пределы существующего слота
                .slotId(7L)
                .slotNumber(1630)
                .timeStart(LocalTime.of(16,30,00))
                .timeFinish(LocalTime.of(17,30,00))
                .build();
        updateWrongTimeRangeDto = updateSlotDto.builder()    // Начало диапазона не может быть позже его окончания
                .slotId(4L)
                .slotNumber(1315)
                .timeStart(LocalTime.of(13,45,00))
                .timeFinish(LocalTime.of(13,15,00))
                .build();
        updateExistNumberDto = updateSlotDto.builder()    // Старый ID и старый Number - непорядок, новый диапазон - новый Number
                .slotId(4L)
                .slotNumber(15)
                .timeStart(LocalTime.of(13,10,00))
                .timeFinish(LocalTime.of(13,30,00))
                .build();
    }

    @Test
    void create_shouldReturnCreatedSlotId_Test() {
        Long createdSlotId = slotService.create(createCorrectDto);
        assertThat(createdSlotId).isNotNull();
        assertThat(createdSlotId).isGreaterThan(9);
    }

    @Test
    void create_shouldReturnException_TimeProblemOverlapExistentTimeSlot_Test() {
        assertThatThrownBy(() -> slotService.create(createOverlapTimeProblemDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Конфликт временного диапазона слота бронирования!");
    }

    @Test
    void create_shouldReturnException_NumberSlotConflict_Test() {
        assertThatThrownBy(() -> slotService.create(createExistNumberDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Слот с номером '" + createExistNumberDto.slotNumber() +
                        "' уже существует!");
    }

    @Test
    void create_shouldReturnException_TimeRangeProblem_Test() {
        assertThatThrownBy(() -> slotService.create(createWrongTimeRangeDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Время начала: " + createWrongTimeRangeDto.timeStart() +
                        " не может быть установлено позже времени окончания слота: " + createWrongTimeRangeDto.timeFinish());
    }

    @Test
    void findById_shouldReturnFindReadDto_Test() {
        Optional<SlotReadUpdateDto> mayBeSlot = slotService.findById(existId);
        if(mayBeSlot.isPresent()){
            assertThat(mayBeSlot.get().slotId()).isEqualTo(existId);
        }
    }

    @Test
    void findById_shouldReturnOptionalEmpty_Test() {
        Optional<SlotReadUpdateDto> mayBeSlot = slotService.findById(notExistentId);
        assertThat(mayBeSlot.isEmpty()).isTrue();
    }

    @Test
    void findAll_shouldReturnReadDtoList_Test() {
        List<SlotReadUpdateDto> dtoList = slotService.findAll();
        assertThat(dtoList.size()).isEqualTo(9);
    }

    @Test
    void findSlotByNumber_shouldReturnFoundSlot_Test() {
        Optional<SlotReadUpdateDto> mayBeSlot = slotService.findSlotByNumber(existNumber);
        if(mayBeSlot.isPresent()){
            assertThat(mayBeSlot.get().slotNumber()).isEqualTo(existNumber);
        }
    }

    @Test
    void findSlotByNumber_shouldReturnOptionalEmpty_Test() {
        Optional<SlotReadUpdateDto> mayBeSlot = slotService.findSlotByNumber(nonExistentNumber);
        assertThat(mayBeSlot.isPresent()).isFalse();
    }

    @Test
    void delete_shouldReturnTrue_afterDelete_Test() {
        boolean isSlotDeleted = slotService.delete(existId);
        assertThat(isSlotDeleted).isTrue();
    }

    @Test
    void delete_shouldReturnException_afterTryNotExistentSlotDelete_Test() {
        assertThatThrownBy(() -> slotService.delete(notExistentId))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Slot with id - " + notExistentId + " not found!");
    }

    @Test
    void update_shouldReturnTrue_IfSuccessSave_Test() {
        boolean isUpdated = slotService.update(updateSlotDto);
        assertThat(isUpdated).isTrue();
    }

    @Test
    void update_shouldReturnException_TimeProblem_Test() {
        assertThatThrownBy(() -> slotService.update(updateTimeProblemDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Обновить временной диапазон можно только в переделах текущего!");
    }

    @Test
    void update_shouldReturnException_wrongTimeRange_Test() {
        assertThatThrownBy(() -> slotService.update(updateWrongTimeRangeDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Время начала: " + updateWrongTimeRangeDto.timeStart() +
                        " не может быть установлено позже времени окончания слота: " + updateWrongTimeRangeDto.timeFinish());
    }

    @Test
    void update_shouldReturnException_duplicateNumber_Test() {
        assertThatThrownBy(() -> slotService.update(updateExistNumberDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Слот с номером '" + updateExistNumberDto.slotNumber() + "' уже существует!");
    }
}