package me.oldboy.mockito.input.repository;

import me.oldboy.input.entity.User;
import me.oldboy.input.entity.Workplace;
import me.oldboy.input.exeptions.WorkplaceBaseException;
import me.oldboy.input.repository.ReserveBase;
import me.oldboy.input.repository.WorkplaceBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
class WorkplaceBaseMockitoTest {

    @Mock
    private ReserveBase reserveBase;
    @InjectMocks
    private WorkplaceBase workplaceBase;
    private static User adminUser;
    private static Integer numberOfExistingAndReservePlaceForCrudOperation;
    private static Integer numberOfExistingAndNotReservePlaceForCrudOperation;
    private static Integer numberOfNotExistingPlaceForCrudOperation;
    private static Integer numberOfExistingAndNeedRemovePlaceForCrudOperation;

    @BeforeEach
    void beforeEachTests() {
        workplaceBase = new WorkplaceBase();
        workplaceBase.initPlaceBase();
        adminUser = new User("Admin", "admin");
        numberOfExistingAndNotReservePlaceForCrudOperation = 1;
        numberOfExistingAndReservePlaceForCrudOperation = 2;
        numberOfExistingAndNeedRemovePlaceForCrudOperation = 6;
        numberOfNotExistingPlaceForCrudOperation = 15;

        MockitoAnnotations.openMocks(this);
    }

    /* Тесты метода Create не проводим их не надо Mock-ать и они прекрасно покрываются Junit тестами */

    /* Тесты метода Remove */
    @Test
    @DisplayName("1 - Should return remove workplace - admin permission, workplace not reservation")
    public void removeAnExistingAndNotReservePlaceByAdminTest(){
        when(reserveBase.getAllReserveWorkplaces())
                .thenReturn(Set.of(numberOfExistingAndReservePlaceForCrudOperation));
        Workplace existPlace = workplaceBase.getWorkplaceBase()
                .get(numberOfExistingAndNeedRemovePlaceForCrudOperation);
        Workplace removeWorkPlace = workplaceBase.removeWorkPlace(adminUser,
                                                                  numberOfExistingAndNeedRemovePlaceForCrudOperation);
        assertThat(existPlace).isEqualTo(removeWorkPlace);
    }

    @Test
    @DisplayName("2 - Should return exception then remove workplace - admin permission, place is reservation")
    public void removeExistingAndReservePlaceByAdminExceptionTest(){
        when(reserveBase.getAllReserveWorkplaces())
                .thenReturn(Set.of(numberOfExistingAndReservePlaceForCrudOperation));

        assertThatThrownBy(()->workplaceBase.removeWorkPlace(adminUser,
                                                             numberOfExistingAndReservePlaceForCrudOperation))
                .isInstanceOf(WorkplaceBaseException.class)
                .hasMessageContaining("Данное рабочее место удалить нельзя, оно забронировано!");
    }

    /* Тестирование метода Update */

    @Test
    @DisplayName("3 - Should return workplace after update - with admin permission")
    public void updateAnExistingAndNotReservePlaceByAdminTest(){
        when(reserveBase.getAllReserveWorkplaces())
                .thenReturn(Set.of(numberOfExistingAndReservePlaceForCrudOperation));
        Workplace afterUpdatePlace =
                new Workplace(numberOfNotExistingPlaceForCrudOperation);
        Workplace updatingPlace =
                workplaceBase.updateWorkPlace(adminUser,
                                              numberOfExistingAndNotReservePlaceForCrudOperation,
                                              numberOfNotExistingPlaceForCrudOperation);

        assertThat(updatingPlace.toString()).isEqualTo(afterUpdatePlace.toString());
    }

    @Test
    @DisplayName("4 - Should return exception after update workplace with reservation - admin permission")
    public void updateAnExistingAndReservePlaceByAdminExceptionTest(){
        when(reserveBase.getAllReserveWorkplaces())
                .thenReturn(Set.of(numberOfExistingAndReservePlaceForCrudOperation));
        assertThatThrownBy(()->workplaceBase.updateWorkPlace(adminUser,
                                                             numberOfExistingAndReservePlaceForCrudOperation,
                                                   numberOfExistingAndReservePlaceForCrudOperation + 15))
                .isInstanceOf(WorkplaceBaseException.class)
                .hasMessageContaining("Данное рабочее место пока нельзя обновлять, оно забронировано!");
    }

    /* Тесты метода Read не проводим их не надо Mock-ать и они прекрасно покрываются Junit тестами */
}
