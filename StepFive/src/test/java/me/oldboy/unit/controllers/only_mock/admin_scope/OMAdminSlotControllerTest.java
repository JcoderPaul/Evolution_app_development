package me.oldboy.unit.controllers.only_mock.admin_scope;

import me.oldboy.controllers.admin_scope.AdminSlotController;
import me.oldboy.dto.slots.SlotCreateDeleteDto;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.exception.slot_exception.SlotControllerException;
import me.oldboy.services.SlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;


class OMAdminSlotControllerTest {

    @Mock
    private SlotService slotService;
    @InjectMocks
    private AdminSlotController slotController;

    private SlotCreateDeleteDto slotCreateDto, slotDeleteDto;
    private SlotReadUpdateDto slotReadDto, slotUpdateDto;
    private Long existId, nonExistId;
    private Integer existNumber, nonExistNumber;
    private LocalTime startTime, finishTime;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existId = 1L;
        nonExistId = 100L;

        existNumber = 10;
        nonExistNumber = 19;

        startTime = LocalTime.of(19, 00, 00);
        finishTime = LocalTime.of(20, 00, 00);

        slotCreateDto = SlotCreateDeleteDto.builder()
                .slotNumber(nonExistNumber)
                .timeStart(startTime)
                .timeFinish(finishTime)
                .build();
        slotReadDto = SlotReadUpdateDto.builder()
                .slotId(nonExistId)
                .slotNumber(nonExistNumber)
                .timeStart(startTime)
                .timeFinish(finishTime)
                .build();
        slotUpdateDto = slotReadDto;
        slotDeleteDto = slotCreateDto;
    }

    @Test
    void createNewSlot_shouldReturnCreatedDto_Test() {
        when(slotService.create(slotCreateDto)).thenReturn(nonExistId);
        when(slotService.findById(nonExistId)).thenReturn(Optional.of(slotReadDto));

        assertThat(slotController.createNewSlot(slotCreateDto)).isEqualTo(slotReadDto);

        verify(slotService, times(1)).create(any(SlotCreateDeleteDto.class));
        verify(slotService, times(1)).findById(anyLong());
    }

    @Test
    void updateSlot_shouldReturnTrue_successUpdate_Test() {
        when(slotService.findById(slotUpdateDto.slotId())).thenReturn(Optional.of(slotReadDto));
        when(slotService.update(slotUpdateDto)).thenReturn(true);

        assertThat(slotController.updateSlot(slotUpdateDto)).isTrue();

        verify(slotService, times(1)).update(any(SlotReadUpdateDto.class));
        verify(slotService, times(1)).findById(anyLong());
    }

    @Test
    void updateSlot_shouldReturnException_haveNoSlotId_Test() {
        when(slotService.findById(slotUpdateDto.slotId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotController.updateSlot(slotUpdateDto))
                .isInstanceOf(SlotControllerException.class)
                .hasMessageContaining("Слот с ID = " + slotUpdateDto.slotId() + " не найден!");

        verify(slotService, times(1)).findById(anyLong());
    }

    @Test
    void deleteSlot_shouldReturnTrue_successDelete_Test() {
        when(slotService.findSlotByNumber(slotDeleteDto.slotNumber())).thenReturn(Optional.of(slotReadDto));
        when(slotService.delete(slotReadDto.slotId())).thenReturn(true);

        assertThat(slotController.deleteSlot(slotDeleteDto)).isTrue();

        verify(slotService, times(1)).delete(anyLong());
        verify(slotService, times(1)).findSlotByNumber(anyInt());
    }

    @Test
    void deleteSlot_shouldReturnException_naveNoSlotForDelete_Test() {
        when(slotService.findSlotByNumber(slotDeleteDto.slotNumber())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotController.deleteSlot(slotDeleteDto))
                .isInstanceOf(SlotControllerException.class)
                .hasMessageContaining("Слот для удаления не найден!");

        verify(slotService, times(1)).findSlotByNumber(anyInt());
    }
}