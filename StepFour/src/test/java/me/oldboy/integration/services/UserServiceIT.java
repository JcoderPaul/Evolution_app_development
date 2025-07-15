package me.oldboy.integration.services;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.users.UserCreateDto;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.dto.users.UserUpdateDeleteDto;
import me.oldboy.exception.user_exception.UserServiceException;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.entity.options.Role;
import me.oldboy.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IT
class UserServiceIT extends TestContainerInit {

    @Autowired
    private UserService userService;

    private UserCreateDto createDto;
    private UserUpdateDeleteDto updateDeleteDto, dtoWithNoCorrectId;
    private Long existId, nonExistentId;
    private String existLogin, nonExistentLogin;

    @BeforeEach
    void setUp() {
        createDto = UserCreateDto.builder()
                .login("MalkolmStone")
                .password("12345")
                .role(Role.USER.name())
                .build();

        updateDeleteDto = UserUpdateDeleteDto.builder()
                .userId(3L)
                .login("DuglasLind")
                .password("33442211")
                .role(Role.ADMIN.name())
                .build();

        dtoWithNoCorrectId = UserUpdateDeleteDto.builder()
                .userId(30L)
                .login("MarkYasbek")
                .password("334411")
                .role(Role.ADMIN.name())
                .build();

        existId = 4L;
        nonExistentId = 100L;

        existLogin = "UserThree";
        nonExistentLogin = "Sanara";
    }

    @Test
    void create_shouldReturnIdGreaterThenCurrentMaxId_Test() {
        Long createdId = userService.create(createDto);
        assertThat(createdId).isGreaterThan(4);
    }

    @Test
    void delete_shouldReturnTrue_deletedExistRecord_Test() {
        boolean isDelete = userService.delete(existId);
        assertThat(isDelete).isTrue();
    }

    @Test
    void delete_shouldReturn_Exception_deletedExistRecord_Test() {
        assertThatThrownBy(() -> userService.delete(nonExistentId))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("User with id - " + nonExistentId + " not found!");
    }

    @Test
    void update_shouldReturnTrue_AfterUpdate_Test() {
        Optional<UserReadDto> beforeUpdateUser = userService.findById(updateDeleteDto.userId());
        String oldLogin = null;

        if (beforeUpdateUser.isPresent()) {
            oldLogin = beforeUpdateUser.get().login();
        }
        boolean isUpdateUser = userService.update(updateDeleteDto);
        assertThat(isUpdateUser).isTrue();

        Optional<UserReadDto> afterUpdateUser = userService.findById(updateDeleteDto.userId());
        if (afterUpdateUser.isPresent()) {
            assertThat(afterUpdateUser.get().login()).isEqualTo(updateDeleteDto.login());
            assertThat(afterUpdateUser.get().login()).isNotEqualTo(oldLogin);
        }
    }

    @Test
    void update_shouldReturnException_HaveNoUserIdOnBase_Test() {
        assertThatThrownBy(() -> userService.update(dtoWithNoCorrectId))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("User id - " + dtoWithNoCorrectId.userId() + " not found!");
    }

    @Test
    void findById_shouldReturnFoundDto_Test() {
        Optional<UserReadDto> mayBeUser = userService.findById(existId);
        if (mayBeUser.isPresent()) {
            assertThat(mayBeUser.get()).isNotNull();
            assertThat(mayBeUser.get().userId()).isEqualTo(existId);
        }
    }

    @Test
    void findById_shouldReturnOptionalEmpty_Test() {
        Optional<UserReadDto> mayBeUser = userService.findById(nonExistentId);
        assertThat(mayBeUser.isPresent()).isFalse();
    }

    @Test
    void findAll_shouldReturn_AllUsersDtoList_Test() {
        List<UserReadDto> userReadDtoList = userService.findAll();
        assertThat(userReadDtoList.size()).isGreaterThan(3);
    }

    @Test
    void findByLogin_shouldReturnDtoByLogin_Test() {
        Optional<UserReadDto> mayBeUser = userService.findByLogin(existLogin);
        if (mayBeUser.isPresent()) {
            assertThat(mayBeUser.get()).isNotNull();
            assertThat(mayBeUser.get().login()).isEqualTo(existLogin);
        }
    }

    @Test
    void findByLogin_shouldReturnEmptyOptional_Test() {
        Optional<UserReadDto> mayBeUser = userService.findByLogin(nonExistentLogin);
        assertThat(mayBeUser.isPresent()).isFalse();
    }
}