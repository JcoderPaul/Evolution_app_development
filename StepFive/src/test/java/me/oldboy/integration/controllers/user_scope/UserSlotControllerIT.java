package me.oldboy.integration.controllers.user_scope;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.integration.ITBaseStarter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
class UserSlotControllerIT extends ITBaseStarter {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;
    private Long existId, nonExistentId;
    private Integer existNumber, nonExistentNumber;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        existId = 1L;
        nonExistentId = 100L;

        existNumber = 10;
        nonExistentNumber = 23;
    }

    /* ----- Тестируем *.readSlotById() ----- */
    @Test
    @SneakyThrows
    void readSlotById_shouldReturnFoundSlot_Test() {
        MvcResult result = mockMvc.perform(get("/api/slots/id/{slotId}", existId))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        SlotReadUpdateDto savedSlotDto = objectMapper.readValue(strReturn, SlotReadUpdateDto.class);

        assertThat(savedSlotDto.slotId()).isEqualTo(existId);
    }

    @Test
    @SneakyThrows
    void readSlotById_shouldReturnException_noPLaceNoFound_Test() {
        mockMvc.perform(get("/api/slots/id/{slotId}", nonExistentId))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"exceptionMsg\":\"Слот с ID: " + nonExistentId + " не существует!\"}"));
    }

    /* ----- Тестируем *.readSlotByNumber() ----- */
    @Test
    @SneakyThrows
    void readSlotByNumber_shouldReturnFoundSlot_Test() {
        MvcResult result = mockMvc.perform(get("/api/slots/number/{slotNumber}", existNumber))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        SlotReadUpdateDto savedSlotDto = objectMapper.readValue(strReturn, SlotReadUpdateDto.class);

        assertThat(savedSlotDto.slotNumber()).isEqualTo(existNumber);
    }

    @Test
    @SneakyThrows
    void readSlotByNumber_shouldReturnException_noNumberNoFound_Test() {
        mockMvc.perform(get("/api/slots/number/{slotNumber}", nonExistentNumber))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"exceptionMsg\":\"Слот с номером: " + nonExistentNumber + " не найден!\"}"));
    }

    /* ----- Тестируем *.getAllSlots() ----- */
    @Test
    @SneakyThrows
    void getAllSlots_shouldReturnSlotList_Test() {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/slots"))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<SlotReadUpdateDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<SlotReadUpdateDto>>() {
        });

        assertThat(listFromResponse.size()).isEqualTo(9);
    }
}