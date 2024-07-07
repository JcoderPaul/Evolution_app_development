package me.oldboy.mockito.input.repository;

import me.oldboy.input.entity.Hall;
import me.oldboy.input.entity.ReserveUnit;
import me.oldboy.input.entity.User;
import me.oldboy.input.entity.Workplace;
import me.oldboy.input.repository.ReserveBase;
import me.oldboy.input.repository.UserBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class ReserveBaseMockitoTest {

    @Mock
    private UserBase userBase;
    @InjectMocks
    private ReserveBase reserveBase;
    private Map<String, User> testUserMap;
    private Map<Integer, ReserveUnit> testReserveUnitMapOne;
    private Map<Integer, ReserveUnit> testReserveUnitMapTwo;
    private LocalDate testDateOne;
    private LocalDate testDateTwo;
    private ReserveUnit reserveUnitOne;
    private ReserveUnit reserveUnitTwo;
    private ReserveUnit reserveUnitThree;

    @BeforeEach
    public void setup(){
        testDateOne = LocalDate.of(2024,9,12);
        testDateTwo = LocalDate.of(2024,12,19);
        reserveUnitOne = new ReserveUnit(testDateOne, new Hall(1),11);
        reserveUnitTwo = new ReserveUnit(testDateOne, new Workplace(5),12);
        reserveUnitThree = new ReserveUnit(testDateTwo, new Workplace(5),12);
        testUserMap = Map.of("Admin", new User("Admin", "admin"),
                "User", new User("User", "user"));

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void goodReserveSlot() {
        when(userBase.getUsersBase()).thenReturn(testUserMap);

        assertThat(reserveBase.reserveSlot(testUserMap.get("Admin"), reserveUnitOne)).isTrue();
    }

    @Test
    void goodReserveOtherSlotBySameUserOnSameDate() {
        when(userBase.getUsersBase()).thenReturn(testUserMap);

        assertThat(reserveBase.reserveSlot(testUserMap.get("Admin"), reserveUnitOne)).isTrue();
        assertThat(reserveBase.reserveSlot(testUserMap.get("Admin"), reserveUnitTwo)).isTrue();
    }

    @Test
    void removeReserveSlot() {
        testReserveUnitMapOne = new HashMap<>();
        testReserveUnitMapTwo = new HashMap<>();
        testReserveUnitMapOne.put(reserveUnitOne.hashCode(), reserveUnitOne);
        testReserveUnitMapOne.put(reserveUnitTwo.hashCode(), reserveUnitTwo);
        testReserveUnitMapTwo.put(reserveUnitThree.hashCode(), reserveUnitThree);
        testUserMap.get("Admin").getUserReservedUnitList().put(reserveUnitOne.hashCode(), reserveUnitOne);
        testUserMap.get("Admin").getUserReservedUnitList().put(reserveUnitTwo.hashCode(), reserveUnitTwo);
        testUserMap.get("User").getUserReservedUnitList().put(reserveUnitThree.hashCode(), reserveUnitThree);

        reserveBase.getAllReserveSlots().put(testDateOne, testReserveUnitMapOne);
        reserveBase.getAllReserveSlots().put(testDateTwo, testReserveUnitMapTwo);

        when(userBase.getUsersBase()).thenReturn(testUserMap);

        assertThat(reserveBase.removeReserveSlot(testUserMap.get("Admin"), reserveUnitOne)).isTrue();
        assertThat(reserveBase.removeReserveSlot(testUserMap.get("User"), reserveUnitThree)).isTrue();
    }
}