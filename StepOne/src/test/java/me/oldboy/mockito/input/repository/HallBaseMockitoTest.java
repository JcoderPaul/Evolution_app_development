package me.oldboy.mockito.input.repository;

import me.oldboy.input.entity.Hall;
import me.oldboy.input.entity.User;
import me.oldboy.input.exeptions.HallBaseException;
import me.oldboy.input.repository.HallBase;
import me.oldboy.input.repository.ReserveBase;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests for HallBase.
 */
class HallBaseMockitoTest {

    @Mock
    private ReserveBase reserveBase;
    @InjectMocks
    private HallBase hallBase;
    private static User adminUser;
    private static Integer numberOfExistingAndReserveHallForCrudOperation;
    private static Integer numberOfExistingAndNotReserveHallForCrudOperation;
    private static Integer numberOfNotExistingHallForCrudOperation;
    private static Integer numberOfExistingAndNeedRemoveHallForCrudOperation;

    @BeforeEach
    void beforeEachTests() {
        hallBase = new HallBase();
        hallBase.initHallBase();
        adminUser = new User("Admin", "admin");
        numberOfExistingAndNotReserveHallForCrudOperation = 1;
        numberOfExistingAndReserveHallForCrudOperation = 2;
        numberOfExistingAndNeedRemoveHallForCrudOperation = 3;
        numberOfNotExistingHallForCrudOperation = 8;

        MockitoAnnotations.openMocks(this);
    }

    /* Тесты метода Create не проводим их не надо Mock-ать и они прекрасно покрываются Junit тестами */

    /* Тесты метода Remove */
    @Test
    @DisplayName("1 - Should return remove hall - admin permission, hall not reservation")
    public void removeAnExistingAndNotReserveHallByAdminTest(){
        when(reserveBase.getAllReserveHalls())
                .thenReturn(Set.of(numberOfExistingAndReserveHallForCrudOperation));
        Hall existHall = hallBase.getHallBase()
                .get(numberOfExistingAndNeedRemoveHallForCrudOperation);
        Hall removeHall = hallBase.removeHall(adminUser, numberOfExistingAndNeedRemoveHallForCrudOperation);

        assertThat(removeHall).isEqualTo(existHall);
    }

    @Test
    @DisplayName("2 - Should return exception then remove hall - admin permission, hall is reservation")
    public void removeExistingAndReserveHallByAdminExceptionTest(){
        when(reserveBase.getAllReserveHalls())
                .thenReturn(Set.of(numberOfExistingAndReserveHallForCrudOperation));

        assertThatThrownBy(()->hallBase.removeHall(adminUser, numberOfExistingAndReserveHallForCrudOperation))
                .isInstanceOf(HallBaseException.class)
                .hasMessageContaining("Данный зал удалить нельзя, он забронирован!");
    }

    /* Тестирование метода Update */

    @Test
    @DisplayName("3 - Should return hall after update - with admin permission")
    public void updateAnExistingAndNotReserveHallByAdminTest(){
        when(reserveBase.getAllReserveHalls()).thenReturn(Set.of(numberOfExistingAndReserveHallForCrudOperation));
        Hall afterUpdateHall = new Hall(numberOfNotExistingHallForCrudOperation);
        Hall updatingHall = hallBase.updateHall(adminUser,
                                                numberOfExistingAndNotReserveHallForCrudOperation,
                                                numberOfNotExistingHallForCrudOperation);

        assertThat(updatingHall.toString()).isEqualTo(afterUpdateHall.toString());
    }

    @Test
    @DisplayName("4 - Should return exception after update hall with reservation - admin permission")
    public void updateAnExistingAndReserveHallByAdminExceptionTest(){
        when(reserveBase.getAllReserveHalls())
                .thenReturn(Set.of(numberOfExistingAndReserveHallForCrudOperation));
        assertThatThrownBy(()->hallBase.updateHall(adminUser,
                                                   numberOfExistingAndReserveHallForCrudOperation,
                                         numberOfExistingAndReserveHallForCrudOperation + 5))
                .isInstanceOf(HallBaseException.class)
                .hasMessageContaining("Данный зал пока нельзя обновлять, он забронирован!");
    }

    /* Тесты метода Read не проводим их не надо Mock-ать и они прекрасно покрываются Junit тестами */
}
