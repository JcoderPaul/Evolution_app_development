package me.oldboy.integration.controllers.user_scope;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.entity.options.Species;
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

/* В текущих тестах мы не будем использовать генерацию токена, используем @WithUserDetails */
@IT
class UserPlaceControllerIT extends TestContainerInit {

    @Autowired
    private WebApplicationContext applicationContext;
    private MockMvc mockMvc;
    private Long existId, nonExistentId;
    private Integer existNumber, nonExistentNumber;
    private Species existInBaseSpecies, nonExistentSpecies;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();

        objectMapper = new ObjectMapper();

        existId = 1L;
        nonExistentId = 100L;

        existNumber = 1;
        nonExistentNumber = 100;

        existInBaseSpecies = Species.HALL;
        nonExistentSpecies = Species.STUDIO;
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void readPlaceById_shouldReturnFindDto_Test() {
        MvcResult result = mockMvc.perform(get("/api/places/{placeId}", existId))    // Можно так
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        PlaceReadUpdateDto readUpdateDto = objectMapper.readValue(strReturn, PlaceReadUpdateDto.class);

        assertThat(readUpdateDto).isNotNull();
        assertThat(readUpdateDto.placeId()).isEqualTo(existId);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void readPlaceById_shouldReturnException_nonExistentId_Test() {
        mockMvc.perform(get("/api/places/" + nonExistentId)) // Можно и так
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Конференц-зала / рабочего места с ID: " + nonExistentId + " не существует!\"}"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void readPlaceBySpeciesAndNumber_shouldReturnReadDto_Test() {
        MvcResult result = mockMvc.perform(get("/api/places/species/" + existInBaseSpecies + "/number/" + existNumber))  // Еще вариант, см. ниже
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        PlaceReadUpdateDto readUpdateDto = objectMapper.readValue(strReturn, PlaceReadUpdateDto.class);

        assertThat(readUpdateDto).isNotNull();
        assertThat(readUpdateDto.species().name()).isEqualTo(existInBaseSpecies.name());
        assertThat(readUpdateDto.placeNumber()).isEqualTo(existNumber);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void readPlaceBySpeciesAndNumber_shouldReturnException_unExpectedSpecies_Test() {
        mockMvc.perform(get("/api/places/species/{species}/number/{placeNumber}",nonExistentSpecies, existNumber))   // Или так,тоже работает
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"'" + nonExistentSpecies + "' - с номером '" + existNumber + "' не существует!\"}"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void readPlaceBySpeciesAndNumber_shouldReturnException_unExpectedNumber_Test() {
        mockMvc.perform(get("/api/places/species/" + existInBaseSpecies + "/number/" + nonExistentNumber))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"'" + existInBaseSpecies + "' - с номером '" + nonExistentNumber + "' не существует!\"}"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void getAllPlaces_shouldReturnDtoList_Test() {
        MvcResult result = mockMvc.perform(get("/api/places"))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        List<PlaceReadUpdateDto> placeDtoList = objectMapper.readValue(strReturn, new TypeReference<List<PlaceReadUpdateDto>>() {});

        assertThat(placeDtoList).isNotNull();
        assertThat(placeDtoList.size()).isGreaterThan(8);
    }
}