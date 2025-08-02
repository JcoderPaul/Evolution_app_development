package me.oldboy.unit.controllers.web_mvc.admin_scope;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import lombok.SneakyThrows;
import me.oldboy.auditor.core.annotation.Auditable;
import me.oldboy.auditor.core.auditing.AuditingAspect;
import me.oldboy.auditor.core.repository.AuditRepository;
import me.oldboy.controllers.admin_scope.AdminPlaceController;
import me.oldboy.dto.places.PlaceCreateDeleteDto;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@WebMvcTest(value = AdminPlaceController.class)
@WithMockUser(authorities = {"ADMIN"})
class MVCAdminPlaceControllerTest {

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

    private PlaceCreateDeleteDto placeCreateValidDto, placeCreateNotValidDto, placeDeleteDto;
    private PlaceReadUpdateDto placeReadDto, placeUpdateDto, placeNoValidUpdateDto;
    private Long existId, nonExistId;
    private Integer existNumber, nonExistNumber;

    @BeforeAll
    static void setStaticContent() {
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp() {
        existId = 1L;
        nonExistId = 100L;

        existNumber = 1;
        nonExistNumber = 100;

        placeCreateValidDto = PlaceCreateDeleteDto.builder().species(Species.HALL).placeNumber(nonExistNumber).build();
        placeReadDto = PlaceReadUpdateDto.builder().placeId(nonExistId).species(Species.HALL).placeNumber(nonExistNumber).build();
        placeUpdateDto = placeReadDto;
        placeDeleteDto = placeCreateValidDto;
        placeCreateNotValidDto = PlaceCreateDeleteDto.builder().placeNumber(-1).build();
        placeNoValidUpdateDto = PlaceReadUpdateDto.builder().placeId(-existId).placeNumber(-3).build();
    }

    /* ------ Тестируем метод *.createNewPlace() ------ */
    @Test
    @SneakyThrows
    void createNewPlace_shouldReturnCreatedPlace_whenPlaceDoesNotExist_Test() {
        /* Готовим данные */
        when(placeService.isPlaceExist(placeCreateValidDto.species(), placeCreateValidDto.placeNumber())).thenReturn(false);
        when(placeService.create(placeCreateValidDto)).thenReturn(nonExistId);
        when(placeService.findById(nonExistId)).thenReturn(Optional.of(placeReadDto));

        String strDtoForCreate = objectMapper.writeValueAsString(placeCreateValidDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForCreate))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        /* Получим результат запроса, обработаем до нужного формата и сравним с исходным */
        String strReturn = result.getResponse().getContentAsString();
        PlaceReadUpdateDto savedPlaceDto = objectMapper.readValue(strReturn, PlaceReadUpdateDto.class);

        assertAll(
                () -> assertThat(savedPlaceDto.placeNumber()).isEqualTo(placeReadDto.placeNumber()),
                () -> assertThat(savedPlaceDto.species()).isEqualTo(placeReadDto.species())
        );

        /* Верифицируем вызовы */
        verify(placeService, times(1)).isPlaceExist(any(Species.class), anyInt());
        verify(placeService, times(1)).create(any(PlaceCreateDeleteDto.class));
        verify(placeService, times(1)).findById(anyLong());
    }

