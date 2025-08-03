package me.oldboy.integration.controllers.admin_scope;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.dto.slots.SlotCreateDeleteDto;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.integration.ITBaseStarter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithUserDetails(value = "Admin", userDetailsServiceBeanName = "clientDetailsService")
class AdminSlotControllerIT extends ITBaseStarter {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;
    private SlotCreateDeleteDto slotCreateValidDto, slotDeleteDto,
            slotDeleteNoValidDto, slotCreateNonValidDto, slotDeleteWithNoIdDto;
    private SlotReadUpdateDto slotUpdateDto, slotNonValidUpdateDto, slotUpdateNonExistentDto;
    private Long existId, nonExistId;
    private Integer existNumber, nonExistNumber;
    private LocalTime startTime, finishTime;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        existId = 1L;
        nonExistId = 100L;

        existNumber = 10;
        nonExistNumber = 19;

        startTime = LocalTime.of(18, 00, 00);
        finishTime = LocalTime.of(19, 00, 00);

        slotCreateValidDto = SlotCreateDeleteDto.builder()
                .slotNumber(nonExistNumber)
                .timeStart(startTime.plus(1, ChronoUnit.HOURS))
                .timeFinish(finishTime.plus(1, ChronoUnit.HOURS))
                .build();
        slotUpdateDto = SlotReadUpdateDto.builder()
                .slotId(existId)
                .slotNumber(existNumber + 15)
                .timeStart(startTime.minus(8, ChronoUnit.HOURS).plus(15, ChronoUnit.MINUTES))
                .timeFinish(finishTime.minus(8, ChronoUnit.HOURS).minus(15, ChronoUnit.MINUTES))
                .build();
        slotUpdateNonExistentDto = SlotReadUpdateDto.builder()
                .slotId(nonExistId)
                .slotNumber(existNumber)
                .timeStart(startTime)
                .timeFinish(finishTime)
                .build();
        slotDeleteDto = SlotCreateDeleteDto.builder()
                .slotNumber(existNumber)
                .timeStart(startTime.minus(8, ChronoUnit.HOURS))
                .timeFinish(finishTime.minus(8, ChronoUnit.HOURS))
                .build();
        slotDeleteWithNoIdDto = SlotCreateDeleteDto.builder()
                .slotNumber(nonExistNumber)
                .timeStart(startTime.minus(8, ChronoUnit.HOURS))
                .timeFinish(finishTime.minus(8, ChronoUnit.HOURS))
                .build();

        slotCreateNonValidDto = SlotCreateDeleteDto.builder().slotNumber(-10).build();
        slotDeleteNoValidDto = slotCreateNonValidDto;
        slotNonValidUpdateDto = SlotReadUpdateDto.builder().slotId(-existId).slotNumber(-existNumber).build();
    }

    /* ----- Тесты метода *.createNewSlot() ----- */
    @Test
    @SneakyThrows
    void createNewSlot_shouldReturnCreatedReadDto_Test() {
        String strDtoForCreate = objectMapper.writeValueAsString(slotCreateValidDto);

        MvcResult result = mockMvc.perform(post("/api/admin/slots/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForCreate))
                .andExpect(status().isOk())
                .andReturn();

        /* Получим результат запроса, обработаем до нужного формата и сравним с исходным */
        String strReturn = result.getResponse().getContentAsString();
        SlotReadUpdateDto savedSlotDto = objectMapper.readValue(strReturn, SlotReadUpdateDto.class);

        assertAll(
                () -> assertThat(savedSlotDto.slotNumber()).isEqualTo(slotCreateValidDto.slotNumber()),
                () -> assertThat(savedSlotDto.timeStart()).isEqualTo(slotCreateValidDto.timeStart()),
                () -> assertThat(savedSlotDto.timeFinish()).isEqualTo(slotCreateValidDto.timeFinish()),
                () -> assertThat(savedSlotDto.slotId()).isGreaterThan(9)
        );
    }

    @Test
    @SneakyThrows
    void createNewSlot_shouldReturnValidationError_Test() {
        String strDtoForCreate = objectMapper.writeValueAsString(slotCreateNonValidDto);

        mockMvc.perform(post("/api/admin/slots/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForCreate))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Start time can not be null")))
                .andExpect(content().string(containsString("Finish time can not be null")))
                .andExpect(content().string(containsString("Slot number can not be blank/null/negative, it must be greater than or equal to 0")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void createNewSlot_shouldReturnForbidden_Test() {
        mockMvc.perform(post("/api/admin/slots/create"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    /* ----- Тесты метода *.updateSlot() ----- */
    @Test
    @SneakyThrows
    void updateSlot_shouldReturnTrue_afterUpdate_Test() {
        String strDtoForUpdate = objectMapper.writeValueAsString(slotUpdateDto);

        mockMvc.perform(post("/api/admin/slots/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void updateSlot_shouldReturnValidationError_Test() {
        String strDtoForUpdate = objectMapper.writeValueAsString(slotNonValidUpdateDto);

        mockMvc.perform(post("/api/admin/slots/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Start time can not be null")))
                .andExpect(content().string(containsString("Slot number can not be blank/null/negative, it must be greater than or equal to 0")))
                .andExpect(content().string(containsString("SlotId can not be null/negative")))
                .andExpect(content().string(containsString("Finish time can not be null")));
    }

    @Test
    @SneakyThrows
    void updateSlot_shouldReturnException_canNotUpdateNonFoundPLace_Test() {
        String strDtoForUpdate = objectMapper.writeValueAsString(slotUpdateNonExistentDto);

        mockMvc.perform(post("/api/admin/slots/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json("{\"exceptionMsg\":\"Слот с ID = " + slotUpdateNonExistentDto.slotId() + " не найден!\"}"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void updateSlot_shouldReturnForbidden_Test() {
        mockMvc.perform(post("/api/admin/slots/update"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    /* ----- Тесты метода *.deleteSlot() ----- */
    @Test
    @SneakyThrows
    void deleteSlot_shouldReturnOk_afterRemoveSlot_Test() {
        String strDtoForDelete = objectMapper.writeValueAsString(slotDeleteDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForDelete))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @SneakyThrows
    void deleteSlot_shouldReturnValidationError_Test() {
        String strDtoForDelete = objectMapper.writeValueAsString(slotDeleteNoValidDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForDelete))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Start time can not be null")))
                .andExpect(content().string(containsString("Finish time can not be null")))
                .andExpect(content().string(containsString("Slot number can not be blank/null/negative, it must be greater than or equal to 0")));
    }

    @Test
    @SneakyThrows
    void deleteSlot_shouldReturnException_haveNoSlotForRemove_Test() {
        String strDtoForDelete = objectMapper.writeValueAsString(slotDeleteWithNoIdDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForDelete))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content()
                        .json("{\"exceptionMsg\":\"Слот для удаления не найден!\"}"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void deleteSlot_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/slots/delete"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().string(""));
    }
}