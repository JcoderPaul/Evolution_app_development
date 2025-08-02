package me.oldboy.unit.controllers.web_mvc.user_scope;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.auditor.core.repository.AuditRepository;
import me.oldboy.controllers.user_scope.UserSlotController;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.repository.PlaceRepository;
import me.oldboy.repository.ReservationRepository;
import me.oldboy.repository.SlotRepository;
import me.oldboy.repository.UserRepository;
import me.oldboy.services.SlotService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserSlotController.class)
@WithMockUser()
class MVCUserSlotControllerTest {

    @MockBean
    private SlotService slotService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private SlotRepository slotRepository;
    @MockBean
    private ReservationRepository reservationRepository;
    @MockBean
    private PlaceRepository placeRepository;
    @MockBean
    private AuditRepository auditRepository;

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;

    /* Мы можем обойтись парой переменных, но для наглядности создадим полный набор */
    private Long existId, nonExistentId;
    private Integer existNumber, nonExistentNumber;

    private SlotReadUpdateDto slotReadDto;
    private List<SlotReadUpdateDto> testReadDtoList;

    @BeforeAll
    static void setStaticContent() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setUp() {
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
    @SneakyThrows
    void readSlotById_shouldReturnFoundSlot_Test() {
        when(slotService.findById(existId)).thenReturn(Optional.of(slotReadDto));

        MvcResult result = mockMvc.perform(get("/api/slots/id/{slotId}", existId))
                .andExpect(status().isOk())
                .andReturn();

        /* Получим результат запроса, обработаем до нужного формата и сравним с исходным */
        String strReturn = result.getResponse().getContentAsString();
        SlotReadUpdateDto savedSlotDto = objectMapper.readValue(strReturn, SlotReadUpdateDto.class);

        assertAll(
                () -> assertThat(savedSlotDto.slotId()).isEqualTo(slotReadDto.slotId()),
                () -> assertThat(savedSlotDto.slotNumber()).isEqualTo(slotReadDto.slotNumber()),
                () -> assertThat(savedSlotDto.timeStart()).isEqualTo(slotReadDto.timeStart()),
                () -> assertThat(savedSlotDto.timeFinish()).isEqualTo(slotReadDto.timeFinish())
        );

        verify(slotService, times(1)).findById(anyLong());
    }

    @Test
    @SneakyThrows
    void readSlotById_shouldReturnException_slotIdNotFound_Test() {
        when(slotService.findById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/slots/id/{slotId}", nonExistentId))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"exceptionMsg\":\"Слот с ID: " + nonExistentId + " не существует!\"}"));

        verify(slotService, times(1)).findById(anyLong());
    }

    @Test
    @SneakyThrows
    void readSlotByNumber_shouldReturnFoundSlot_andResponseStatusOk_Test() {
        when(slotService.findSlotByNumber(existNumber)).thenReturn(Optional.of(slotReadDto));

        MvcResult result = mockMvc.perform(get("/api/slots/number/{slotNumber}", existNumber))
                .andExpect(status().isOk())
                .andReturn();

        /* Получим результат запроса, обработаем до нужного формата и сравним с исходным */
        String strReturn = result.getResponse().getContentAsString();
        SlotReadUpdateDto savedSlotDto = objectMapper.readValue(strReturn, SlotReadUpdateDto.class);

        assertAll(
                () -> assertThat(savedSlotDto.slotId()).isEqualTo(slotReadDto.slotId()),
                () -> assertThat(savedSlotDto.slotNumber()).isEqualTo(slotReadDto.slotNumber()),
                () -> assertThat(savedSlotDto.timeStart()).isEqualTo(slotReadDto.timeStart()),
                () -> assertThat(savedSlotDto.timeFinish()).isEqualTo(slotReadDto.timeFinish())
        );

        verify(slotService, times(1)).findSlotByNumber(anyInt());
    }

    @Test
    @SneakyThrows
    void readSlotByNumber_shouldReturnBadRequest_notFoundSlotByNumber_Test() {
        when(slotService.findSlotByNumber(nonExistentNumber)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/slots/number/{slotNumber}", nonExistentNumber))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"exceptionMsg\":\"Слот с номером: " + nonExistentNumber + " не найден!\"}"));

        verify(slotService, times(1)).findSlotByNumber(anyInt());
    }

    @Test
    @SneakyThrows
    void getAllSlots_shouldReturnReadDtoList_Test() {
        when(slotService.findAll()).thenReturn(testReadDtoList);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/slots"))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<SlotReadUpdateDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<SlotReadUpdateDto>>() {
        });

        assertThat(listFromResponse.size()).isEqualTo(testReadDtoList.size());
    }
}