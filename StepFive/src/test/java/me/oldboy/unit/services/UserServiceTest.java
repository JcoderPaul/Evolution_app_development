package me.oldboy.unit.services;

import me.oldboy.dto.users.UserCreateDto;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.dto.users.UserUpdateDeleteDto;
import me.oldboy.exception.user_exception.UserServiceException;
import me.oldboy.mapper.UserMapper;
import me.oldboy.models.entity.User;
import me.oldboy.models.entity.options.Role;
import me.oldboy.repository.UserRepository;
import me.oldboy.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private UserCreateDto userCreateDto;
    private UserUpdateDeleteDto updateDto;
    private UserReadDto userReadDto, userReadDtoTwo;
    private User savedUser, capturedUser, deleteUser, foundUser;
    private Long userId, notExistUserId;
    private String newLogin, newPassword;
    private List<User> usersList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = 1L;
        notExistUserId = 10L;

        newLogin = "Malcolm Stone";
        newPassword = "4321";

        userCreateDto = UserCreateDto.builder().login(newLogin).password(newPassword).role(Role.USER.name()).build();
        savedUser = User.builder().userId(notExistUserId).login(newLogin).password(newPassword).role(Role.USER).build();

        deleteUser = savedUser;

        foundUser = User.builder().userId(userId).login(newLogin + "II").password(newPassword + 1234).role(Role.ADMIN).build();

        updateDto = UserUpdateDeleteDto.builder().userId(notExistUserId).login(newLogin).password(newPassword).role(Role.USER.name()).build();
        userReadDto = UserReadDto.builder().userId(notExistUserId).login(newLogin).role(Role.USER).build();
        userReadDtoTwo = UserReadDto.builder().userId(userId).login(newLogin + 4321).role(Role.ADMIN).build();

        usersList = List.of(savedUser, foundUser);
    }

    @Test
    void create_shouldReturn_createdUserId_Test() {
        /* Чтобы тест не падал из-за отсутствующего userId - заглушим его */
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(passwordEncoder.encode(userCreateDto.password())).thenReturn(userCreateDto.password());

        /* Убьем двух зайцев - проработаем логику метода и проверим движение данных внутри него */
        assertThat(userService.create(userCreateDto)).isEqualTo(notExistUserId);

        /* Нам бы хотелось понять, что же прилетает на вход метода *.save() класса UserRepository */
        verify(userRepository).save(userCaptor.capture());

        /* Перехватываем "прилет" и извлекаем данные */
        capturedUser = userCaptor.getValue();

        /* Сравниваем - то что и ожидалось */
        assertThat(userCreateDto.login()).isEqualTo(capturedUser.getLogin());
        assertThat(userCreateDto.password()).isEqualTo(capturedUser.getPassword());
        assertThat(Role.USER).isEqualTo(capturedUser.getRole());
    }

    @Test
    void delete_shouldReturnTrue_Test() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(deleteUser));
        assertThat(userService.delete(userId)).isTrue();
    }

    @Test
    void delete_shouldReturnException_Test() {
        when(userRepository.findById(notExistUserId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.delete(notExistUserId))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("User with id - " + notExistUserId + " not found!");
    }

    @Test
    void update_shouldReturnTrueAfterUpdate_Test() {
        when(userRepository.findById(updateDto.userId())).thenReturn(Optional.of(foundUser));
        when(userRepository.save(any(User.class))).thenReturn(foundUser);

        assertThat(userService.update(updateDto)).isTrue();
    }

    @Test
    void update_shouldReturnException_Test() {
        when(userRepository.findById(updateDto.userId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(updateDto))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("User id - " + updateDto.userId() + " not found!");
    }

    @Test
    void findById_shouldReturnDtoFoundUser_Test() {
        when(userRepository.findById(foundUser.getUserId()))
                .thenReturn(Optional.of(foundUser));
        assertThat(userService.findById(foundUser.getUserId()).get())
                .isEqualTo(UserMapper.INSTANCE.mapToUserReadDto(foundUser));
    }

    @Test
    void findById_shouldReturnTrue_AndOptionalEmptyDto_Test() {
        when(userRepository.findById(foundUser.getUserId())).thenReturn(Optional.empty());
        assertThat(userService.findById(foundUser.getUserId()).isEmpty()).isTrue();
    }

    @Test
    void findAll_shouldReturnUserList_Test() {
        when(userRepository.findAll()).thenReturn(usersList);
        assertThat(userService.findAll().size()).isEqualTo(usersList.size());
    }

    @Test
    void findByLogin_shouldReturnFoundUser_Test() {
        when(userRepository.findByLogin(foundUser.getLogin()))
                .thenReturn(Optional.of(foundUser));
        assertThat(userService.findByLogin(foundUser.getLogin()).get())
                .isEqualTo(UserMapper.INSTANCE.mapToUserReadDto(foundUser));
    }

    @Test
    void findByLogin_shouldReturnFalse_andOptionalEmpty_Test() {
        when(userRepository.findByLogin(foundUser.getLogin())).thenReturn(Optional.empty());
        assertThat(userService.findByLogin(foundUser.getLogin()).isPresent()).isFalse();
    }
}