package me.oldboy.junit.input.repository;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.entity.Hall;
import me.oldboy.input.entity.ReserveUnit;
import me.oldboy.input.entity.User;
import me.oldboy.input.exeptions.HallBaseException;
import me.oldboy.input.repository.HallBase;
import me.oldboy.input.repository.ReserveBase;
import me.oldboy.input.repository.UserBase;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HallBase.
 */
class HallBaseTest {

    private static HallBase hallBase;
    private static UserBase userBase;
    private static ReserveBase reserveBase;
    private static User simpleUser;
    private static User adminUser;
    private static LocalDate reserveDate;
    private static Integer numberOfExistingAndReserveHallForCrudOperation;
    private static Integer numberOfExistingAndNotReserveHallForCrudOperation;
    private static Integer numberOfNotExistingHallForCrudOperation;

    @BeforeAll
    public static void setUp() {
        CoworkingContext.getInstance();
        userBase = CoworkingContext.getUserBase();
        reserveBase = CoworkingContext.getReserveBase();
        hallBase = CoworkingContext.getHallBase();
    }

    @BeforeEach
    public void createReserveUnitForExistingHall(){
        simpleUser = new User("User");
        userBase.getUsersBase().put(simpleUser.getLogin(), simpleUser);
        adminUser = userBase.getUsersBase().get("Admin");

        numberOfExistingAndReserveHallForCrudOperation = 2;
        numberOfExistingAndNotReserveHallForCrudOperation = 1;
        numberOfNotExistingHallForCrudOperation = 5;
        reserveDate = LocalDate.of(2024, 06,9);

        Integer reserveSlot = 2;
        Hall hall = hallBase.getHallBase().get(numberOfExistingAndReserveHallForCrudOperation);
        ReserveUnit reserveUnit = new ReserveUnit(reserveDate, hall, reserveSlot);
        Integer reserveUnitKey = reserveUnit.hashCode();
        Map<Integer, ReserveUnit> reserveUnitMapByConcreteDate = new HashMap<>();
        reserveUnitMapByConcreteDate.put(reserveUnitKey, reserveUnit);
        reserveBase.getAllReserveSlots().put(reserveDate, reserveUnitMapByConcreteDate);
    }

    @AfterEach
    public void cleanReserveAndHallDataBase() {
        reserveBase.getAllReserveSlots().clear();
        hallBase.getHallBase().clear();
        hallBase.initHallBase();
    }

