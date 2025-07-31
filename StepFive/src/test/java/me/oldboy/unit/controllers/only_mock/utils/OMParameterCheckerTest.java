package me.oldboy.unit.controllers.only_mock.utils;

import me.oldboy.config.security_details.SecurityUserDetails;
import me.oldboy.controllers.utils.ParameterChecker;
import me.oldboy.dto.places.PlaceReadUpdateDto;
import me.oldboy.dto.reservations.ReservationReadDto;
import me.oldboy.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.dto.slots.SlotReadUpdateDto;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.exception.reservation_exception.ReservationControllerException;
import me.oldboy.models.entity.User;
import me.oldboy.models.entity.options.Role;
import me.oldboy.services.PlaceService;
import me.oldboy.services.ReservationService;
import me.oldboy.services.SlotService;
import me.oldboy.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class OMParameterCheckerTest {

    @Mock
    private UserService userService;
    @Mock
    private PlaceService placeService;
    @Mock
    private SlotService slotService;
    @Mock
    private ReservationService reservationService;
    @InjectMocks
    private ParameterChecker parameterChecker;

    private User testUserRoleAdmin, testUserRoleUser;
    private UserDetails userDetails;
    private Long existId, anotherId;
    private Integer existNumber;
    private LocalDate existDate;
    private String testLogin, testPassword, validStringDate, notValidStringDate;
    private ReservationReadDto reservationReadDto;
    private ReservationUpdateDeleteDto reservationUpdateDeleteDto;
    private SlotReadUpdateDto slotReadUpdateDto;
    private PlaceReadUpdateDto placeReadUpdateDto;
    private UserReadDto userReadDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existId = 1L;
        anotherId = 5L;
        existNumber = 1;

        existDate = LocalDate.of(2032, 12, 12);

        testLogin = "Malkolm Stone";
        testPassword = "4213";
        validStringDate = "2032-12-12";
        notValidStringDate = "12-03-2043";

        testUserRoleAdmin = User.builder().userId(existId).login(testLogin).password(testPassword).role(Role.ADMIN).build();
        testUserRoleUser = User.builder().userId(existId).login(testLogin).password(testPassword).role(Role.USER).build();

        userReadDto = UserReadDto.builder().userId(existId).login(testLogin).role(Role.USER).build();

        reservationReadDto = ReservationReadDto.builder()
                .reservationId(existId)
                .reservationDate(existDate)
                .slotId(existId)
                .placeId(existId)
                .userId(anotherId)
                .build();
        reservationUpdateDeleteDto = ReservationUpdateDeleteDto.builder()
                .reservationId(existId)
                .reservationDate(existDate)
                .slotId(existId)
                .placeId(existId)
                .userId(existId)
                .build();

        slotReadUpdateDto = SlotReadUpdateDto.builder().slotId(existId).build();
        placeReadUpdateDto = PlaceReadUpdateDto.builder().placeId(existId).build();
    }

    @Test
    void isAdmin_shouldReturnTrue_ifUserRoleAdmin_Test() {
        userDetails = new SecurityUserDetails(testUserRoleAdmin);
        assertThat(parameterChecker.isAdmin(userDetails)).isTrue();
    }

    @Test
    void isAdmin_shouldReturnFalse_ifUserRoleUser_Test() {
        userDetails = new SecurityUserDetails(testUserRoleUser);
        assertThat(parameterChecker.isAdmin(userDetails)).isFalse();
    }

    @Test
    void convertStringDateWithValidate_shouldReturnParsedDate_ifValid_Test() {
        assertThat(parameterChecker.convertStringDateWithValidate(validStringDate)).isEqualTo(existDate);
    }

    @Test
    void convertStringDateWithValidate_shouldReturnException_ifNotValid_Test() {
        assertThatThrownBy(() -> parameterChecker.convertStringDateWithValidate(notValidStringDate))
                .isInstanceOf(ReservationControllerException.class)
                .hasMessageContaining("Значение даты пустое или не верно, ожидается, например - '2007-12-03' !");
    }

    @Test
    void canUpdateOrDelete_correctVoidResult_withoutException_Test() {
        when(reservationService.findById(reservationUpdateDeleteDto.reservationId())).thenReturn(Optional.of(reservationReadDto));
        when(userService.findByLogin(testLogin)).thenReturn(Optional.of(userReadDto));

        /* В данном случае метод void и нас интересует ситуация при которой он не кидает ни одного исключения */
        parameterChecker.canUpdateOrDelete(testLogin, true, reservationUpdateDeleteDto);

        verify(reservationService, times(1)).findById(anyLong());
        verify(userService, times(1)).findByLogin(anyString());
    }

    @Test
    void canUpdateOrDelete_shouldReturnException_haveNoReservation_Test() {
        when(reservationService.findById(reservationUpdateDeleteDto.reservationId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parameterChecker.canUpdateOrDelete(testLogin, true, reservationUpdateDeleteDto))
                .isInstanceOf(ReservationControllerException.class)
                .hasMessageContaining("Бронь для обновления или удаления не найдена!");

        verify(reservationService, times(1)).findById(anyLong());
    }

    @Test
    void canUpdateOrDelete_shouldReturnException_haveNoPermission_Test() {
        when(reservationService.findById(reservationUpdateDeleteDto.reservationId())).thenReturn(Optional.of(reservationReadDto));
        when(userService.findByLogin(testLogin)).thenReturn(Optional.of(userReadDto));

        assertThatThrownBy(() -> parameterChecker.canUpdateOrDelete(testLogin, false, reservationUpdateDeleteDto))
                .isInstanceOf(ReservationControllerException.class)
                .hasMessageContaining("Недостаточно прав на обновление или удаление брони!");

        verify(reservationService, times(1)).findById(anyLong());
        verify(userService, times(1)).findByLogin(anyString());
    }

    @Test
    void isUserCorrect_shouldReturnNoReaction_Test() {
        when(userService.findById(existId)).thenReturn(Optional.of(userReadDto));
        parameterChecker.isUserCorrect(existId);

        verify(userService, times(1)).findById(anyLong());
    }

    @Test
    void isUserCorrect_shouldReturnException_canNotFindUserById_Test() {
        when(userService.findById(existId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parameterChecker.isUserCorrect(existId))
                .isInstanceOf(ReservationControllerException.class)
                .hasMessageContaining("Применен несуществующий идентификатор пользователя!");

        verify(userService, times(1)).findById(anyLong());
    }

    @Test
    void isReservationNotDuplicate_shouldReturnNoReaction_Test() {
        when(reservationService.findByDatePlaceAndSlot(existDate, existId, anotherId)).thenReturn(Optional.empty());
        parameterChecker.isReservationNotDuplicate(existDate, existId, anotherId);

        verify(reservationService, times(1))
                .findByDatePlaceAndSlot(any(LocalDate.class), anyLong(), anyLong());
    }

    @Test
    void isReservationNotDuplicate_shouldReturnException_Test() {
        when(reservationService.findByDatePlaceAndSlot(existDate, existId, anotherId)).thenReturn(Optional.of(reservationReadDto));

        assertThatThrownBy(() -> parameterChecker.isReservationNotDuplicate(existDate, existId, anotherId))
                .isInstanceOf(ReservationControllerException.class)
                .hasMessageContaining("Дублирование брони!");

        verify(reservationService, times(1))
                .findByDatePlaceAndSlot(any(LocalDate.class), anyLong(), anyLong());
    }

    @Test
    void isSlotCorrect_shouldReturnNoReaction_Test() {
        when(slotService.findById(existId)).thenReturn(Optional.of(slotReadUpdateDto));
        parameterChecker.isSlotCorrect(existId);

        verify(slotService, times(1)).findById(anyLong());
    }

    @Test
    void isSlotCorrect_shouldReturnException_Test() {
        when(slotService.findById(existId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parameterChecker.isSlotCorrect(existId))
                .isInstanceOf(ReservationControllerException.class)
                .hasMessageContaining("Попытка использовать несуществующий слот времени!");

        verify(slotService, times(1)).findById(anyLong());
    }

    @Test
    void isPlaceCorrect_shouldReturnNoReaction_Test() {
        when(placeService.findById(existId)).thenReturn(Optional.of(placeReadUpdateDto));
        parameterChecker.isPlaceCorrect(existId);

        verify(placeService, times(1)).findById(anyLong());
    }

    @Test
    void isPlaceCorrect_shouldReturnException_Test() {
        when(placeService.findById(existId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parameterChecker.isPlaceCorrect(existId))
                .isInstanceOf(ReservationControllerException.class)
                .hasMessageContaining("Попытка использовать несуществующее место/зал!");

        verify(placeService, times(1)).findById(anyLong());
    }
}