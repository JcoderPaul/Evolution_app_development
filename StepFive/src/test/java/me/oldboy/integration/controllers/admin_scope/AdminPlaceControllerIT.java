package me.oldboy.integration.controllers.admin_scope;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.dto.places.PlaceCreateDeleteDto;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.integration.ITBaseStarter;
import me.oldboy.models.entity.options.Species;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;

@AutoConfigureMockMvc
@WithUserDetails(value = "Admin", userDetailsServiceBeanName = "clientDetailsService")
class AdminPlaceControllerIT extends ITBaseStarter {

    @Autowired
    private MockMvc mockMvc;

    private Long existId, nonExistId;
    private Integer existNumber, nonExistNumber;
    private Species speciesExistInBase, speciesNoExistInBase;
    private PlaceCreateDeleteDto placeCreateNewDto, placeCreateDuplicateDto,
            placeDeleteExistDto, placeDeleteNonExistDto, placeCreateNoValidDto;
    private PlaceReadUpdateDto placeUpdateDto, placeNotExistUpdateDto, placeDuplicateAfterUpdateDto;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        existId = 5L;
        nonExistId = 100L;

        existNumber = 2;
        nonExistNumber = 100;

        speciesExistInBase = Species.WORKPLACE;
        speciesNoExistInBase = Species.STUDIO;

        placeCreateNewDto = PlaceCreateDeleteDto.builder().species(speciesNoExistInBase).placeNumber(nonExistNumber).build();
        placeDeleteExistDto = PlaceCreateDeleteDto.builder().species(speciesExistInBase).placeNumber(existNumber).build();
        placeCreateDuplicateDto = placeDeleteExistDto;
        placeDeleteNonExistDto = placeCreateNewDto;

        placeUpdateDto = PlaceReadUpdateDto.builder().placeId(existId).species(speciesNoExistInBase).placeNumber(nonExistNumber).build();
        placeDuplicateAfterUpdateDto = PlaceReadUpdateDto.builder().placeId(existId).species(speciesExistInBase).placeNumber(existNumber + 1).build();
        placeNotExistUpdateDto = PlaceReadUpdateDto.builder().placeId(nonExistId).species(speciesExistInBase).placeNumber(nonExistNumber).build();
    }

    /* ------ Тестируем *.createNewPlace() ------ */
    @Test
    @SneakyThrows
    void createNewPlace_shouldReturnGeneratedId_Test() {
        String strDtoForCreate = objectMapper.writeValueAsString(placeCreateNewDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForCreate))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        /* Получим результат запроса, обработаем до нужного формата и сравним с исходным */
        String strReturn = result.getResponse().getContentAsString();
        PlaceReadUpdateDto savedPlaceDto = objectMapper.readValue(strReturn, PlaceReadUpdateDto.class);

        assertAll(
                () -> assertThat(savedPlaceDto.placeNumber()).isEqualTo(placeCreateNewDto.placeNumber()),
                () -> assertThat(savedPlaceDto.species()).isEqualTo(placeCreateNewDto.species()),
                () -> assertThat(savedPlaceDto.placeId()).isGreaterThan(9)
        );
    }

    @Test
    @SneakyThrows
    void createNewPlace_shouldReturnBadRequest_createDuplicatePlace_Test() {
        String strDtoForCreate = objectMapper.writeValueAsString(placeCreateDuplicateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForCreate))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content()
                        .json("{\"exceptionMsg\":\"Попытка создать дубликат рабочего места/зала!\"}"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void createNewPlace_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/create"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string(""));
    }

    @Test
    @SneakyThrows
    void createNewPlace_shouldReturnValidationError_Test() {
        placeCreateNoValidDto = PlaceCreateDeleteDto.builder().placeNumber(-existNumber).build();
        String strDtoForCreate = objectMapper.writeValueAsString(placeCreateNoValidDto);


        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForCreate))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Species can not be blank/empty")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("PlaceNumber can not be null/negative")));
    }

    /* ------ Тестируем *.updatePlace() ------ */
    @Test
    @SneakyThrows
    void updatePlace_shouldReturnOk_Test() {
        String strDtoForUpdate = objectMapper.writeValueAsString(placeUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .json("{\"message\" : \"Update success!\"}"));
    }

    @Test
    @SneakyThrows
    void updatePlace_shouldReturnException_notFoundPlaceId_Test() {
        String strDtoForUpdate = objectMapper.writeValueAsString(placeNotExistUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content()
                        .json("{\"exceptionMsg\":\"Место или зал для обновления не найдены!\"}"));
    }

    @Test
    @SneakyThrows
    void updatePlace_shouldReturnException_duplicationAfterUpdate_Test() {
        String strDtoForUpdate = objectMapper.writeValueAsString(placeDuplicateAfterUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content()
                        .json("{\"exceptionMsg\":\"Обновления приведут к дублированию данных!\"}"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void updatePlace_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string(""));
    }

    /* ------ Тестируем *.deletePlace() ------ */
    @Test
    @SneakyThrows
    void deletePlace_shouldReturnOk_afterRemovePlace_Test() {
        String strDtoForDelete = objectMapper.writeValueAsString(placeDeleteExistDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForDelete))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .json("{\"message\" : \"Remove successful!\"}"));
    }

    @Test
    @SneakyThrows
    void deletePlace_shouldReturnException_notFoundPlaceForRemove_Test() {
        String strDtoForDelete = objectMapper.writeValueAsString(placeDeleteNonExistDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForDelete))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content()
                        .json("{\"exceptionMsg\":\"'" + placeDeleteNonExistDto.species().name() +
                                "' - с номером '" + placeDeleteNonExistDto.placeNumber() + "' не существует!\"}"));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void deletePlace_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/delete"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string(""));
    }
}