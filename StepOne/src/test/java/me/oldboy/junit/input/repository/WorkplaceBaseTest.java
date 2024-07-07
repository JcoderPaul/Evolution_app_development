package me.oldboy.junit.input.repository;

import me.oldboy.input.context.CoworkingContext;
import me.oldboy.input.entity.ReserveUnit;
import me.oldboy.input.entity.User;
import me.oldboy.input.entity.Workplace;
import me.oldboy.input.exeptions.WorkplaceBaseException;
import me.oldboy.input.repository.ReserveBase;
import me.oldboy.input.repository.UserBase;
import me.oldboy.input.repository.WorkplaceBase;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WorkplaceBase.
 */
class WorkplaceBaseTest {

    private static WorkplaceBase workplaceBase;
    private static UserBase userBase;
    private static ReserveBase reserveBase;
    private static User simpleUser;
    private static User adminUser;
    private static LocalDate reserveDate;
    private static Integer numberOfExistingAndReservePlaceForCrudOperation;
    private static Integer numberOfExistingAndNotReservePlaceForCrudOperation;
    private static Integer numberOfNotExistingPlaceForCrudOperation;

    @BeforeAll
    public static void setUp() {
        CoworkingContext.getInstance();
        userBase = CoworkingContext.getUserBase();
        reserveBase = CoworkingContext.getReserveBase();
        workplaceBase = CoworkingContext.getWorkplaceBase();
    }

    @BeforeEach
    public void createReserveUnitForExistingWorkplace(){
        simpleUser = new User("User");
        userBase.getUsersBase().put(simpleUser.getLogin(), simpleUser);
        adminUser = userBase.getUsersBase().get("Admin");

        numberOfExistingAndReservePlaceForCrudOperation = 2;
        numberOfExistingAndNotReservePlaceForCrudOperation = 1;
        numberOfNotExistingPlaceForCrudOperation = 23;
        reserveDate = LocalDate.of(2024, 06,9);

        Integer reserveSlot = 6;
        Workplace workplace =
                workplaceBase.getWorkplaceBase().get(numberOfExistingAndReservePlaceForCrudOperation);
        ReserveUnit reserveUnit = new ReserveUnit(reserveDate, workplace, reserveSlot);
        Integer reserveUnitKey = reserveUnit.hashCode();
        Map<Integer, ReserveUnit> reserveUnitMapByConcreteDate = new HashMap<>();
        reserveUnitMapByConcreteDate.put(reserveUnitKey, reserveUnit);
        reserveBase.getAllReserveSlots().put(reserveDate, reserveUnitMapByConcreteDate);
    }

    @AfterEach
    public void cleanReserveAndWorkplacesDataBase() {
        reserveBase.getAllReserveSlots().clear();
        workplaceBase.getWorkplaceBase().clear();
        workplaceBase.initPlaceBase();
    }

