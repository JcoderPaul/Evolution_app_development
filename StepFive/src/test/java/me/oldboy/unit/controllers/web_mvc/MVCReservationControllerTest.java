package me.oldboy.unit.controllers.web_mvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import me.oldboy.auditor.core.repository.AuditRepository;
import me.oldboy.config.security_details.SecurityUserDetails;
import me.oldboy.controllers.ReservationController;
import me.oldboy.controllers.utils.ParameterChecker;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.dto.reservations.ReservationCreateDto;
import me.oldboy.dto.reservations.ReservationReadDto;
import me.oldboy.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.models.entity.User;
import me.oldboy.models.entity.options.Role;
import me.oldboy.models.entity.options.Species;
import me.oldboy.repository.PlaceRepository;
import me.oldboy.repository.ReservationRepository;
import me.oldboy.repository.SlotRepository;
import me.oldboy.repository.UserRepository;
import me.oldboy.services.PlaceService;
import me.oldboy.services.ReservationService;
import me.oldboy.services.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@WithMockUser(username = "Duglas Lind", password = "1234", authorities = {"USER"})
class MVCReservationControllerTest {

    @MockBean
    private ReservationService reservationService;
    @MockBean
    private PlaceService placeService;
    @MockBean
    private UserService userService;
    @MockBean
    private ParameterChecker parameterChecker;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ReservationRepository reservationRepository;
    @MockBean
    private PlaceRepository placeRepository;
    @MockBean
    private SlotRepository slotRepository;
    @MockBean
    private AuditRepository auditRepository;
    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;
    private ReservationReadDto reservationReadDto;
    private ReservationCreateDto reservationCreateDto, reservationCreateNoValidDto;
    private ReservationUpdateDeleteDto reservationUpdateDto, reservationUpdateNotValidDto,
            reservationDeleteDto, reservationDeleteNotValidDto;
    private PlaceReadUpdateDto placeReadDto;
    private UserReadDto userReadDto;
    private Long existId, nonExistentId;
    private Integer existNumber;
    private LocalDate reservationDate, validReservationDate;
    private String enteredLogin, enteredPassword, stringValidDate, stringNotValidDate;
    private UserDetails userDetails;
    private User existUser;
    private List<ReservationReadDto> testDtoList;
    private Map<Long, List<Long>> testMap;

    @BeforeAll
    static void setStaticContent() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setUp() {
        existId = 1L;
        nonExistentId = 100L;

        existNumber = 10;

        enteredLogin = "Duglas Lind";
        enteredPassword = "1234";
        stringValidDate = "2029-12-03";
        stringNotValidDate = "12-03-2029";

        reservationDate = LocalDate.of(2031, 04, 02);
        validReservationDate = LocalDate.of(2029, 12, 03);

        userReadDto = UserReadDto.builder().userId(existId).login(enteredLogin).role(Role.USER).build();
        existUser = User.builder().userId(existId).login(enteredLogin).password(enteredPassword).role(Role.USER).build();

        reservationCreateDto = ReservationCreateDto.builder().reservationDate(reservationDate).userId(existId).placeId(existId).slotId(existId).build();
        reservationCreateNoValidDto = ReservationCreateDto.builder().userId(-existId).placeId(-existId).slotId(-existId).build();
        reservationReadDto = ReservationReadDto.builder().reservationId(nonExistentId).reservationDate(reservationDate).userId(existId).placeId(existId).slotId(existId).build();
        reservationUpdateDto = ReservationUpdateDeleteDto.builder().reservationId(existId).reservationDate(reservationDate).userId(existId).slotId(existId).placeId(existId).build();
        reservationUpdateNotValidDto = ReservationUpdateDeleteDto.builder().userId(-existId).slotId(-existId).placeId(-existId).build();

        reservationDeleteDto = reservationUpdateDto;
        reservationDeleteNotValidDto = reservationUpdateNotValidDto;

        userDetails = new SecurityUserDetails(existUser);
        testDtoList = List.of(reservationReadDto, ReservationReadDto.builder().reservationId(existId).build());
        testMap = Map.of(0L, List.of(0L, 1L), 1L, List.of(2L, 3L));
        placeReadDto = PlaceReadUpdateDto.builder().placeId(existId).placeNumber(existNumber).species(Species.HALL).build();
    }

