package me.oldboy.unit.controllers.only_mock;

import me.oldboy.config.security_details.SecurityUserDetails;
import me.oldboy.controllers.ReservationController;
import me.oldboy.controllers.utils.ParameterChecker;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.dto.reservations.ReservationCreateDto;
import me.oldboy.dto.reservations.ReservationReadDto;
import me.oldboy.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.exception.NotValidArgumentException;
import me.oldboy.models.entity.User;
import me.oldboy.models.entity.options.Role;
import me.oldboy.models.entity.options.Species;
import me.oldboy.services.PlaceService;
import me.oldboy.services.ReservationService;
import me.oldboy.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OMReservationControllerTest {

    @Mock
    private ReservationService reservationService;
    @Mock
    private PlaceService placeService;
    @Mock
    private UserService userService;
    @Mock
    private ParameterChecker parameterChecker;
    @InjectMocks
    private ReservationController reservationController;

    private ReservationReadDto reservationReadDto;
    private ReservationCreateDto reservationCreateDto;
    private ReservationUpdateDeleteDto reservationUpdateDto, reservationDeleteDto;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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

        reservationCreateDto = ReservationCreateDto.builder()
                .reservationDate(reservationDate)
                .userId(existId)
                .placeId(existId)
                .slotId(existId)
                .build();
        reservationReadDto = ReservationReadDto.builder()
                .reservationId(nonExistentId)
                .reservationDate(reservationDate)
                .userId(existId)
                .placeId(existId)
                .slotId(existId)
                .build();
        reservationUpdateDto = ReservationUpdateDeleteDto.builder()
                .reservationId(existId)
                .reservationDate(reservationDate)
                .userId(existId)
                .slotId(existId)
                .placeId(existId)
                .build();
        reservationDeleteDto = reservationUpdateDto;
        userDetails = new SecurityUserDetails(existUser);
        testDtoList = List.of(reservationReadDto, ReservationReadDto.builder().reservationId(existId).build());
        testMap = Map.of(0L, List.of(0L, 1L), 1L, List.of(2L, 3L));
        placeReadDto = PlaceReadUpdateDto.builder().placeId(existId).placeNumber(existNumber).species(Species.HALL).build();
    }

    @Test
    void createReservation_shouldReturnReservationDto_Test() {
        when(userService.findByLogin(enteredLogin)).thenReturn(Optional.of(userReadDto));
        when(reservationService.create(reservationCreateDto)).thenReturn(nonExistentId);
        when(reservationService.findById(nonExistentId)).thenReturn(Optional.of(reservationReadDto));

        ResponseEntity<ReservationReadDto> response =
                reservationController.createReservation(reservationCreateDto, userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(reservationReadDto);

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
    void updateReservation_shouldReturnResponseStatusOk_Test() {
        when(reservationService.update(reservationUpdateDto)).thenReturn(true);

        ResponseEntity<?> response = reservationController.updateReservation(reservationUpdateDto, userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"message\": \"Reservation updated\"}");

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
    void deleteReservation_shouldReturnStatusOk_Test() {
        when(reservationService.delete(reservationDeleteDto)).thenReturn(true);

        ResponseEntity<?> response = reservationController.deleteReservation(reservationDeleteDto, userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"message\": \"Reservation removed\"}");

        verify(reservationService, times(1)).delete(any(ReservationUpdateDeleteDto.class));
        /* Утилитный класс мы будем тестировать отдельно, тут только верифицируем */
        verify(parameterChecker, times(1)).isUserCorrect(anyLong());
        verify(parameterChecker, times(1)).isAdmin(any(UserDetails.class));
        verify(parameterChecker, times(1))
                .canUpdateOrDelete(anyString(), anyBoolean(), any(ReservationUpdateDeleteDto.class));
    }

    @Test
    void readAllReservation_shouldReturnReservationDtoList_Test() {
        when(reservationService.findAll()).thenReturn(testDtoList);

        assertThat(reservationController.readAllReservation().size()).isEqualTo(testDtoList.size());
    }

    @Test
    void getReservationByParam_shouldReturnReservationList_foundByDate_Test() {
        when(reservationService.findByDate(reservationDate)).thenReturn(Optional.of(testDtoList));

        List<ReservationReadDto> fromBaseReservationList =
                reservationController.getReservationByParam(reservationDate, null, null);

        assertThat(fromBaseReservationList.size()).isEqualTo(testDtoList.size());
    }

    @Test
    void getReservationByParam_shouldReturnReservationList_foundByUserId_Test() {
        when(reservationService.findByUserId(existId)).thenReturn(Optional.of(testDtoList));
        when(userService.findById(existId)).thenReturn(Optional.of(userReadDto));

        List<ReservationReadDto> fromBaseReservationList =
                reservationController.getReservationByParam(null, existId, null);

        assertThat(fromBaseReservationList.size()).isEqualTo(testDtoList.size());
    }

    @Test
    void getReservationByParam_shouldReturnReservationList_foundByPlaceId_Test() {
        when(reservationService.findByPlaceId(existId)).thenReturn(Optional.of(testDtoList));
        when(placeService.findById(existId)).thenReturn(Optional.of(placeReadDto));

        List<ReservationReadDto> fromBaseReservationList =
                reservationController.getReservationByParam(null, null, existId);

        assertThat(fromBaseReservationList.size()).isEqualTo(testDtoList.size());
    }

    @Test
    void getReservationByParam_shouldReturnException_moreThenOneParamOnRequest_Test() {
        assertThatThrownBy(() -> reservationController.getReservationByParam(reservationDate, null, existId))
                .isInstanceOf(NotValidArgumentException.class)
                .hasMessageContaining("Неверное сочетание параметров (достаточно одного параметра, комбинация не принимается)!");
    }

    @Test
    void getReservationByParam_shouldReturnException_notCorrectUserId_Test() {
        when(userService.findById(-existId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationController.getReservationByParam(null, -existId, null))
                .isInstanceOf(NotValidArgumentException.class)
                .hasMessageContaining("Введенный параметр не найден в БД или отрицательный!");
    }

    @Test
    void getReservationByParam_shouldReturnException_notCorrectPlaceId_Test() {
        when(placeService.findById(-existId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationController.getReservationByParam(null, null, -existId))
                .isInstanceOf(NotValidArgumentException.class)
                .hasMessageContaining("Введенный параметр не найден в БД или отрицательный!");
    }

    @Test
    void getFreeSlotsByDate_shouldReturnFreeSlotsMap_Test() {
        when(parameterChecker.convertStringDateWithValidate(stringValidDate)).thenReturn(validReservationDate);
        when(reservationService.findAllFreeSlotsByDate(validReservationDate)).thenReturn(testMap);

        assertThat(reservationController.getFreeSlotsByDate(stringValidDate).size()).isEqualTo(testMap.size());
    }
}