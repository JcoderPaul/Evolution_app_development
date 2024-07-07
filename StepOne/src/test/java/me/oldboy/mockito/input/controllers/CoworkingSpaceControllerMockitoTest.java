package me.oldboy.mockito.input.controllers;

import me.oldboy.input.controllers.CoworkingSpaceController;
import me.oldboy.input.entity.Hall;
import me.oldboy.input.entity.Place;
import me.oldboy.input.entity.ReserveUnit;
import me.oldboy.input.entity.User;
import me.oldboy.input.exeptions.ReserveBaseException;
import me.oldboy.input.exeptions.SpaceControllerException;
import me.oldboy.input.repository.ReserveBase;
import me.oldboy.input.repository.UserBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.mockito.*;

/**
 * Tests for CoworkingSpaceController.
 */
class CoworkingSpaceControllerMockitoTest {

    private User testUser;
    private Place testPlace;
    private LocalDate testDate;
    private Integer testSlot;
    private ReserveUnit reserveUnit;

    @Mock
    private UserBase userBase;

    @Mock
    private ReserveBase reserveBase;

    @InjectMocks
    private CoworkingSpaceController coworkingSpaceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User("User");
        testPlace = new Hall(4);
        testDate = LocalDate.of(2028, 05, 03);
        testSlot = 14;
        reserveUnit = new ReserveUnit(testDate, testPlace, testSlot);
    }

    /* Тестирование метода создания нового резервирования */

    @Test
    public void goodReserveSlotTest() {
        when(userBase.isUserRegister(testUser.getLogin())).thenReturn(true);
        when(reserveBase.reserveSlot(testUser, reserveUnit)).thenReturn(true);

        assertThat(coworkingSpaceController.reserveSlot(testUser, testPlace, testDate, testSlot)).isTrue();
    }

    @Test
    public void nonRegisterUserReserveSlotExceptionTest() {
        when(userBase.isUserRegister(testUser.getLogin())).thenReturn(false);
        when(reserveBase.reserveSlot(testUser, reserveUnit)).thenReturn(true);

        assertThatThrownBy(()->coworkingSpaceController.reserveSlot(testUser, testPlace, testDate, testSlot))
                .isInstanceOf(SpaceControllerException.class)
                .hasMessageContaining("Пользователь не зарегистрирован!");
    }

    @Test
    public void registerUserReserveDuplicateSlotMakeHimselfExceptionTest() {
        testUser.getUserReservedUnitList().put(reserveUnit.hashCode(), reserveUnit);

        when(userBase.isUserRegister(testUser.getLogin())).thenReturn(true);

        assertThatThrownBy(()->coworkingSpaceController.reserveSlot(testUser, testPlace, testDate, testSlot))
                .isInstanceOf(SpaceControllerException.class)
                .hasMessageContaining("Такая бронь уже есть!");
    }

    @Test
    public void registerUserReserveDuplicateSlotMakeSomeoneElseExceptionTest() {
        when(userBase.isUserRegister(testUser.getLogin())).thenReturn(true);
        when(reserveBase.reserveSlot(testUser, reserveUnit)).thenThrow(new ReserveBaseException("Данный слот занят!"));

        assertThatThrownBy(()->coworkingSpaceController.reserveSlot(testUser, testPlace, testDate, testSlot))
                .isInstanceOf(ReserveBaseException.class)
                .hasMessageContaining("Данный слот занят!");
    }

    /* Тестирование метода создания нового резервирования */

    @Test
    public void goodRemoveReservePlacesTest(){
        testUser.getUserReservedUnitList().put(reserveUnit.hashCode(), reserveUnit);

        when(userBase.isUserRegister(testUser.getLogin())).thenReturn(true);
        when(reserveBase.removeReserveSlot(testUser, reserveUnit)).thenReturn(true);

        assertThat(coworkingSpaceController.removeReserveSlot(testUser, testPlace, testDate, testSlot)).isTrue();
    }

    @Test
    public void nonRegisterUserRemoveReserveSlotExceptionTest() {
        when(userBase.isUserRegister(testUser.getLogin())).thenReturn(false);

        assertThatThrownBy(()->coworkingSpaceController.reserveSlot(testUser, testPlace, testDate, testSlot))
                .isInstanceOf(SpaceControllerException.class)
                .hasMessageContaining("Пользователь не зарегистрирован!");
    }

    @Test
    public void registerUserRemoveReserveSlotMakeOtherUserExceptionTest() {
        when(userBase.isUserRegister(testUser.getLogin())).thenReturn(true);

        assertThatThrownBy(()->coworkingSpaceController.removeReserveSlot(testUser, testPlace, testDate, testSlot))
                .isInstanceOf(SpaceControllerException.class)
                .hasMessageContaining("Такой брони не существует или у вас недостаточно прав!");
    }
}