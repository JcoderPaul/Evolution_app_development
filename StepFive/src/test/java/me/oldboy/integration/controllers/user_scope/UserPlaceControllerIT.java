package me.oldboy.integration.controllers.user_scope;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.integration.ITBaseStarter;
import me.oldboy.models.entity.options.Species;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
class UserPlaceControllerIT extends ITBaseStarter {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private Long existId, nonExistentId;
    private Integer existNumber, nonExistentNumber;
    private Species existInBaseSpecies, noExistInBaseSpecies;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        existId = 1L;
        nonExistentId = 100L;

        existNumber = 1;
        nonExistentNumber = 100;

        existInBaseSpecies = Species.HALL;
        noExistInBaseSpecies = Species.STUDIO;
    }

    /* ----- Тестируем *.readPlaceById() ----- */
    @Test
    @SneakyThrows
    void readPlaceById_shouldReturnFoundPlace_Test() {
        MvcResult result = mockMvc.perform(get("/api/places/{placeId}", existId))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        PlaceReadUpdateDto savedPlaceDto = objectMapper.readValue(strReturn, PlaceReadUpdateDto.class);

        assertAll(
                () -> assertThat(savedPlaceDto.placeNumber()).isEqualTo(existNumber),
                () -> assertThat(savedPlaceDto.species()).isEqualTo(existInBaseSpecies)
        );
    }

    @Test
    @SneakyThrows
    void readPlaceById_shouldReturnException_negativeId_Test() {
        mockMvc.perform(get("/api/places/{placeId}", -existId))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"exceptionMsg\":\"Идентификатор не может быть отрицательным!\"}"));
    }

    @Test
    @SneakyThrows
    void readPlaceById_shouldReturnException_noPlaceNoFound_Test() {
        mockMvc.perform(get("/api/places/{placeId}", nonExistentId))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"exceptionMsg\":\"Конференц-зала / рабочего места с ID: " + nonExistentId + " не существует!\"}"));
    }

    /* ----- Тестируем *.readPlaceBySpeciesAndNumber() ----- */
    @Test
    @SneakyThrows
    void readPlaceBySpeciesAndNumber_shouldReturnFoundPlace_Test() {
        MvcResult result = mockMvc.perform(get("/api/places/species/{species}/number/{placeNumber}", existInBaseSpecies, existNumber))
                .andExpect(status().isOk())
                .andReturn();

        /* Получим результат запроса, обработаем до нужного формата и сравним с исходным */
        String strReturn = result.getResponse().getContentAsString();
        PlaceReadUpdateDto savedPlaceDto = objectMapper.readValue(strReturn, PlaceReadUpdateDto.class);

        assertAll(
                () -> assertThat(savedPlaceDto.placeNumber()).isEqualTo(existNumber),
                () -> assertThat(savedPlaceDto.species()).isEqualTo(existInBaseSpecies)
        );
    }

    @Test
    @SneakyThrows
    void readPlaceBySpeciesAndNumber_shouldReturnException_haveNoPlaceWithParam_Test() {
        mockMvc.perform(get("/api/places/species/{species}/number/{placeNumber}", noExistInBaseSpecies, nonExistentNumber))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"exceptionMsg\":\"'" + noExistInBaseSpecies +
                        "' - с номером '" + nonExistentNumber + "' не существует!\"}"));
    }

    /* ----- Тестируем *.getAllPlaces() ----- */
    @Test
    @SneakyThrows
    void getAllPlaces_shouldReturnPlaceList_Test() {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/places"))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<PlaceReadUpdateDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<PlaceReadUpdateDto>>() {
        });

        assertThat(listFromResponse.size()).isEqualTo(9);
    }
}