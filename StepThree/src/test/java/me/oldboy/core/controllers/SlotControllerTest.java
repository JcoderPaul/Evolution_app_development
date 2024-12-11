package me.oldboy.core.controllers;

import me.oldboy.core.dto.slots.SlotCreateDeleteDto;
import me.oldboy.core.dto.slots.SlotReadUpdateDto;
import me.oldboy.core.model.service.SlotService;
import me.oldboy.exception.SlotControllerException;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SlotControllerTest {

    @Mock
    private SlotService slotService;
    @InjectMocks
    private SlotController slotController;

    private static String testUserName;
    private static SlotCreateDeleteDto createDto, deleteDto;
    private static SlotReadUpdateDto readDto, updateDto;
    private static Long existId, notExistId;

    @BeforeAll
    public static void initParam(){
        existId = 1L;
        notExistId = 10L;
        createDto = new SlotCreateDeleteDto(20, LocalTime.parse("20:00"), LocalTime.parse("21:00"));
        deleteDto = createDto;
        readDto = new SlotReadUpdateDto(existId, 1, LocalTime.parse("10:00"), LocalTime.parse("11:00"));
        updateDto = readDto;
        testUserName = "Admin";
    }

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    /* Блок тестов - выделим каждую группу тестов, если это необходимо, в отдельный вложенный класс, для удобства */

    /* Тесты метода *.createNewSlot() */

    @Test
    @DisplayName("1 - SlotController class *.createNewSlot method test")
    void shouldReturnCreatedSlotDto_createNewSlotTest() {
        when(slotService.create(createDto)).thenReturn(notExistId);
        when(slotService.findById(notExistId)).thenReturn(Optional.of(readDto));

        assertThat(slotController.createNewSlot(createDto, testUserName)).isEqualTo(readDto);

        verify(slotService, times(1)).create(any(SlotCreateDeleteDto.class));
        verify(slotService, times(1)).findById(anyLong());
    }

    /* Тесты метода *.readSlotById() */

    @Nested
    @DisplayName("2 - SlotController class *.readSlotById method tests")
    class ReadSlotByIdMethodTests {

        @Test
        void shouldReturnReadingSlotDto_readSlotByIdTest() {
            when(slotService.findById(existId)).thenReturn(Optional.of(readDto));

            try {
                assertThat(slotController.readSlotById(existId)).isEqualTo(readDto);
            } catch (SlotControllerException e) {
                throw new RuntimeException(e);
            }

            verify(slotService, times(1)).findById(anyLong());
        }

        @Test
        void shouldThrowExceptionHaveNoSlot_readSlotByIdTest() {
            when(slotService.findById(notExistId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> slotController.readSlotById(notExistId))
                    .isInstanceOf(SlotControllerException.class)
                    .hasMessageContaining("Slot with ID: " + notExistId + " not exist! " +
                            "Слот с ID: " + notExistId + " не существует!");

            verify(slotService, times(1)).findById(anyLong());
        }
    }

    /* Тесты метода *.readSlotByNumber() */

    @Nested
    @DisplayName("3 - SlotController class *.readSlotByNumber method tests")
    class ReadSlotByNumberMethodTests {

        @Test
        void shouldReturnFindSlotDto_readSlotByNumberTest() {
            when(slotService.findSlotByNumber(readDto.slotNumber())).thenReturn(Optional.of(readDto));

            try {
                assertThat(slotController.readSlotByNumber(readDto.slotNumber())).isEqualTo(readDto);
            } catch (SlotControllerException e) {
                throw new RuntimeException(e);
            }

            verify(slotService, times(1)).findSlotByNumber(anyInt());
        }

        @Test
        void shouldThrowExceptionNotFindSlotByNumber_readSlotByNumberTest() {
            when(slotService.findSlotByNumber(readDto.slotNumber())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> slotController.readSlotByNumber(readDto.slotNumber()))
                    .isInstanceOf(SlotControllerException.class)
                    .hasMessageContaining("Slot with " + readDto.slotNumber() + " number not found! " +
                            "Слот с номером " + readDto.slotNumber() + " не найден!");

            verify(slotService, times(1)).findSlotByNumber(anyInt());
        }
    }

    /* Тесты метода *.getAllSlots() */

    @Test
    @DisplayName("4 - SlotController class *.getAllSlots method test")
    void shouldReturnSizeOfSlotList_getAllSlotsTest() {
        when(slotService.findAll())
                .thenReturn(List.of(new SlotReadUpdateDto(1L, 10, LocalTime.parse("10:00"), LocalTime.parse("11:00")),
                                    new SlotReadUpdateDto(2L, 11, LocalTime.parse("11:00"), LocalTime.parse("12:00"))));

        assertThat(slotController.getAllSlots().size()).isEqualTo(2);

        verify(slotService, times(1)).findAll();
    }

    /* Тесты метода *.updateSlot() */

    @Nested
    @DisplayName("5 - SlotController class *.updateSlot method tests")
    class UpdateSlotMethodTests {

        @Test
        void shouldReturnTrueUpdateSuccess_updateSlotTest() {
            when(slotService.findById(updateDto.slotId())).thenReturn(Optional.of(updateDto));
            when(slotService.update(updateDto.slotId(), updateDto)).thenReturn(true);

            try {
                assertThat(slotController.updateSlot(updateDto, testUserName)).isTrue();
            } catch (SlotControllerException e) {
                throw new RuntimeException(e);
            }

            verify(slotService, times(1)).findById(anyLong());
            verify(slotService, times(1)).update(anyLong(), any(SlotReadUpdateDto.class));
        }

        @Test
        void shouldThrowExceptionUpdateFail_updateSlotTest() {
            when(slotService.findById(updateDto.slotId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> slotController.updateSlot(updateDto, testUserName))
                    .isInstanceOf(SlotControllerException.class)
                    .hasMessageContaining("Slot with ID = " + updateDto.slotId() + " not existent! " +
                            "Слот с ID = " + updateDto.slotId() + " не найден!");

            verify(slotService, times(1)).findById(anyLong());
        }
    }

    /* Тесты метода *.deleteSlot() */

    @Nested
    @DisplayName("6 - SlotController class *.deleteSlot method tests")
    class DeleteSlotMethodTests {

        @Test
        void shouldReturnTrueDeleteSuccess_deleteSlotTest() {
            when(slotService.findSlotByNumber(deleteDto.slotNumber())).thenReturn(Optional.of(readDto));
            when(slotService.delete(readDto.slotId())).thenReturn(true);

            try {
                assertThat(slotController.deleteSlot(deleteDto, testUserName)).isTrue();
            } catch (SlotControllerException e) {
                throw new RuntimeException(e);
            }

            verify(slotService, times(1)).findSlotByNumber(anyInt());
            verify(slotService, times(1)).delete(readDto.slotId());
        }

        @Test
        void shouldThrowExceptionFalseDeleteFail_deleteSlotTest() {
            when(slotService.findSlotByNumber(deleteDto.slotNumber())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> slotController.deleteSlot(deleteDto, testUserName))
                    .isInstanceOf(SlotControllerException.class)
                    .hasMessageContaining("Have no slot to delete! Слот для удаления не найден!");

            verify(slotService, times(1)).findSlotByNumber(anyInt());
        }
    }
}