    @Test
    @SneakyThrows
    void createReservation_shouldReturnReservationDto_Test() {
        when(userService.findByLogin(enteredLogin)).thenReturn(Optional.of(userReadDto));
        when(reservationService.create(reservationCreateDto)).thenReturn(nonExistentId);
        when(reservationService.findById(nonExistentId)).thenReturn(Optional.of(reservationReadDto));

        String strDtoForCreate = objectMapper.writeValueAsString(reservationCreateDto);

        MvcResult result = mockMvc.perform(post("/api/reservations/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForCreate))
                .andExpect(status().isOk())
                .andReturn();

        /* Получим результат запроса, обработаем до нужного формата и сравним с исходным */
        String strReturn = result.getResponse().getContentAsString();
        ReservationReadDto savedDto = objectMapper.readValue(strReturn, ReservationReadDto.class);

        assertAll(
                () -> assertThat(savedDto.reservationDate()).isEqualTo(reservationCreateDto.getReservationDate()),
                () -> assertThat(savedDto.slotId()).isEqualTo(reservationCreateDto.getSlotId()),
                () -> assertThat(savedDto.placeId()).isEqualTo(reservationCreateDto.getPlaceId()),
                () -> assertThat(savedDto.userId()).isEqualTo(reservationCreateDto.getUserId())
        );

        verify(userService, times(1)).findByLogin(anyString());
        verify(reservationService, times(1)).create(any(ReservationCreateDto.class));
        verify(reservationService, times(1)).findById(anyLong());
        /* Сам утилитный класс мы будем тестировать отдельно, тут только верифицируем */
        verify(parameterChecker, times(1)).isUserCorrect(anyLong());
        verify(parameterChecker, times(1)).isPlaceCorrect(anyLong());
        verify(parameterChecker, times(1)).isSlotCorrect(anyLong());
        verify(parameterChecker, times(1))
                .isReservationNotDuplicate(any(LocalDate.class), anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    void createReservation_shouldReturnValidationErrors_Test() {
        String strDtoForCreate = objectMapper.writeValueAsString(reservationCreateNoValidDto);

        mockMvc.perform(post("/api/reservations/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForCreate))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Place ID must be greater than or equal to 0")))
                .andExpect(content().string(containsString("Slot ID must be greater than or equal to 0")))
                .andExpect(content().string(containsString("Reservation date can not be blank or null")))
                .andExpect(content().string(containsString("User ID must be greater than or equal to 0")));
    }

    @Test
    @SneakyThrows
    void updateReservation_shouldReturnResponseStatusOk_Test() {
        when(reservationService.update(reservationUpdateDto)).thenReturn(true);

        String strDtoForUpdate = objectMapper.writeValueAsString(reservationUpdateDto);

        mockMvc.perform(post("/api/reservations/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\": \"Reservation updated\"}"));

        verify(reservationService, times(1)).update(any(ReservationUpdateDeleteDto.class));
        /* Утилитный класс мы будем тестировать отдельно, тут только верифицируем */
        verify(parameterChecker, times(1)).isUserCorrect(anyLong());
        verify(parameterChecker, times(1)).isPlaceCorrect(anyLong());
        verify(parameterChecker, times(1)).isSlotCorrect(anyLong());
        verify(parameterChecker, times(1))
                .isReservationNotDuplicate(any(LocalDate.class), anyLong(), anyLong());
        verify(parameterChecker, times(1)).isAdmin(any(UserDetails.class));
        verify(parameterChecker, times(1))
                .canUpdateOrDelete(anyString(), anyBoolean(), any(ReservationUpdateDeleteDto.class));
    }

    @Test
    @SneakyThrows
    void updateReservation_shouldReturnValidationError_Test() {
        String strDtoForUpdate = objectMapper.writeValueAsString(reservationUpdateNotValidDto);

        mockMvc.perform(post("/api/reservations/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Reservation ID can not be blank")))
                .andExpect(content().string(containsString("Place ID must be greater than or equal to 0")))
                .andExpect(content().string(containsString("Slot ID must be greater than or equal to 0")))
                .andExpect(content().string(containsString("Reservation date can not be blank or null")))
                .andExpect(content().string(containsString("User ID must be greater than or equal to 0")));
    }

    @Test
    @SneakyThrows
    void deleteReservation_shouldReturnStatusOk_Test() {
        when(reservationService.delete(reservationDeleteDto)).thenReturn(true);

        String strDtoForDelete = objectMapper.writeValueAsString(reservationDeleteDto);

        mockMvc.perform(post("/api/reservations/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForDelete))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("{\"message\": \"Reservation removed\"}"));

        verify(reservationService, times(1)).delete(any(ReservationUpdateDeleteDto.class));
        /* Утилитный класс мы будем тестировать отдельно, тут только верифицируем */
        verify(parameterChecker, times(1)).isUserCorrect(anyLong());
        verify(parameterChecker, times(1)).isAdmin(any(UserDetails.class));
        verify(parameterChecker, times(1))
                .canUpdateOrDelete(anyString(), anyBoolean(), any(ReservationUpdateDeleteDto.class));
    }

    @Test
    @SneakyThrows
    void readAllReservation_shouldReturnReservationDtoList_Test() {
        when(reservationService.findAll()).thenReturn(testDtoList);

        MvcResult result = mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<ReservationReadDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<ReservationReadDto>>() {
        });

        assertThat(listFromResponse.size()).isEqualTo(testDtoList.size());
    }

    @Test
    @SneakyThrows
    void getReservationByParam_shouldReturnReservationList_foundByDate_Test() {
        when(reservationService.findByDate(reservationDate)).thenReturn(Optional.of(testDtoList));

        String formattedDate = reservationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        MvcResult result = mockMvc.perform(get("/api/reservations/booked")
                        .param("reservationDate", formattedDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<ReservationReadDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<ReservationReadDto>>() {
        });

        assertThat(listFromResponse.size()).isEqualTo(testDtoList.size());
    }

    @Test
    @SneakyThrows
    void getReservationByParam_shouldReturnReservationList_foundByUserId_Test() {
        when(reservationService.findByUserId(existId)).thenReturn(Optional.of(testDtoList));
        when(userService.findById(existId)).thenReturn(Optional.of(userReadDto));

        MvcResult result = mockMvc.perform(get("/api/reservations/booked")
                        .param("userId", String.valueOf(existId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<ReservationReadDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<ReservationReadDto>>() {
        });

        assertThat(listFromResponse.size()).isEqualTo(testDtoList.size());
    }

    @Test
    @SneakyThrows
    void getReservationByParam_shouldReturnReservationList_foundByPlaceId_Test() {
        when(reservationService.findByPlaceId(existId)).thenReturn(Optional.of(testDtoList));
        when(placeService.findById(existId)).thenReturn(Optional.of(placeReadDto));

        MvcResult result = mockMvc.perform(get("/api/reservations/booked")
                        .param("placeId", String.valueOf(existId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<ReservationReadDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<ReservationReadDto>>() {
        });


        assertThat(listFromResponse.size()).isEqualTo(testDtoList.size());
    }

    @Test
    @SneakyThrows
    void getReservationByParam_shouldReturnException_moreThenOneParamOnRequest_Test() {
        mockMvc.perform(get("/api/reservations/booked")
                        .param("placeId", String.valueOf(existId))
                        .param("userId", String.valueOf(existId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json("{\"exceptionMsg\":\"Parse or unexpected error (check the entered parameters): " +
                                "Неверное сочетание параметров (достаточно одного параметра, комбинация не принимается)!\"}"));
    }

    @Test
    @SneakyThrows
    void getReservationByParam_shouldReturnException_notCorrectUserId_Test() {
        mockMvc.perform(get("/api/reservations/booked")
                        .param("userId", String.valueOf(-existId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json("{\"exceptionMsg\":\"Parse or unexpected error (check the entered parameters): " +
                                "Введенный параметр не найден в БД или отрицательный!\"}"));
    }

    @Test
    @SneakyThrows
    void getReservationByParam_shouldReturnException_notCorrectPlaceId_Test() {
        mockMvc.perform(get("/api/reservations/booked")
                        .param("placeId", String.valueOf(-existId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json("{\"exceptionMsg\":\"Parse or unexpected error (check the entered parameters): " +
                                "Введенный параметр не найден в БД или отрицательный!\"}"));
    }

    @Test
    @SneakyThrows
    void getFreeSlotsByDate_shouldReturnFreeSlotsMap_Test() {
        when(parameterChecker.convertStringDateWithValidate(stringValidDate)).thenReturn(validReservationDate);
        when(reservationService.findAllFreeSlotsByDate(validReservationDate)).thenReturn(testMap);

        String formattedDate = validReservationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        MvcResult result = mockMvc.perform(get("/api/reservations/free/date/{date}", formattedDate))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        Map<Long, List<Long>> mapFromResponse = objectMapper.readValue(strResult, new TypeReference<Map<Long, List<Long>>>() {
        });

        assertThat(mapFromResponse.size()).isEqualTo(testMap.size());
    }
}