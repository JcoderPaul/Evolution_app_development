package me.oldboy.unit.controllers.web_mvc.admin_scope;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.auditor.core.repository.AuditRepository;
import me.oldboy.controllers.admin_scope.AdminSlotController;
import me.oldboy.dto.slots.SlotCreateDeleteDto;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebMvcTest(AdminSlotController.class)
@WithMockUser(authorities = {"ADMIN"})
class MVCAdminSlotControllerTest {

    @MockBean
    private SlotService slotService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ReservationRepository reservationRepository;
    @MockBean
    private PlaceRepository placeRepository;
    @MockBean
    private SlotRepository slotRepository;
    @MockBean
    private AuditRepository auditRepository;

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;

    private SlotCreateDeleteDto slotCreateValidDto, slotDeleteDto, slotCreateNonValidDto;
    private SlotReadUpdateDto slotReadDto, slotUpdateDto, slotNonValidUpdateDto;
    private Long existId, nonExistId;
    private Integer existNumber, nonExistNumber;
    private LocalTime startTime, finishTime;

    @BeforeAll
    static void setStaticContent() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setUp() {
        existId = 1L;
        nonExistId = 100L;

        existNumber = 10;
        nonExistNumber = 19;

        startTime = LocalTime.of(19, 00, 00);
        finishTime = LocalTime.of(20, 00, 00);

        slotCreateValidDto = SlotCreateDeleteDto.builder()
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
        slotDeleteDto = slotCreateValidDto;
        slotCreateNonValidDto = SlotCreateDeleteDto.builder().slotNumber(-10).build();
        slotNonValidUpdateDto = SlotReadUpdateDto.builder().slotId(-existId).slotNumber(-existNumber).build();
    }

    /* ------ Тестируем *.createNewSlot() ------ */
    @Test
    @SneakyThrows
    void createNewSlot_shouldReturnCreatedDto_Test() {
        when(slotService.create(slotCreateValidDto)).thenReturn(nonExistId);
        when(slotService.findById(nonExistId)).thenReturn(Optional.of(slotReadDto));

        String strDtoForCreate = objectMapper.writeValueAsString(slotCreateValidDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForCreate))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        /* Получим результат запроса, обработаем до нужного формата и сравним с исходным */
        String strReturn = result.getResponse().getContentAsString();
        SlotReadUpdateDto savedSlotDto = objectMapper.readValue(strReturn, SlotReadUpdateDto.class);

        assertAll(
                () -> assertThat(savedSlotDto.slotNumber()).isEqualTo(slotCreateValidDto.slotNumber()),
                () -> assertThat(savedSlotDto.timeStart()).isEqualTo(slotCreateValidDto.timeStart()),
                () -> assertThat(savedSlotDto.timeFinish()).isEqualTo(slotCreateValidDto.timeFinish())
        );

        verify(slotService, times(1)).create(any(SlotCreateDeleteDto.class));
        verify(slotService, times(1)).findById(anyLong());
    }

    /*
        Как и ранее мы проверим работу валидации только в одном методе - текущем, т.к. корректность
        SlotCreateDeleteDto обрабатывается одинаково и в методе создания слота и в методе удаления.
    */
    @Test
    @SneakyThrows
    void createNewSlot_shouldReturnValidationError_Test() {
        String strDtoForCreate = objectMapper.writeValueAsString(slotCreateNonValidDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForCreate))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Start time can not be null")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("Slot number can not be blank/null/negative, it must be greater than or equal to 0")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("Finish time can not be null")));
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"USER"})
    void createNewSlot_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/create"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string(""));
    }

    /* ------ Тестируем *.updateSlot() ------ */
    @Test
    @SneakyThrows
    void updateSlot_shouldReturnTrue_successUpdate_Test() {
        when(slotService.findById(slotUpdateDto.slotId())).thenReturn(Optional.of(slotReadDto));
        when(slotService.update(slotUpdateDto)).thenReturn(true);

        String strDtoForUpdate = objectMapper.writeValueAsString(slotUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(slotService, times(1)).update(any(SlotReadUpdateDto.class));
        verify(slotService, times(1)).findById(anyLong());
    }

    @Test
    @SneakyThrows
    void updateSlot_shouldReturnException_haveNoSlotId_Test() {
        when(slotService.findById(slotUpdateDto.slotId())).thenReturn(Optional.empty());

        String strDtoForUpdate = objectMapper.writeValueAsString(slotUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content()
                        .json("{\"exceptionMsg\":\"Слот с ID = " + slotUpdateDto.slotId() + " не найден!\"}"));

        verify(slotService, times(1)).findById(anyLong());
    }

    @Test
    @SneakyThrows
    void updateSlot_shouldReturnValidationError_Test() {
        String strDtoForUpdate = objectMapper.writeValueAsString(slotNonValidUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Start time can not be null")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("Slot number can not be blank/null/negative, it must be greater than or equal to 0")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("Finish time can not be null")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("SlotId can not be null/negative")));
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"USER"})
    void updateSlot_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/update"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string(""));
    }

    /* ------ Тестируем *.deleteSlot() ------ */
    @Test
    @SneakyThrows
    void deleteSlot_shouldReturnTrue_successDelete_Test() {
        when(slotService.findSlotByNumber(slotDeleteDto.slotNumber())).thenReturn(Optional.of(slotReadDto));
        when(slotService.delete(slotReadDto.slotId())).thenReturn(true);

        String strDtoForDelete = objectMapper.writeValueAsString(slotDeleteDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForDelete))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(slotService, times(1)).delete(anyLong());
        verify(slotService, times(1)).findSlotByNumber(anyInt());
    }

    @Test
    @SneakyThrows
    void deleteSlot_shouldReturnException_naveNoSlotForDelete_Test() {
        when(slotService.findSlotByNumber(slotDeleteDto.slotNumber())).thenReturn(Optional.empty());

        String strDtoForDelete = objectMapper.writeValueAsString(slotDeleteDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForDelete))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content()
                        .json("{\"exceptionMsg\":\"Слот для удаления не найден!\"}"));

        verify(slotService, times(1)).findSlotByNumber(anyInt());
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"USER"})
    void deleteSlot_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/delete"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string(""));
    }
}