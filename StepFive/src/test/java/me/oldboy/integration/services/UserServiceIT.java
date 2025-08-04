package me.oldboy.integration.services;

import me.oldboy.dto.users.UserCreateDto;
import me.oldboy.dto.users.UserUpdateDeleteDto;
import me.oldboy.exception.user_exception.UserServiceException;
import me.oldboy.integration.ITBaseStarter;
import me.oldboy.models.entity.options.Role;
import me.oldboy.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class UserServiceIT extends ITBaseStarter {

    @Autowired
    private UserService userService;

    private UserCreateDto userCreateDto;
    private UserUpdateDeleteDto userUpdateDto, userUpdateNonExistDto;
    private Long existId, nonExistId;
    private String existLogin, nonExistentLogin;

    @BeforeEach
    void setUp() {
        existId = 1L;
        nonExistId = 100L;

        existLogin = "Admin";
        nonExistentLogin = "Malkolm Stone";
    }

    @Test
    void create_shouldReturnGeneratedId_Test() {
        userCreateDto = UserCreateDto.builder().login("Login").password("NewPassword").role(Role.USER.name()).build();

        assertThat(userService.create(userCreateDto)).isGreaterThan(4);
    }

    @Test
    void delete_shouldReturnTrue_ifUserExist_Test() {
        assertThat(userService.delete(existId)).isTrue();
    }

    @Test
    void delete_shouldReturnException_ifUserNonExistent_Test() {
        assertThatThrownBy(() -> userService.delete(nonExistId))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("User with id - " + nonExistId + " not found!");
    }

    @Test
    void update_shouldReturnTrue_ifUpdateSuccess_Test() {
        userUpdateDto = UserUpdateDeleteDto.builder().userId(existId).login("NewLogin").password("AndNewPass").role(Role.USER.name()).build();
        assertThat(userService.update(userUpdateDto)).isTrue();
    }

    @Test
    void update_shouldReturnException_ifUpdateNonExistentUser_Test() {
        userUpdateNonExistDto = UserUpdateDeleteDto.builder().userId(nonExistId).login("Admin").password("1234").role(Role.ADMIN.name()).build();
        assertThatThrownBy(() -> userService.update(userUpdateNonExistDto))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("User id - " + userUpdateNonExistDto.userId() + " not found!");
    }

    @Test
    void findById_shouldReturnTrue_andReadDto_forExistUser_Test() {
        assertThat(userService.findById(existId).isPresent()).isTrue();
    }

    @Test
    void findById_shouldReturnFalse_andOptionalEmpty_forNonExistentUser_Test() {
        assertThat(userService.findById(nonExistId).isPresent()).isFalse();
    }

    @Test
    void findAll_shouldReturnUserRecordList_Test() {
        assertThat(userService.findAll().size()).isEqualTo(4);
    }

    @Test
    void findByLogin_shouldReturnTrue_andReadDto_Test() {
        assertThat(userService.findByLogin(existLogin).isPresent()).isTrue();
    }

    @Test
    void findByLogin_shouldReturnFalse_andOptionalEmpty_Test() {
        assertThat(userService.findByLogin(nonExistentLogin).isPresent()).isFalse();
    }
}