    // Create tests
    @Test
    @DisplayName("Should return creating hall")
    public void createNonExistingHallByAdminTest(){
        Hall mayBeNewHall = hallBase.createHall(adminUser, numberOfNotExistingHallForCrudOperation);
        assertEquals(mayBeNewHall, hallBase.getHallBase().get(numberOfNotExistingHallForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception then user creating hall without admin permission")
    public void createNonExistingHallBySimpleUserExceptionTest(){
        assertThrows(HallBaseException.class,
                () -> hallBase.createHall(simpleUser, numberOfNotExistingHallForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception then admin creating existing hall")
    public void createAnExistingHallByAdminExceptionTest(){
        assertThrows(HallBaseException.class,
                () -> hallBase.createHall(adminUser, numberOfExistingAndReserveHallForCrudOperation));
        assertThrows(HallBaseException.class,
                () -> hallBase.createHall(adminUser, numberOfExistingAndNotReserveHallForCrudOperation));
    }

    // Remove tests
    @Test
    @DisplayName("Should return remove hall - admin permission, hall not reservation")
    public void removeAnExistingAndNotReserveHallByAdminTest(){
        Hall mayBeRemoveHall =
                hallBase.getHallBase().get(numberOfExistingAndNotReserveHallForCrudOperation);
        assertEquals(mayBeRemoveHall,
                hallBase.removeHall(adminUser, numberOfExistingAndNotReserveHallForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception then remove hall - admin permission, hall is reservation")
    public void removeExistingAndReserveHallByAdminExceptionTest(){
        assertThrows(HallBaseException.class,
                () -> hallBase.removeHall(adminUser, numberOfExistingAndReserveHallForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception then remove not existing hall - admin permission")
    public void removeNonExistingHallByAdminExceptionTest(){
        assertThrows(HallBaseException.class,
                () -> hallBase.removeHall(adminUser, numberOfNotExistingHallForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception then remove existing hall by user - have no admin permission")
    public void removeAnExistingAndNotReserveHallBySimpleUserExceptionTest(){
        assertThrows(HallBaseException.class,
                () -> hallBase.removeHall(simpleUser, numberOfExistingAndNotReserveHallForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception then remove existing hall by user - have no admin permission")
    public void removeExistingAndReserveHallBySimpleUserExceptionTest(){
        assertThrows(HallBaseException.class,
                () -> hallBase.removeHall(simpleUser, numberOfExistingAndReserveHallForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception to remove not existing hall by user - have no admin permission")
    public void removeNonExistingHallBySimpleUserExceptionTest(){
        assertThrows(HallBaseException.class,
                () -> hallBase.removeHall(simpleUser, numberOfNotExistingHallForCrudOperation));
    }

    // Update tests
    @Test
    @DisplayName("Should return hall after update - with admin permission")
    public void updateAnExistingAndNotReserveHallByAdminTest(){
        Hall mayBeUpdateHall =
                hallBase.updateHall(adminUser,
                                    numberOfExistingAndNotReserveHallForCrudOperation,
                                    numberOfNotExistingHallForCrudOperation);
        assertEquals(mayBeUpdateHall,
                hallBase.getHallBase().get(numberOfNotExistingHallForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after update hall with reservation - admin permission")
    public void updateAnExistingAndReserveHallByAdminExceptionTest(){
        assertThrows(HallBaseException.class,
                () -> hallBase.updateHall(adminUser,
                                          numberOfExistingAndReserveHallForCrudOperation,
                                          numberOfNotExistingHallForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after update non existing hall - admin permission")
    public void updateNonExistingHallByAdminExceptionTest(){
        Integer newNumberForUpdate = numberOfNotExistingHallForCrudOperation + 1;
        assertThrows(HallBaseException.class,
                () -> hallBase.updateHall(adminUser,
                                          numberOfNotExistingHallForCrudOperation,
                                          newNumberForUpdate));
    }

    @Test
    @DisplayName("Should return exception after update existing and non reservation " +
                 "hall by simple user - have no admin permission")
    public void updateAnExistingAndNotReserveHallBySimpleUserExceptionTest(){
        assertThrows(HallBaseException.class,
                () -> hallBase.updateHall(simpleUser,
                        numberOfExistingAndNotReserveHallForCrudOperation,
                        numberOfNotExistingHallForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after update existing and reservation " +
                 "hall by simple user - have no admin permission")
    public void updateExistingAndReserveHallBySimpleUserExceptionTest(){
        assertThrows(HallBaseException.class,
                () -> hallBase.updateHall(simpleUser,
                                          numberOfExistingAndReserveHallForCrudOperation,
                                          numberOfNotExistingHallForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after update non existing " +
                 "hall by simple user - have no admin permission")
    public void updateNonExistingHallBySimpleUserExceptionTest(){
        Integer newNumberForUpdate = numberOfNotExistingHallForCrudOperation + 1;
        assertThrows(HallBaseException.class,
                () -> hallBase.updateHall(simpleUser,
                                          numberOfNotExistingHallForCrudOperation,
                                          newNumberForUpdate));
    }

    // Read tests
    @Test
    @DisplayName("Should return existing hall")
    public void readExistingHallTest(){
        Hall mayBeReadHall =
                hallBase.readHall(numberOfExistingAndNotReserveHallForCrudOperation);
        assertEquals(mayBeReadHall,
                hallBase.getHallBase().get(numberOfExistingAndNotReserveHallForCrudOperation));
        Hall mayBeReadHallTwo =
                hallBase.readHall(numberOfExistingAndReserveHallForCrudOperation);
        assertEquals(mayBeReadHallTwo,
                hallBase.getHallBase().get(numberOfExistingAndReserveHallForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception - try to read non existing hall")
    public void readNonExistingHallExceptionTest(){
        assertThrows(HallBaseException.class,
                () -> hallBase.readHall(numberOfNotExistingHallForCrudOperation));
    }

    @Test
    @DisplayName("Should return true if base size is increase after add new hall")
    public void stringViewHallBaseWithAddNewHallTest(){
        Integer lengthBeforeAddition = hallBase.stringViewHallBase().length();
        Integer newHall = numberOfNotExistingHallForCrudOperation;
        hallBase.getHallBase().put(newHall, new Hall(newHall));
        Integer newLengthAfterAddNewHall = hallBase.stringViewHallBase().length();
        assertTrue(newLengthAfterAddNewHall > lengthBeforeAddition);
    }

    @Test
    @DisplayName("Should return true if base size is decrease after remove hall")
    public void stringViewHallBaseWithRemoveHallTest(){
        Integer lengthBeforeDelete = hallBase.stringViewHallBase().length();
        hallBase.getHallBase().remove(numberOfExistingAndNotReserveHallForCrudOperation);
        Integer newLengthAfterDeleteHall = hallBase.stringViewHallBase().length();
        assertTrue(newLengthAfterDeleteHall < lengthBeforeDelete);
    }
}