    /*
        Сущность PlaceCreateDeleteDto применяется в двух методах и подвергается валидации по единому
        сценарию в обоих, посему в методе *.deletePlace() мы не будем проводить тест валидации, т.к.
        проверили работу данного функционала, а он один и тот же, в текущем методе.
    */
    @Test
    @SneakyThrows
    void createNewPlace_shouldReturnValidationException_Test() {
        String strDtoForCreate = objectMapper.writeValueAsString(placeCreateNotValidDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForCreate))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content()
                        .string(containsString("Species can not be blank/empty")))
                .andExpect(MockMvcResultMatchers.content()
                        .string(containsString("PlaceNumber can not be null/negative")));
    }

    @Test
    @SneakyThrows
    void createNewPlace_shouldReturnException_tryToDuplicate_Test() {
        /* Готовим данные */
        when(placeService.isPlaceExist(placeCreateValidDto.species(), placeCreateValidDto.placeNumber())).thenReturn(true);

        String strDtoForCreate = objectMapper.writeValueAsString(placeCreateValidDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForCreate))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content()
                        .json("{\"exceptionMsg\":\"Попытка создать дубликат рабочего места/зала!\"}"));

        /* Верифицируем вызовы */
        verify(placeService, times(1)).isPlaceExist(any(Species.class), anyInt());
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"USER"})
    void createNewPlace_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/create"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string(""));
    }


    /* ------ Тестируем метод *.updatePlace() ------ */
    @Test
    @SneakyThrows
    void updatePlace_shouldReturnOk_whenPlaceUpdatedSuccess_Test() {
        when(placeService.isPlaceExist(placeUpdateDto.placeId())).thenReturn(true);
        when(placeService.isPlaceExist(placeUpdateDto.species(), placeUpdateDto.placeNumber())).thenReturn(false);
        when(placeService.update(placeUpdateDto)).thenReturn(true);

        String strDtoForUpdate = objectMapper.writeValueAsString(placeUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Update success!")));

        verify(placeService, times(1)).isPlaceExist(any(Species.class), anyInt());
        verify(placeService, times(1)).isPlaceExist(anyLong());
        verify(placeService, times(1)).update(any(PlaceReadUpdateDto.class));
    }

    @Test
    @SneakyThrows
    void updatePlace_shouldReturnValidationException_Test() {
        String strDtoForUpdate = objectMapper.writeValueAsString(placeNoValidUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content()
                        .string(containsString("Species can not be blank/empty")))
                .andExpect(MockMvcResultMatchers.content()
                        .string(containsString("PlaceId can not be null/negative")))
                .andExpect(MockMvcResultMatchers.content()
                        .string(containsString("PlaceNumber can not be null/negative")));
    }

    @Test
    @SneakyThrows
    void updatePlace_shouldReturnBadRequest_unExpectedError_Test() {
        when(placeService.isPlaceExist(placeUpdateDto.placeId())).thenReturn(true);
        when(placeService.isPlaceExist(placeUpdateDto.species(), placeUpdateDto.placeNumber())).thenReturn(false);
        when(placeService.update(placeUpdateDto)).thenReturn(false);

        String strDtoForUpdate = objectMapper.writeValueAsString(placeUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Update failed!")));

        verify(placeService, times(1)).isPlaceExist(any(Species.class), anyInt());
        verify(placeService, times(1)).isPlaceExist(anyLong());
        verify(placeService, times(1)).update(any(PlaceReadUpdateDto.class));
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"USER"})
    void updatePlace_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string(""));
    }

    @Test
    @SneakyThrows
    void updatePlace_shouldReturnException_haveNoPlaceForUpdate_Test() {
        when(placeService.isPlaceExist(placeUpdateDto.placeId())).thenReturn(false);

        String strDtoForUpdate = objectMapper.writeValueAsString(placeUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content()
                        .json("{\"exceptionMsg\":\"Место или зал для обновления не найдены!\"}"));

        verify(placeService, times(1)).isPlaceExist(anyLong());
    }

    @Test
    @SneakyThrows
    void updatePlace_shouldReturnException_toDuplicatedUpdate_Test() {
        when(placeService.isPlaceExist(placeUpdateDto.placeId())).thenReturn(true);
        when(placeService.isPlaceExist(placeUpdateDto.species(), placeUpdateDto.placeNumber())).thenReturn(true);

        String strDtoForUpdate = objectMapper.writeValueAsString(placeUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content()
                        .json("{\"exceptionMsg\":\"Обновления приведут к дублированию данных!\"}"));

        verify(placeService, times(1)).isPlaceExist(any(Species.class), anyInt());
        verify(placeService, times(1)).isPlaceExist(anyLong());
    }

    /* ------ Тестируем метод *.deletePlace() ------ */
    @Test
    @SneakyThrows
    void deletePlace_shouldReturnOk_afterDeleteExistPlace_Test() {
        when(placeService.findPlaceBySpeciesAndNumber(placeDeleteDto.species(), placeDeleteDto.placeNumber())).thenReturn(Optional.of(placeReadDto));
        when(placeService.delete(placeReadDto.placeId())).thenReturn(true);

        String strDtoForDelete = objectMapper.writeValueAsString(placeDeleteDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForDelete))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Remove successful!")));

        verify(placeService, times(1)).findPlaceBySpeciesAndNumber(any(Species.class), anyInt());
        verify(placeService, times(1)).delete(anyLong());
    }

    @Test
    @SneakyThrows
    void deletePlace_shouldReturnBadRequest_unExpectedError_Test() {
        when(placeService.findPlaceBySpeciesAndNumber(placeDeleteDto.species(), placeDeleteDto.placeNumber())).thenReturn(Optional.of(placeReadDto));
        when(placeService.delete(placeReadDto.placeId())).thenReturn(false);

        String strDtoForDelete = objectMapper.writeValueAsString(placeDeleteDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForDelete))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Remove failed!")));

        verify(placeService, times(1)).findPlaceBySpeciesAndNumber(any(Species.class), anyInt());
        verify(placeService, times(1)).delete(anyLong());
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"USER"})
    void deletePlace_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/places/delete"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string(""));
    }
}