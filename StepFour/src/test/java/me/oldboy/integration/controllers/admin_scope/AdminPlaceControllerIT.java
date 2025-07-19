package me.oldboy.integration.controllers.admin_scope;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.jwt.JwtAuthRequest;
import me.oldboy.dto.jwt.JwtAuthResponse;
import me.oldboy.dto.places.PlaceCreateDeleteDto;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.entity.options.Species;
import me.oldboy.services.PlaceService;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class AdminPlaceControllerIT extends TestContainerInit {

    @Autowired
    private PlaceService placeService;
    @Autowired
    private WebApplicationContext applicationContext;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private JwtAuthRequest adminJwtRequest;
    private String goodJwtToken, notValidToken;
    private PlaceReadUpdateDto updatePlaceDto, updateNotExistentPlaceDto, updateNonValidPlaceDto, updateDuplicatePlaceDto;
    private PlaceCreateDeleteDto createNewPlaceDto, createNotValidPlaceDto, createDuplicatePlaceDto,
            deletePlaceDto, deleteNotValidPlaceDto, deleteNonExistentPlaceDto;
    private String BEARER_PREFIX = "Bearer ";
    private String HEADER_NAME = "Authorization";

    @BeforeEach
    @SneakyThrows
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();

        objectMapper = new ObjectMapper();

        adminJwtRequest = JwtAuthRequest.builder()
                .login("Admin")
                .password("1234")
                .build();

        updatePlaceDto = PlaceReadUpdateDto.builder()
                .placeId(1L)
                .species(Species.STUDIO)
                .placeNumber(10)
                .build();
        updateNotExistentPlaceDto = PlaceReadUpdateDto.builder()
                .placeId(23L)
                .species(Species.HALL)
                .placeNumber(2)
                .build();
        updateNonValidPlaceDto = PlaceReadUpdateDto.builder()
                .placeId(1L)
                .placeNumber(-10)
                .build();
        updateDuplicatePlaceDto = PlaceReadUpdateDto.builder()
                .placeId(1L)
                .species(Species.WORKPLACE)
                .placeNumber(3)
                .build();

        createNewPlaceDto = PlaceCreateDeleteDto.builder()
                .species(Species.STUDIO)
                .placeNumber(1)
                .build();
        createDuplicatePlaceDto = PlaceCreateDeleteDto.builder()
                .species(Species.HALL)
                .placeNumber(2)
                .build();
        createNotValidPlaceDto = PlaceCreateDeleteDto.builder()
                .placeNumber(-2)
                .build();

        deletePlaceDto = createDuplicatePlaceDto;
        deleteNotValidPlaceDto = createNotValidPlaceDto;
        deleteNonExistentPlaceDto = createNewPlaceDto;

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

    /* --- Тестируем метод создания рабочего места/зала --- */
    @Test
    @SneakyThrows
    void createNewPlace_shouldReturnCreatedDto_Test() {
        String forCreatePlace = objectMapper.writeValueAsString(createNewPlaceDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/create")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreatePlace))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        PlaceReadUpdateDto placeReadUpdateDto = objectMapper.readValue(strReturn, PlaceReadUpdateDto.class);

        assertThat(createNewPlaceDto.species()).isEqualTo(placeReadUpdateDto.species());
        assertThat(createNewPlaceDto.placeNumber()).isEqualTo(placeReadUpdateDto.placeNumber());
    }

    @Test
    @SneakyThrows
    void createNewPlace_shouldReturnException_tryToCreateDuplicateRecord_Test() {
        String forCreatePlace = objectMapper.writeValueAsString(createDuplicatePlaceDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/create")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreatePlace))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Попытка создать дубликат рабочего места/зала!\"}"));
    }

    @Test
    @SneakyThrows
    void createNewPlace_shouldReturnValidationError_Test() {
        String forCreatePlace = objectMapper.writeValueAsString(createNotValidPlaceDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/create")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forCreatePlace))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"species\" : \"Species can not be blank/empty\"")))
                .andExpect(content().string(containsString("\"placeNumber\" : \"PlaceNumber can not be null/negative\"")));
    }

    @Test
    @SneakyThrows
    void createNewPlace_shouldReturnForbidden_notValidToken_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/create")
                        .header(HEADER_NAME, notValidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void createNewPlace_shouldReturnForbidden_notAdminAuth_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    /* --- Тестируем метод обновления рабочего места/зала --- */
    @Test
    @SneakyThrows
    void updatePlace_shouldReturnOk_AfterUpdate_Test() {
        String forUpdatePlace = objectMapper.writeValueAsString(updatePlaceDto);
        Optional<PlaceReadUpdateDto> beforeUpdatePlace = placeService.findById(updatePlaceDto.placeId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdatePlace))
                .andExpect(status().isOk());

        Optional<PlaceReadUpdateDto> afterUpdatePlace = placeService.findById(updatePlaceDto.placeId());

        if (beforeUpdatePlace.isPresent() && afterUpdatePlace.isPresent()) {
            assertThat(beforeUpdatePlace.get().species()).isNotEqualTo(updatePlaceDto.species());
            assertThat(afterUpdatePlace.get().species()).isEqualTo(updatePlaceDto.species());
        }
    }

    @Test
    @SneakyThrows
    void updatePlace_shouldReturnException_nonExistentPlaceUpdate_Test() {
        String forUpdatePlace = objectMapper.writeValueAsString(updateNotExistentPlaceDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdatePlace))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Место или зал для обновления не найдены!\"}"));
    }

    @Test
    @SneakyThrows
    void updatePlace_shouldReturnException_duplicatePlaceUpdate_Test() {
        String forUpdatePlace = objectMapper.writeValueAsString(updateDuplicatePlaceDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdatePlace))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Обновления приведут к дублированию данных!\"}"));
    }

    @Test
    @SneakyThrows
    void updatePlace_shouldReturnValidationError_Test() {
        String forUpdatePlace = objectMapper.writeValueAsString(updateNonValidPlaceDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdatePlace))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"species\" : \"Species can not be blank/empty\"")))
                .andExpect(content().string(containsString("\"placeNumber\" : \"PlaceNumber can not be null/negative\"")));
    }

    @Test
    @SneakyThrows
    void updatePlace_shouldReturnForbidden_notValidToken_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update")
                        .header(HEADER_NAME, notValidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void updatePlace_shouldReturnForbidden_notAdminUser_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    /* --- Тестируем метод удаления рабочего места/зала --- */
    @Test
    @SneakyThrows
    void deletePlace_shouldReturnOk_successDelete_Test() {
        String forDeletePlace = objectMapper.writeValueAsString(deletePlaceDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/delete")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeletePlace))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Удаление рабочего места прошло успешно!")));
    }

    @Test
    @SneakyThrows
    void deletePlace_shouldReturnException_haveNoPlaceForDelete_Test() {
        String forDeletePlace = objectMapper.writeValueAsString(deleteNonExistentPlaceDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/delete")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeletePlace))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("{\"exceptionMsg\":\"'STUDIO' - с номером '1' не существует!\"}")));
    }

    @Test
    @SneakyThrows
    void deletePlace_shouldReturnValidationError_Test() {
        String forDeletePlace = objectMapper.writeValueAsString(deleteNotValidPlaceDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/delete")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeletePlace))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"species\" : \"Species can not be blank/empty\"")))
                .andExpect(content().string(containsString("\"placeNumber\" : \"PlaceNumber can not be null/negative\"")));
    }

    @Test
    @SneakyThrows
    void deletePlace_shouldReturnForbidden_notValidToken_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/delete")
                        .header(HEADER_NAME, notValidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void deletePlace_shouldReturnForbidden_notAdminAuth_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/delete")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }
}