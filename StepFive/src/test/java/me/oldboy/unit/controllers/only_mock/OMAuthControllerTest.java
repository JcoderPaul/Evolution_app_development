package me.oldboy.unit.controllers.only_mock;

import lombok.SneakyThrows;
import me.oldboy.config.jwt_config.JwtTokenGenerator;
import me.oldboy.config.security_details.ClientDetailsService;
import me.oldboy.config.security_details.SecurityUserDetails;
import me.oldboy.controllers.AuthController;
import me.oldboy.dto.jwt.JwtAuthRequest;
import me.oldboy.dto.jwt.JwtAuthResponse;
import me.oldboy.dto.users.UserCreateDto;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.exception.user_exception.UserControllerException;
import me.oldboy.models.entity.User;
import me.oldboy.models.entity.options.Role;
import me.oldboy.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OMAuthControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private ClientDetailsService clientDetailsService;
    @Mock
    private JwtTokenGenerator jwtTokenGenerator;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AuthController authController;

    private JwtAuthRequest authRequest;
    private JwtAuthResponse authResponse;
    private SecurityUserDetails securityUserDetails;
    private UserCreateDto userCreateDto;
    private UserReadDto userReadDto;
    private String regLogin, regPass, validJwtToken;
    private Long createdId;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        regLogin = "Malkolm Stone";
        regPass = "1234";

        validJwtToken = "valid-token";

        createdId = 5L;

        userCreateDto = UserCreateDto.builder().login(regLogin).password(regPass).role(Role.USER.name()).build();
        userReadDto = UserReadDto.builder().userId(createdId).login(regLogin).role(Role.USER).build();

        authRequest = JwtAuthRequest.builder().login(regLogin).password(regPass).build();
        authResponse = JwtAuthResponse.builder().id(createdId).login(regLogin).accessToken(validJwtToken).build();

        testUser = User.builder().userId(createdId).login(regLogin).password(regPass).role(Role.USER).build();
        securityUserDetails = new SecurityUserDetails(testUser);
    }

    @Test
    @SneakyThrows
    void regUser_shouldReturnOk_successRegUser_Test() {
        when(userService.findByLogin(regLogin)).thenReturn(Optional.empty());
        when(userService.create(userCreateDto)).thenReturn(createdId);
        when(userService.findById(createdId)).thenReturn(Optional.of(userReadDto));

        ResponseEntity<?> response = authController.regUser(userCreateDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(userReadDto);

        verify(userService, times(1)).findByLogin(anyString());
        verify(userService, times(1)).create(any(UserCreateDto.class));
        verify(userService, times(1)).findById(anyLong());
    }

    @Test
    @SneakyThrows
    void regUser_shouldReturnException_duplicateLogin_Test() {
        when(userService.findByLogin(regLogin)).thenReturn(Optional.of(userReadDto));

        assertThatThrownBy(() -> authController.regUser(userCreateDto))
                .isInstanceOf(UserControllerException.class)
                .hasMessageContaining("Пользователь с именем '" + userCreateDto.login() + "' уже существует!");

        verify(userService, times(1)).findByLogin(anyString());
    }

    @Test
    @SneakyThrows
    void regUser_shouldReturnException_unExpectedError_Test() {
        when(userService.findByLogin(regLogin)).thenReturn(Optional.empty());
        when(userService.create(userCreateDto)).thenReturn(createdId);
        when(userService.findById(createdId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = authController.regUser(userCreateDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("{\"message\": \"Check entered data!\"}");

        verify(userService, times(1)).findByLogin(anyString());
        verify(userService, times(1)).create(any(UserCreateDto.class));
        verify(userService, times(1)).findById(anyLong());
    }

    @Test
    void loginUser_shouldReturnResponseWithToken_Test() {
        when(clientDetailsService.loadUserByUsername(authRequest.getLogin())).thenReturn(securityUserDetails);
        when(passwordEncoder.matches(authRequest.getPassword(), securityUserDetails.getPassword())).thenReturn(true);
        when(jwtTokenGenerator.getToken(securityUserDetails.getUser().getUserId(), securityUserDetails.getUsername())).thenReturn(validJwtToken);

        ResponseEntity<?> response = authController.loginUser(authRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(authResponse);

        verify(clientDetailsService, times(1)).loadUserByUsername(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(jwtTokenGenerator, times(1)).getToken(anyLong(), anyString());
    }

    @Test
    void loginUser_shouldReturnException_wrongPasswordEntered_Test() {
        when(clientDetailsService.loadUserByUsername(authRequest.getLogin())).thenReturn(securityUserDetails);
        when(passwordEncoder.matches(authRequest.getPassword(), securityUserDetails.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authController.loginUser(authRequest))
                .isInstanceOf(UserControllerException.class)
                .hasMessageContaining("Введен неверный пароль!");

        verify(clientDetailsService, times(1)).loadUserByUsername(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }
}