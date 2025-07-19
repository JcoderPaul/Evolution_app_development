package me.oldboy.integration.controllers.admin_scope;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.controllers.admin_scope.AdminSlotController;
import me.oldboy.dto.jwt.JwtAuthRequest;
import me.oldboy.dto.jwt.JwtAuthResponse;
import me.oldboy.dto.slots.SlotCreateDeleteDto;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.services.SlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class AdminSlotControllerIT extends TestContainerInit {

    @Autowired
    private AdminSlotController slotController;
    @Autowired
    private SlotService slotService;
    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private JwtAuthRequest adminJwtRequest;
    private String goodJwtToken, notValidToken;
    private SlotReadUpdateDto updateSlotDto, updateNotExistentSlotDto, updateNonValidSlotDto,
            updateDuplicateSlotDto, updateInCorrectTimeRangeDto, updateOverlapTimeRangeDto;
    private SlotCreateDeleteDto createNewSlotDto, createNotValidSlotDto, createDuplicateNumberSlotDto,
            createInCorrectTimeRangeDto, createOverlapTimeRangeDto,
            deleteSlotDto, deleteNotValidSlotDto, deleteNonExistentSlotDto;
    private String BEARER_PREFIX = "Bearer ";
    private String HEADER_NAME = "Authorization";

    @BeforeEach
    @SneakyThrows
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        adminJwtRequest = JwtAuthRequest.builder()
                .login("Admin")
                .password("1234")
                .build();

        updateSlotDto = SlotReadUpdateDto.builder()
                .slotId(1L)
                .slotNumber(1015)
                .timeStart(LocalTime.of(10, 15, 00))
                .timeFinish(LocalTime.of(10, 45, 00))
                .build();
        updateNotExistentSlotDto = SlotReadUpdateDto.builder()
                .slotId(23L)
                .slotNumber(2300)
                .timeStart(LocalTime.of(23, 00, 00))
                .timeFinish(LocalTime.of(00, 00, 00))
                .build();
        updateNonValidSlotDto = SlotReadUpdateDto.builder()
                .slotId(7L)
                .slotNumber(-10)
                .build();
        updateDuplicateSlotDto = SlotReadUpdateDto.builder()
                .slotId(1L)
                .slotNumber(11)
                .timeStart(LocalTime.of(10, 05, 00))
                .timeFinish(LocalTime.of(10, 55, 00))
                .build();
        updateInCorrectTimeRangeDto = SlotReadUpdateDto.builder()
                .slotId(2L)
                .slotNumber(1125)
                .timeStart(LocalTime.of(11, 45, 00))
                .timeFinish(LocalTime.of(11, 25, 00))
                .build();
        updateOverlapTimeRangeDto = SlotReadUpdateDto.builder()
                .slotId(8L)
                .slotNumber(1745)
                .timeStart(LocalTime.of(17, 45, 00))
                .timeFinish(LocalTime.of(18, 15, 00))
                .build();

        createNewSlotDto = SlotCreateDeleteDto.builder()
                .slotNumber(19)
                .timeStart(LocalTime.of(19, 00, 00))
                .timeFinish(LocalTime.of(20, 00, 00))
                .build();
        createDuplicateNumberSlotDto = SlotCreateDeleteDto.builder()
                .slotNumber(10)
                .timeStart(LocalTime.of(10, 00, 00))
                .timeFinish(LocalTime.of(11, 00, 00))
                .build();
        createNotValidSlotDto = SlotCreateDeleteDto.builder()
                .slotNumber(-2)
                .timeStart(LocalTime.of(10, 00, 00))
                .timeFinish(LocalTime.of(11, 00, 00))
                .build();
        createInCorrectTimeRangeDto = SlotCreateDeleteDto.builder()
                .slotNumber(20)
                .timeStart(LocalTime.of(20, 45, 00))
                .timeFinish(LocalTime.of(20, 15, 00))
                .build();
        createOverlapTimeRangeDto = SlotCreateDeleteDto.builder()
                .slotNumber(18)
                .timeStart(LocalTime.of(18, 45, 00))
                .timeFinish(LocalTime.of(19, 15, 00))
                .build();

        deleteSlotDto = createDuplicateNumberSlotDto;
        deleteNotValidSlotDto = createNotValidSlotDto;
        deleteNonExistentSlotDto = createNewSlotDto;

        String requestBodyData = objectMapper.writeValueAsString(adminJwtRequest);

        /* Имитируем запрос */
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyData))
                .andExpect(status().isOk())
                .andReturn();

        /* Получим результат запроса, обработаем и извлекаем токен */
        String strReturn = result.getResponse().getContentAsString();
        JwtAuthResponse jwtAuthResponse = objectMapper.readValue(strReturn, JwtAuthResponse.class);
        goodJwtToken = BEARER_PREFIX + jwtAuthResponse.getAccessToken();
        notValidToken = "not_valid_token";
    }

    /* --- Тестируем метод создания "слота времени" для резервирования --- */
    @Test
    @SneakyThrows
    void createNewSlot_shouldReturnCreatedDto_Test() {
        String forCreateSlot = objectMapper.writeValueAsString(createNewSlotDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/create")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreateSlot))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        SlotReadUpdateDto SlotReadUpdateDto = objectMapper.readValue(strReturn, SlotReadUpdateDto.class);

        assertThat(createNewSlotDto.slotNumber()).isEqualTo(SlotReadUpdateDto.slotNumber());
        assertThat(createNewSlotDto.timeStart()).isEqualTo(SlotReadUpdateDto.timeStart());
        assertThat(createNewSlotDto.timeFinish()).isEqualTo(SlotReadUpdateDto.timeFinish());
    }

    @Test
    @SneakyThrows
    void createNewSlot_shouldReturnException_tryToCreateDuplicateNumber_Test() {
        String forCreateSlot = objectMapper.writeValueAsString(createDuplicateNumberSlotDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/create")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Слот с номером '" + createDuplicateNumberSlotDto.slotNumber() + "' уже существует!\"}"));
    }

    @Test
    @SneakyThrows
    void createNewSlot_shouldReturnValidationError_Test() {
        String forCreateSlot = objectMapper.writeValueAsString(createNotValidSlotDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/create")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(" \"slotNumber\" : " +
                        "\"Slot number can not be blank/null/negative, it must be greater than or equal to 0\"")));
    }

    @Test
    @SneakyThrows
    void createNewSlot_shouldReturnException_inCorrectTimeRange_Test() {
        String forCreateSlot = objectMapper.writeValueAsString(createInCorrectTimeRangeDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/create")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .string(containsString("\"exceptionMsg\":\"Время начала: " + createInCorrectTimeRangeDto.timeStart() +
                                " не может быть установлено позже времени окончания слота: " + createInCorrectTimeRangeDto.timeFinish() + "\"")));
    }

    @Test
    @SneakyThrows
    void createNewSlot_shouldReturnException_overlapTimeRange_Test() {
        String forCreateSlot = objectMapper.writeValueAsString(createOverlapTimeRangeDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/create")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .string(containsString("Конфликт временного диапазона слота бронирования!")));
    }

    @Test
    @SneakyThrows
    void createNewSlot_shouldReturnForbidden_notValidToken_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/create")
                        .header(HEADER_NAME, notValidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void createNewSlot_shouldReturnForbidden_notAdminAuth_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    /* --- Тестируем метод обновления слота времени --- */
    @Test
    @SneakyThrows
    void updateSlot_shouldReturnOk_AfterUpdate_Test() {
        String forUpdateSlot = objectMapper.writeValueAsString(updateSlotDto);
        Optional<SlotReadUpdateDto> beforeUpdateSlot = slotService.findById(updateSlotDto.slotId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/update")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdateSlot))
                .andExpect(status().isOk());

        Optional<SlotReadUpdateDto> afterUpdateSlot = slotService.findById(updateSlotDto.slotId());

        if (beforeUpdateSlot.isPresent() && afterUpdateSlot.isPresent()) {
            assertThat(beforeUpdateSlot.get().slotNumber()).isNotEqualTo(updateSlotDto.slotNumber());

            assertThat(afterUpdateSlot.get().slotNumber()).isEqualTo(updateSlotDto.slotNumber());
            assertThat(afterUpdateSlot.get().timeStart()).isEqualTo(updateSlotDto.timeStart());
            assertThat(afterUpdateSlot.get().timeFinish()).isEqualTo(updateSlotDto.timeFinish());
        }
    }

    @Test
    @SneakyThrows
    void updateSlot_shouldReturnException_nonExistentSlotUpdate_Test() {
        String forUpdateSlot = objectMapper.writeValueAsString(updateNotExistentSlotDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/update")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Слот с ID = " +
                        updateNotExistentSlotDto.slotId() + " не найден!\"}"));
    }

    @Test
    @SneakyThrows
    void updateSlot_shouldReturnException_duplicateSlotNumberUpdate_Test() {
        String forUpdateSlot = objectMapper.writeValueAsString(updateDuplicateSlotDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/update")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Слот с номером '" +
                        updateDuplicateSlotDto.slotNumber() + "' уже существует!\"}"));
    }

    @Test
    @SneakyThrows
    void updateSlot_shouldReturnException_inCorrectTimeRange_Test() {
        String forUpdateSlot = objectMapper.writeValueAsString(updateInCorrectTimeRangeDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/update")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Время начала: " + updateInCorrectTimeRangeDto.timeStart() +
                        " не может быть установлено позже времени окончания слота: " + updateInCorrectTimeRangeDto.timeFinish() + "\"}"));
    }

    @Test
    @SneakyThrows
    void updateSlot_shouldReturnException_overlapTimeRange_Test() {
        String forUpdateSlot = objectMapper.writeValueAsString(updateOverlapTimeRangeDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/update")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Обновить временной диапазон можно только в переделах текущего!\"}"));
    }

    @Test
    @SneakyThrows
    void updateSlot_shouldReturnValidationError_Test() {
        String forCreateSlot = objectMapper.writeValueAsString(updateNonValidSlotDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/update")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreateSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(" \"slotNumber\" : " +
                        "\"Slot number can not be blank/null/negative, it must be greater than or equal to 0\"")));
    }

    @Test
    @SneakyThrows
    void updateSlot_shouldReturnForbidden_notValidToken_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/update")
                        .header(HEADER_NAME, notValidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void updateSlot_shouldReturnForbidden_notAdminAuth_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/update")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    /* --- Тестируем метод удаления слота времени --- */
    @Test
    @SneakyThrows
    void deleteSlot_shouldReturn_okAndTrueAfterDelete_Test() {
        String forDeleteSlot = objectMapper.writeValueAsString(deleteSlotDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/delete")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeleteSlot))
                .andExpect(status().isOk());

        Optional<SlotReadUpdateDto> afterDeleteSlot = slotService.findSlotByNumber(deleteSlotDto.slotNumber());

        assertThat(afterDeleteSlot.isEmpty()).isTrue();
    }

    @Test
    @SneakyThrows
    void deleteSlot_shouldReturnException_canNotDeleteNotExistentSlot_Test() {
        String forDeleteSlot = objectMapper.writeValueAsString(deleteNonExistentSlotDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/delete")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeleteSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Слот для удаления не найден!\"}"));
    }

    @Test
    @SneakyThrows
    void deleteSlot_shouldReturnValidationError_Test() {
        String forDeleteSlot = objectMapper.writeValueAsString(deleteNotValidSlotDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/delete")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeleteSlot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(" \"slotNumber\" : " +
                        "\"Slot number can not be blank/null/negative, it must be greater than or equal to 0\"")));
    }

    @Test
    @SneakyThrows
    void deleteSlot_shouldReturnForbidden_notValidToken_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/delete")
                        .header(HEADER_NAME, notValidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void deleteSlot_shouldReturnForbidden_notAdminAuth_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/delete")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }
}