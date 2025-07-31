package me.oldboy.unit.controllers.only_mock.user_scope;

import me.oldboy.controllers.user_scope.UserSlotController;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.exception.slot_exception.SlotControllerException;
import me.oldboy.services.SlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OMUserSlotControllerTest {

    @Mock
    private SlotService slotService;
    @InjectMocks
    private UserSlotController slotController;

    /* Мы можем обойтись парой переменных, но для наглядности создадим полный набор */
    private Long existId, nonExistentId;
    private Integer existNumber, nonExistentNumber;

    private SlotReadUpdateDto slotReadDto;
    private List<SlotReadUpdateDto> testReadDtoList;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        existId = 1L;
        nonExistentId = 100L;

        existNumber = 10;
        nonExistentNumber = 23;

        slotReadDto = SlotReadUpdateDto.builder().slotId(existId).slotNumber(existNumber).build();

        testReadDtoList = List.of(
                SlotReadUpdateDto.builder().slotId(existId).build(),
                SlotReadUpdateDto.builder().slotId(nonExistentId).build()
        );
    }

    @Test
    void readSlotById_shouldReturnFoundSlot_Test() {
        when(slotService.findById(existId)).thenReturn(Optional.of(slotReadDto));

        ResponseEntity<?> response = slotController.readSlotById(existId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(slotReadDto);

        verify(slotService, times(1)).findById(anyLong());
    }

    @Test
    void readSlotById_shouldReturnException_slotIdNotFound_Test() {
        when(slotService.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotController.readSlotById(nonExistentId))
                .isInstanceOf(SlotControllerException.class)
                .hasMessageContaining("Слот с ID: " + nonExistentId + " не существует!");

        verify(slotService, times(1)).findById(anyLong());
    }

    @Test
    void readSlotByNumber_shouldReturnFoundSlot_andResponseStatusOk_Test() {
        when(slotService.findSlotByNumber(existNumber)).thenReturn(Optional.of(slotReadDto));

        ResponseEntity<?> response = slotController.readSlotByNumber(existNumber);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(slotReadDto);

        verify(slotService,times(1)).findSlotByNumber(anyInt());
    }

    @Test
    void readSlotByNumber_shouldReturnBadRequest_notFoundSlotByNumber_Test() {
        when(slotService.findSlotByNumber(nonExistentNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotController.readSlotByNumber(nonExistentNumber))
                .isInstanceOf(SlotControllerException.class)
                .hasMessageContaining("Слот с номером: " + nonExistentNumber + " не найден!");

        verify(slotService,times(1)).findSlotByNumber(anyInt());
    }

    @Test
    void getAllSlots_shouldReturnReadDtoList_Test() {
        when(slotService.findAll()).thenReturn(testReadDtoList);

        assertThat(slotController.getAllSlots().size()).isEqualTo(testReadDtoList.size());
    }
}