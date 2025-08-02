package me.oldboy.unit.controllers.web_mvc.user_scope;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.auditor.core.repository.AuditRepository;
import me.oldboy.controllers.user_scope.UserPlaceController;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.models.entity.options.Species;
import me.oldboy.repository.PlaceRepository;
import me.oldboy.repository.ReservationRepository;
import me.oldboy.repository.SlotRepository;
import me.oldboy.repository.UserRepository;
import me.oldboy.services.PlaceService;
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

@WebMvcTest(UserPlaceController.class)
@WithMockUser()
class MVCUserPlaceControllerTest {

    @MockBean
    private PlaceService placeService;
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
    private Long existId, nonExistentId;
    private Integer existNumber, nonExistentNumber;
    private PlaceReadUpdateDto placeReadDto;

    @BeforeAll
    static void setStaticContent() {
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp() {
        existId = 1L;
        nonExistentId = 100L;

        existNumber = 1;
        nonExistentNumber = 100;

        placeReadDto = PlaceReadUpdateDto.builder().placeId(existId).placeNumber(existNumber).species(Species.HALL).build();
    }

    @Test
    @SneakyThrows
    void readPlaceById_shouldReturnResponseStatusOk_Test() {
        when(placeService.findById(existId)).thenReturn(Optional.of(placeReadDto));

        MvcResult result = mockMvc.perform(get("/api/places/{placeId}", existId))
                .andExpect(status().isOk())
                .andReturn();

        /* Получим результат запроса, обработаем до нужного формата и сравним с исходным */
        String strReturn = result.getResponse().getContentAsString();
        PlaceReadUpdateDto savedPlaceDto = objectMapper.readValue(strReturn, PlaceReadUpdateDto.class);

        assertAll(
                () -> assertThat(savedPlaceDto.placeNumber()).isEqualTo(placeReadDto.placeNumber()),
                () -> assertThat(savedPlaceDto.species()).isEqualTo(placeReadDto.species())
        );

        verify(placeService, times(1)).findById(anyLong());
    }

    @Test
    @SneakyThrows
    void readPlaceById_shouldReturnException_canNotFindPlaceId_Test() {
        when(placeService.findById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/places/{placeId}", nonExistentId))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json("{\"exceptionMsg\":\"Конференц-зала / рабочего места с ID: " + nonExistentId + " не существует!\"}"));

        verify(placeService, times(1)).findById(anyLong());
    }

    @Test
    @SneakyThrows
    void readPlaceById_shouldReturnException_negativePlaceId_Test() {
        mockMvc.perform(get("/api/places/{placeId}", -nonExistentId))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json("{\"exceptionMsg\":\"Идентификатор не может быть отрицательным!\"}"));
    }

    @Test
    @SneakyThrows
    void readPlaceBySpeciesAndNumber_shouldReturnFindPlace_Test() {
        when(placeService.findPlaceBySpeciesAndNumber(placeReadDto.species(), placeReadDto.placeNumber())).thenReturn(Optional.of(placeReadDto));

        MvcResult result = mockMvc.perform(get("/api/places/species/{species}/number/{placeNumber}", placeReadDto.species(), placeReadDto.placeNumber()))
                .andExpect(status().isOk())
                .andReturn();

        /* Получим результат запроса, обработаем до нужного формата и сравним с исходным */
        String strReturn = result.getResponse().getContentAsString();
        PlaceReadUpdateDto savedPlaceDto = objectMapper.readValue(strReturn, PlaceReadUpdateDto.class);

        assertAll(
                () -> assertThat(savedPlaceDto.placeNumber()).isEqualTo(placeReadDto.placeNumber()),
                () -> assertThat(savedPlaceDto.species()).isEqualTo(placeReadDto.species())
        );

        verify(placeService, times(1)).findPlaceBySpeciesAndNumber(any(Species.class), anyInt());
    }

    @Test
    @SneakyThrows
    void getAllPlaces_shouldReturnDtoList_Test() {
        List<PlaceReadUpdateDto> testDtoDList =
                List.of(PlaceReadUpdateDto.builder().placeId(existId).build(),
                        PlaceReadUpdateDto.builder().placeId(nonExistentId).build());
        when(placeService.findAll()).thenReturn(testDtoDList);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/places"))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<PlaceReadUpdateDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<PlaceReadUpdateDto>>() {
        });

        assertThat(listFromResponse.size()).isEqualTo(testDtoDList.size());
    }
}