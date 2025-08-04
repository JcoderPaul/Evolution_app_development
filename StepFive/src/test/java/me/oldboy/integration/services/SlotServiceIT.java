package me.oldboy.integration.services;

import me.oldboy.dto.slots.SlotCreateDeleteDto;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.exception.slot_exception.SlotServiceException;
import me.oldboy.integration.ITBaseStarter;
import me.oldboy.services.SlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlotServiceIT extends ITBaseStarter {

    @Autowired
    private SlotService slotService;

    private SlotCreateDeleteDto slotCreateNewDto, slotCreateDuplicateNumberDto,
            slotCreateTimeOverlapDto, slotCreateTimeRangeConflictDto;
    private SlotReadUpdateDto slotUpdateDto, slotUpdateWithExistNumberDto,
            slotUpdateWithTimeRangeConflictDto, slotUpdateTimeOverlapDto;
    private Long existId, nonExistId;
    private Integer existNumber, nonExistNumber;
    private LocalTime startTime, finishTime;

    @BeforeEach
    void setUp() {
        existId = 1L;
        nonExistId = 100L;

        existNumber = 10;
        nonExistNumber = 100;

        startTime = LocalTime.of(10, 00, 00);
        finishTime = LocalTime.of(11, 00, 00);

        slotCreateNewDto = SlotCreateDeleteDto.builder()
                .slotNumber(existNumber + 9)
                .timeStart(startTime.plus(9, ChronoUnit.HOURS))
                .timeFinish(finishTime.plus(9, ChronoUnit.HOURS))
                .build();
        slotCreateTimeOverlapDto = SlotCreateDeleteDto.builder()
                .slotNumber(1015)
                .timeStart(startTime.plus(15, ChronoUnit.MINUTES))
                .timeFinish(finishTime.plus(15, ChronoUnit.MINUTES))
                .build();
        slotCreateDuplicateNumberDto = SlotCreateDeleteDto.builder()
                .slotNumber(18)
                .timeStart(startTime.plus(8, ChronoUnit.HOURS))
                .timeFinish(finishTime.plus(8, ChronoUnit.HOURS))
                .build();
        slotCreateTimeRangeConflictDto = SlotCreateDeleteDto.builder().slotNumber(19).timeStart(finishTime).timeFinish(startTime).build();

        slotUpdateDto = SlotReadUpdateDto.builder()
                .slotId(existId)
                .slotNumber(existNumber + 10)
                .timeStart(startTime.plus(10, ChronoUnit.MINUTES))
                .timeFinish(finishTime.minus(10, ChronoUnit.MINUTES))
                .build();
        slotUpdateTimeOverlapDto = SlotReadUpdateDto.builder()
                .slotId(existId)
                .slotNumber(existNumber + 15)
                .timeStart(startTime.plus(15, ChronoUnit.MINUTES))
                .timeFinish(finishTime.plus(15, ChronoUnit.MINUTES))
                .build();
        slotUpdateWithTimeRangeConflictDto = SlotReadUpdateDto.builder()
                .slotId(existId)
                .slotNumber(existNumber)
                .timeStart(finishTime)
                .timeFinish(startTime)
                .build();
        slotUpdateWithExistNumberDto = SlotReadUpdateDto.builder()
                .slotId(existId + 1)
                .slotNumber(existNumber + 2)
                .timeStart(startTime.plus(1, ChronoUnit.HOURS))
                .timeFinish(finishTime.plus(1, ChronoUnit.HOURS))
                .build();
    }

    @Test
    void create_shouldReturnGeneratedId_Test() {
        Long createdId = slotService.create(slotCreateNewDto);
        assertThat(createdId).isGreaterThan(9);
    }

    @Test
    void create_shouldReturnException_timeOverlap_Test() {
        assertThatThrownBy(() -> slotService.create(slotCreateTimeOverlapDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Конфликт временного диапазона слота бронирования!");
    }

    @Test
    void create_shouldReturnException_duplicateSlotNumber_Test() {
        assertThatThrownBy(() -> slotService.create(slotCreateDuplicateNumberDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Слот с номером '" + slotCreateDuplicateNumberDto.slotNumber() + "' уже существует!");
    }

    @Test
    void create_shouldReturnException_timeRangeConflict_Test() {
        assertThatThrownBy(() -> slotService.create(slotCreateTimeRangeConflictDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Время начала: " + slotCreateTimeRangeConflictDto.timeStart() +
                        " не может быть установлено позже времени окончания слота: " + slotCreateTimeRangeConflictDto.timeFinish());
    }

    @Test
    void findById_shouldReturnTrue_forExistSlot_Test() {
        assertThat(slotService.findById(existId).isPresent()).isTrue();
    }

    @Test
    void findById_shouldReturnFalse_forNonExistSlot_Test() {
        assertThat(slotService.findById(nonExistId).isPresent()).isFalse();
    }

    @Test
    void findAll_shouldReturnRecordList_Test() {
        assertThat(slotService.findAll().size()).isEqualTo(9);
    }

    @Test
    void findSlotByNumber_shouldReturnTrue_forExistSlotNumber_Test() {
        assertThat(slotService.findSlotByNumber(existNumber).isPresent()).isTrue();
    }

    @Test
    void findSlotByNumber_shouldReturnFalse_forNonExistSlot_Test() {
        assertThat(slotService.findSlotByNumber(nonExistNumber).isPresent()).isFalse();
    }

    @Test
    void delete_shouldReturnTrue_forExistSlot_Test() {
        assertThat(slotService.delete(existId)).isTrue();
    }

    @Test
    void delete_shouldReturnException_forNonExistSlot_Test() {
        assertThatThrownBy(() -> slotService.delete(nonExistId))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Slot with id - " + nonExistId + " not found!");
    }

    @Test
    void update_shouldReturnTrue_ifSuccessUpdate_Test() {
        assertThat(slotService.update(slotUpdateDto)).isTrue();
    }

    @Test
    void update_shouldReturnException_itTimeOverlap_Test() {
        assertThatThrownBy(() -> slotService.update(slotUpdateTimeOverlapDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Обновить временной диапазон можно только в переделах текущего!");
    }

    @Test
    void update_shouldReturnException_timeRangeConflict_Test() {
        assertThatThrownBy(() -> slotService.update(slotUpdateWithTimeRangeConflictDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Время начала: " +
                        slotUpdateWithTimeRangeConflictDto.timeStart() +
                        " не может быть установлено позже времени окончания слота: " +
                        slotUpdateWithTimeRangeConflictDto.timeFinish());
    }

    @Test
    void update_shouldReturnException_slotNumberDuplicate_Test() {
        assertThatThrownBy(() -> slotService.update(slotUpdateWithExistNumberDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Слот с номером '" + slotUpdateWithExistNumberDto.slotNumber() +
                        "' уже существует!");
    }
}