    // Create tests
    @Test
    @DisplayName("Should return new workplace after create - admin permission")
    public void createNonExistingWorkplaceByAdminTest(){
        Workplace mayBeNewWorkplace =
                workplaceBase.createWorkPlace(adminUser, numberOfNotExistingPlaceForCrudOperation);
        assertEquals(mayBeNewWorkplace,
                workplaceBase.getWorkplaceBase().get(numberOfNotExistingPlaceForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after create new workplace by user - admin permission")
    public void createNonExistingWorkplaceBySimpleUserExceptionTest(){
        assertThrows(WorkplaceBaseException.class,
                () -> workplaceBase.createWorkPlace(simpleUser, numberOfNotExistingPlaceForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after create existing workplace - admin permission")
    public void createAnExistingWorkplaceByAdminExceptionTest(){
        assertThrows(WorkplaceBaseException.class,
                () -> workplaceBase.createWorkPlace(adminUser, numberOfExistingAndReservePlaceForCrudOperation));
        assertThrows(WorkplaceBaseException.class,
                () -> workplaceBase.createWorkPlace(adminUser, numberOfExistingAndNotReservePlaceForCrudOperation));
    }

    // Remove tests
    @Test
    @DisplayName("Should return workplace after remove it - admin permission")
    public void removeAnExistingAndNotReserveWorkplaceByAdminTest(){
        Workplace mayBeRemoveWorkplace =
                workplaceBase.getWorkplaceBase().get(numberOfExistingAndNotReservePlaceForCrudOperation);
        assertEquals(mayBeRemoveWorkplace,
                workplaceBase.removeWorkPlace(adminUser, numberOfExistingAndNotReservePlaceForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after remove workplace with reservation - admin permission")
    public void removeExistingAndReserveWorkplaceByAdminExceptionTest(){
        assertThrows(WorkplaceBaseException.class,
                () -> workplaceBase.removeWorkPlace(adminUser, numberOfExistingAndReservePlaceForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after remove non exist workplace - admin permission")
    public void removeNonExistingWorkplaceByAdminExceptionTest(){
        assertThrows(WorkplaceBaseException.class,
                () -> workplaceBase.removeWorkPlace(adminUser, numberOfNotExistingPlaceForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after remove exist workplace " +
                 "by user - have no admin permission")
    public void removeAnExistingAndNotReserveWorkplaceBySimpleUserExceptionTest(){
        assertThrows(WorkplaceBaseException.class,
                () -> workplaceBase.removeWorkPlace(simpleUser, numberOfExistingAndNotReservePlaceForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after remove exist and reserve workplace " +
                 "by user - have no admin permission")
    public void removeExistingAndReserveWorkplaceBySimpleUserExceptionTest(){
        assertThrows(WorkplaceBaseException.class,
                () -> workplaceBase.removeWorkPlace(simpleUser, numberOfExistingAndReservePlaceForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after remove non exist workplace " +
                 "by user - have no admin permission")
    public void removeNonExistingWorkplaceBySimpleUserExceptionTest(){
        assertThrows(WorkplaceBaseException.class,
                () -> workplaceBase.removeWorkPlace(simpleUser, numberOfNotExistingPlaceForCrudOperation));
    }

    // Update tests
    @Test
    @DisplayName("Should return hall after update an exist workplace " +
                 "by admin - admin permission")
    public void updateAnExistingAndNotReserveWorkplaceByAdminTest(){
        Workplace mayBeUpdateHall =
                workplaceBase.updateWorkPlace(adminUser,
                        numberOfExistingAndNotReservePlaceForCrudOperation,
                        numberOfNotExistingPlaceForCrudOperation);
        assertEquals(mayBeUpdateHall,
                workplaceBase.getWorkplaceBase().get(numberOfNotExistingPlaceForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after update an exist and reserve workplace " +
                 "by admin - admin permission")
    public void updateAnExistingAndReserveWorkplaceByAdminExceptionTest(){
        assertThrows(WorkplaceBaseException.class,
                () -> workplaceBase.updateWorkPlace(adminUser,
                        numberOfExistingAndReservePlaceForCrudOperation,
                        numberOfNotExistingPlaceForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after update non exist workplace " +
                 "by admin - admin permission")
    public void updateNonExistingWorkplaceByAdminExceptionTest(){
        Integer newNumberForUpdate = numberOfNotExistingPlaceForCrudOperation + 1;
        assertThrows(WorkplaceBaseException.class,
                () -> workplaceBase.updateWorkPlace(adminUser,
                        numberOfNotExistingPlaceForCrudOperation,
                        newNumberForUpdate));
    }

    @Test
    @DisplayName("Should return exception after update exist and not reservation workplace " +
                 "by user - have no admin permission")
    public void updateAnExistingAndNotReserveWorkplaceBySimpleUserExceptionTest(){
        assertThrows(WorkplaceBaseException.class,
                () -> workplaceBase.updateWorkPlace(simpleUser,
                        numberOfExistingAndNotReservePlaceForCrudOperation,
                        numberOfNotExistingPlaceForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after update exist and reservation workplace " +
                 "by user - have no admin permission")
    public void updateExistingAndReserveWorkplaceBySimpleUserExceptionTest(){
        assertThrows(WorkplaceBaseException.class,
                () -> workplaceBase.updateWorkPlace(simpleUser,
                        numberOfExistingAndReservePlaceForCrudOperation,
                        numberOfNotExistingPlaceForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after update non exist workplace " +
                 "by user - have no admin permission")
    public void updateNonExistingWorkplaceBySimpleUserExceptionTest(){
        Integer newNumberForUpdate = numberOfNotExistingPlaceForCrudOperation + 1;
        assertThrows(WorkplaceBaseException.class,
                () -> workplaceBase.updateWorkPlace(simpleUser,
                        numberOfNotExistingPlaceForCrudOperation,
                                                    newNumberForUpdate));
    }

    // Read tests
    @Test
    @DisplayName("Should return existing workplace in base")
    public void readExistingWorkplaceTest(){
        Workplace mayBeReadHall =
                workplaceBase.readWorkPlace(numberOfExistingAndNotReservePlaceForCrudOperation);
        assertEquals(mayBeReadHall,
                workplaceBase.getWorkplaceBase().get(numberOfExistingAndNotReservePlaceForCrudOperation));
        Workplace mayBeReadHallTwo =
                workplaceBase.readWorkPlace(numberOfExistingAndReservePlaceForCrudOperation);
        assertEquals(mayBeReadHallTwo,
                workplaceBase.getWorkplaceBase().get(numberOfExistingAndReservePlaceForCrudOperation));
    }

    @Test
    @DisplayName("Should return exception after read non existing workplace")
    public void readNonExistingWorkplaceExceptionTest(){
        assertThrows(WorkplaceBaseException.class,
                () -> workplaceBase.readWorkPlace(numberOfNotExistingPlaceForCrudOperation));
    }

    @Test
    @DisplayName("Should return true if base size is increase after add new workplace")
    public void stringViewWorkplaceBaseWithAddNewPlaceTest(){
        Integer lengthBeforeAddition = workplaceBase.stringViewWorkplaceBase().length();
        Integer newPlace = numberOfNotExistingPlaceForCrudOperation;
        workplaceBase.getWorkplaceBase().put(newPlace, new Workplace(newPlace));
        Integer newLengthAfterAddNewHall = workplaceBase.stringViewWorkplaceBase().length();
        assertTrue(newLengthAfterAddNewHall > lengthBeforeAddition);
    }

    @Test
    @DisplayName("Should return true if base size is decrease after remove workplace")
    public void stringViewWorkplaceBaseWithRemovePlaceTest(){
        Integer lengthBeforeDelete = workplaceBase.stringViewWorkplaceBase().length();
        workplaceBase.getWorkplaceBase().remove(numberOfExistingAndNotReservePlaceForCrudOperation);
        Integer newLengthAfterDeleteHall = workplaceBase.stringViewWorkplaceBase().length();
        assertTrue(newLengthAfterDeleteHall < lengthBeforeDelete);
    }
}