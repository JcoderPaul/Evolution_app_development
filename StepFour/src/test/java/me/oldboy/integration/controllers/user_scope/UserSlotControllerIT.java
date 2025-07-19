package me.oldboy.integration.controllers.user_scope;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.integration.annotation.IT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class UserSlotControllerIT extends TestContainerInit {

    @Autowired
    private WebApplicationContext applicationContext;
    private MockMvc mockMvc;

    private Long existId, nonExistentId;
    private Integer slotNumber, nonExistentNumber;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        existId = 1L;
        nonExistentId = 100L;

        slotNumber = 10;
        nonExistentNumber = 100;
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void readSlotById_shouldReturnReadDto_Test() {
        MvcResult result = mockMvc.perform(get("/api/slots/id/{slotId}", existId))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        SlotReadUpdateDto slotReadUpdateDto = objectMapper.readValue(strReturn, SlotReadUpdateDto.class);

        assertThat(slotReadUpdateDto).isNotNull();
        assertThat(slotReadUpdateDto.slotId()).isEqualTo(existId);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void readSlotById_shouldReturnException_nonExistentId_Test() {
        mockMvc.perform(get("/api/slots/id/{slotId}", nonExistentId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Слот с ID: " + nonExistentId + " не существует!\"}"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void readSlotByNumber_shouldReturnFoundSlot_Test() {
        MvcResult result = mockMvc.perform(get("/api/slots/number/{slotNumber}", slotNumber))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        SlotReadUpdateDto slotReadUpdateDto = objectMapper.readValue(strReturn, SlotReadUpdateDto.class);

        assertThat(slotReadUpdateDto).isNotNull();
        assertThat(slotReadUpdateDto.slotNumber()).isEqualTo(slotNumber);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void readSlotByNumber_shouldReturnException_haveNoSlotNumberInBase_Test() {
        mockMvc.perform(get("/api/slots/number/{slotNumber}", nonExistentNumber))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Слот с номером: " + nonExistentNumber + " не найден!\"}"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void getAllSlots_shouldReturnDtoList_Test() {
        MvcResult result = mockMvc.perform(get("/api/slots"))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        List<SlotReadUpdateDto> placeDtoList = objectMapper.readValue(strReturn, new TypeReference<List<SlotReadUpdateDto>>() {});

        assertThat(placeDtoList).isNotNull();
        assertThat(placeDtoList.size()).isGreaterThan(8);
    }
}