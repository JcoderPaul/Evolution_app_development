package me.oldboy.unit.services;

import me.oldboy.dto.slots.SlotCreateDeleteDto;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.exception.slot_exception.SlotServiceException;
import me.oldboy.mapper.SlotMapper;
import me.oldboy.models.entity.Slot;
import me.oldboy.repository.SlotRepository;
import me.oldboy.services.SlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SlotServiceTest {

    @Mock
    private SlotRepository mockSlotRepository;
    @InjectMocks
    private SlotService slotService;

    @Captor
    private ArgumentCaptor<Slot> slotCaptor;

    private SlotCreateDeleteDto createNormalDto, createDtoWithTimeOverlapConflict,
            createDuplicateDto, createWithTimeRangeErrorDto;
    private SlotReadUpdateDto updateNormalDto, updateTimeOverlapConflictDto,
            updateDuplicateDto, updateTimeRangeErrorDto;
    private Slot createSlot, deleteSlot, updateSlot, capturedSlot;
    private List<Slot> controlList;
    private LocalTime startTime, endTime;
    private long existId, nonExistentId;
    private int slotNumber;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existId = 1L;
        nonExistentId = 10L;
        slotNumber = 13;

        controlList = new ArrayList<>();
        controlList.add(Slot.builder().slotId(1L).slotNumber(10).timeStart(LocalTime.of(10, 00, 00)).timeFinish(LocalTime.of(11, 00, 00)).build());
        controlList.add(Slot.builder().slotId(2L).slotNumber(11).timeStart(LocalTime.of(11, 00, 00)).timeFinish(LocalTime.of(12, 00, 00)).build());
        controlList.add(Slot.builder().slotId(3L).slotNumber(12).timeStart(LocalTime.of(12, 00, 00)).timeFinish(LocalTime.of(13, 00, 00)).build());

        startTime = LocalTime.of(13, 00, 00);
        endTime = LocalTime.of(14, 00, 00);

        createSlot = Slot.builder().slotId(existId).slotNumber(slotNumber).timeStart(startTime).timeFinish(endTime).build();
        deleteSlot = createSlot;
        updateSlot = createSlot;

        createNormalDto = SlotCreateDeleteDto.builder().slotNumber(slotNumber).timeStart(startTime).timeFinish(endTime).build();
        createDtoWithTimeOverlapConflict = SlotCreateDeleteDto.builder()
                .slotNumber(13)
                .timeStart(startTime.minus(100, ChronoUnit.MINUTES))
                .timeFinish(endTime.minus(100, ChronoUnit.MINUTES))
                .build();
        createDuplicateDto = SlotCreateDeleteDto.builder().slotNumber(10).timeStart(startTime).timeFinish(endTime).build();
        createWithTimeRangeErrorDto = SlotCreateDeleteDto.builder().slotNumber(14).timeStart(endTime).timeFinish(startTime).build();

        updateNormalDto = SlotReadUpdateDto.builder()
                .slotNumber(slotNumber + 1)
                .timeStart(startTime.plus(10, ChronoUnit.MINUTES))
                .timeFinish(endTime.minus(10, ChronoUnit.MINUTES))
                .build();
        updateTimeOverlapConflictDto = SlotReadUpdateDto.builder()
                .slotNumber(slotNumber + 1)
                .timeStart(startTime.minus(10, ChronoUnit.MINUTES))
                .timeFinish(endTime.plus(10, ChronoUnit.MINUTES))
                .build();
        updateTimeRangeErrorDto = SlotReadUpdateDto.builder().slotNumber(slotNumber + 1).timeStart(endTime).timeFinish(startTime).build();
        updateDuplicateDto = updateNormalDto;
    }

    @Test
    void create_shouldReturnSavedSlotId_Test() {
        Slot createForSaveSlot = SlotMapper.INSTANCE.mapToEntity(createNormalDto);
        when(mockSlotRepository.save(createForSaveSlot)).thenReturn(createSlot);

        assertThat(slotService.create(createNormalDto)).isEqualTo(createSlot.getSlotId());
    }

    @Test
    void create_shouldReturnException_TimeOverlapConflict_Test() {
        when(mockSlotRepository.findAll()).thenReturn(controlList);

        assertThatThrownBy(() -> slotService.create(createDtoWithTimeOverlapConflict))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Конфликт временного диапазона слота бронирования!");
    }

    @Test
    void create_shouldReturnException_duplicateSlotNumber_Test() {
        Slot foundSlot = SlotMapper.INSTANCE.mapToEntity(createDuplicateDto);
        when(mockSlotRepository.findBySlotNumber(createDuplicateDto.slotNumber())).thenReturn(Optional.of(foundSlot));

        assertThatThrownBy(() -> slotService.create(createDuplicateDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Слот с номером '" + createDuplicateDto.slotNumber() + "' уже существует!");
    }

    @Test
    void create_shouldReturnException_timeRangeError_Test() {
        assertThatThrownBy(() -> slotService.create(createWithTimeRangeErrorDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Время начала: " + createWithTimeRangeErrorDto.timeStart() +
                        " не может быть установлено позже времени окончания слота: " + createWithTimeRangeErrorDto.timeFinish());
    }

    @Test
    void findById_shouldReturnFoundSlot_Test() {
        when(mockSlotRepository.findById(createSlot.getSlotId())).thenReturn(Optional.of(createSlot));
        Optional<SlotReadUpdateDto> slotReadUpdateDto = slotService.findById(createSlot.getSlotId());
        if (slotReadUpdateDto.isPresent()) {
            assertThat(slotReadUpdateDto.get().slotNumber()).isEqualTo(createSlot.getSlotNumber());
            assertThat(slotReadUpdateDto.get().timeStart()).isEqualTo(createSlot.getTimeStart());
            assertThat(slotReadUpdateDto.get().timeFinish()).isEqualTo(createSlot.getTimeFinish());
        }
    }

    @Test
    void findById_shouldReturnOptionalEmpty_Test() {
        when(mockSlotRepository.findById(createSlot.getSlotId())).thenReturn(Optional.empty());
        Optional<SlotReadUpdateDto> slotReadUpdateDto = slotService.findById(createSlot.getSlotId());

        assertThat(slotReadUpdateDto.isEmpty()).isTrue();
    }

    @Test
    void findAll_shouldReturnSlotList_Test() {
        when(mockSlotRepository.findAll()).thenReturn(controlList);
        List<SlotReadUpdateDto> foundList = slotService.findAll();

        assertThat(foundList.size()).isEqualTo(controlList.size());
    }

    @Test
    void findSlotByNumber_shouldReturnFoundSlot_Test() {
        when(mockSlotRepository.findBySlotNumber(createSlot.getSlotNumber())).thenReturn(Optional.of(createSlot));
        Optional<SlotReadUpdateDto> slotReadUpdateDto = slotService.findSlotByNumber(createSlot.getSlotNumber());

        if (slotReadUpdateDto.isPresent()) {
            assertThat(slotReadUpdateDto.get().slotNumber()).isEqualTo(createSlot.getSlotNumber());
            assertThat(slotReadUpdateDto.get().timeStart()).isEqualTo(createSlot.getTimeStart());
            assertThat(slotReadUpdateDto.get().timeFinish()).isEqualTo(createSlot.getTimeFinish());
        }
    }

    @Test
    void findSlotByNumber_shouldReturnOptionalEmpty_Test() {
        when(mockSlotRepository.findBySlotNumber(createSlot.getSlotNumber())).thenReturn(Optional.empty());

        assertThat(slotService.findSlotByNumber(createSlot.getSlotNumber()).isEmpty()).isTrue();
    }

    @Test
    void delete_shouldReturnTrue_Test() {
        when(mockSlotRepository.findById(deleteSlot.getSlotId())).thenReturn(Optional.of(deleteSlot));

        assertThat(slotService.delete(deleteSlot.getSlotId())).isTrue();
    }

    @Test
    void delete_shouldReturnException_Test() {
        when(mockSlotRepository.findById(deleteSlot.getSlotId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.delete(deleteSlot.getSlotId()))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Slot with id - " + deleteSlot.getSlotId() + " not found!");
    }

    @Test
    void update_shouldReturnTrue_Test() {
        when(mockSlotRepository.findById(updateNormalDto.slotId())).thenReturn(Optional.of(updateSlot));

        assertThat(slotService.update(updateNormalDto)).isTrue();

        /* Хотим проверить, что действительно "прилетает" на вход метода *.save() класса SlotRepository */
        verify(mockSlotRepository).save(slotCaptor.capture());
        capturedSlot = slotCaptor.getValue();

        assertThat(capturedSlot.getSlotNumber()).isEqualTo(updateNormalDto.slotNumber());
        assertThat(capturedSlot.getTimeStart()).isEqualTo(updateNormalDto.timeStart());
        assertThat(capturedSlot.getTimeFinish()).isEqualTo(updateNormalDto.timeFinish());
    }

    @Test
    void update_shouldReturnException_timeOverlap_Test() {
        when(mockSlotRepository.findById(updateTimeOverlapConflictDto.slotId())).thenReturn(Optional.of(updateSlot));

        assertThatThrownBy(() -> slotService.update(updateTimeOverlapConflictDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Обновить временной диапазон можно только в переделах текущего!");
    }

    @Test
    void update_shouldReturnException_timeRangeError_Test() {
        when(mockSlotRepository.findById(updateTimeRangeErrorDto.slotId())).thenReturn(Optional.of(updateSlot));

        assertThatThrownBy(() -> slotService.update(updateTimeRangeErrorDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Время начала: " +
                        updateTimeRangeErrorDto.timeStart() +
                        " не может быть установлено позже времени окончания слота: " +
                        updateTimeRangeErrorDto.timeFinish());
    }

    @Test
    void update_shouldReturnException_duplicateSlotNumber_Test() {
        when(mockSlotRepository.findById(updateDuplicateDto.slotId())).thenReturn(Optional.of(updateSlot));
        when(mockSlotRepository.findBySlotNumber(updateDuplicateDto.slotNumber())).thenReturn(Optional.of(updateSlot));

        assertThatThrownBy(() -> slotService.update(updateDuplicateDto))
                .isInstanceOf(SlotServiceException.class)
                .hasMessageContaining("Слот с номером '" + updateDuplicateDto.slotNumber() + "' уже существует!");
    }
}