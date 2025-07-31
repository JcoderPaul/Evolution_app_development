package me.oldboy.unit.controllers.only_mock.admin_scope;

import me.oldboy.controllers.admin_scope.AdminUserController;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.dto.users.UserUpdateDeleteDto;
import me.oldboy.exception.user_exception.UserControllerException;
import me.oldboy.models.entity.options.Role;
import me.oldboy.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class OMAdminUserControllerTest {

    @Mock
    private UserService userService;
    @InjectMocks
    private AdminUserController userController;

    private UserUpdateDeleteDto userDeleteDto, userUpdateDto;
    private UserReadDto userReadDto, anotherUserDto;
    private Long existId, nonExistId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existId = 1L;
        nonExistId = 100L;

        userDeleteDto = UserUpdateDeleteDto.builder().userId(existId).login("Malkolm Stone").password("1234").role(Role.ADMIN.name()).build();
        userReadDto = UserReadDto.builder().userId(existId).login("Malkolm Stone").role(Role.ADMIN).build();
        anotherUserDto = UserReadDto.builder().userId(nonExistId).login("Duglas Goodspeed").role(Role.USER).build();
    }

    @Test
    void deleteUser_shouldReturnOk_afterDeleteUser_Test() {
        when(userService.findByLogin(userDeleteDto.login())).thenReturn(Optional.of(userReadDto));
        when(userService.delete(userDeleteDto.userId())).thenReturn(true);

        ResponseEntity<?> response = userController.deleteUser(userDeleteDto);

        /* Получаем ответ и сравниваем с ожидаемым */
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"message\": \"User removed!\"}");

        /* Верифицируем вызовы */
        verify(userService, times(1)).findByLogin(anyString());
        verify(userService, times(1)).delete(anyLong());
    }

    @Test
    void deleteUser_shouldReturnException_haveNoUserForDeleter_Test() {
        when(userService.findByLogin(userDeleteDto.login())).thenReturn(Optional.empty());

        /* Получаем ответ и сравниваем с ожидаемым */
        assertThatThrownBy(() -> userController.deleteUser(userDeleteDto))
                .isInstanceOf(UserControllerException.class)
                .hasMessageContaining("Пользователь для удаления не найден!");

        /* Верифицируем вызовы */
        verify(userService, times(1)).findByLogin(anyString());
    }

    @Test
    void deleteUser_shouldReturnException_notCongruentData_Test() {
        when(userService.findByLogin(userDeleteDto.login())).thenReturn(Optional.of(anotherUserDto));

        /* Получаем ответ и сравниваем с ожидаемым */
        assertThatThrownBy(() -> userController.deleteUser(userDeleteDto))
                .isInstanceOf(UserControllerException.class)
                .hasMessageContaining("Вы уверенны что хотите удалить именно этого пользователя?");

        /* Верифицируем вызовы */
        verify(userService, times(1)).findByLogin(anyString());
    }

    @Test
    void deleteUser_shouldReturnBadRequest_unExpectedError_Test() {
        when(userService.findByLogin(userDeleteDto.login())).thenReturn(Optional.of(userReadDto));
        when(userService.delete(userDeleteDto.userId())).thenReturn(false);

        ResponseEntity<?> response = userController.deleteUser(userDeleteDto);

        /* Получаем ответ и сравниваем с ожидаемым */
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("{\"message\": \"Remove failed!\"}");

        /* Верифицируем вызовы */
        verify(userService, times(1)).findByLogin(anyString());
        verify(userService, times(1)).delete(anyLong());
    }

    @Test
    void updateUser_shouldReturnTrue_Test() {
        when(userService.update(userUpdateDto)).thenReturn(true);

        assertThat(userController.updateUser(userUpdateDto)).isTrue();

        verify(userService, times(1)).update(userUpdateDto);
    }

    @Test
    void updateUser_shouldReturnFalse_Test() {
        when(userService.update(userUpdateDto)).thenReturn(false);

        assertThat(userController.updateUser(userUpdateDto)).isFalse();

        verify(userService, times(1)).update(userUpdateDto);
    }

    @Test
    void getAllUser_shouldReturnUsersList_Test() {
        List<UserReadDto> testList = List.of(UserReadDto.builder().userId(existId).build(), UserReadDto.builder().userId(nonExistId).build());
        when(userService.findAll()).thenReturn(testList);

        assertThat(userController.getAllUser().size()).isEqualTo(testList.size());

        verify(userService, times(1)).findAll();
    }
}