package me.oldboy.unit.controllers.web_mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.auditor.core.repository.AuditRepository;
import me.oldboy.config.jwt_config.JwtTokenGenerator;
import me.oldboy.config.security_details.ClientDetailsService;
import me.oldboy.config.security_details.SecurityUserDetails;
import me.oldboy.controllers.AuthController;
import me.oldboy.dto.jwt.JwtAuthRequest;
import me.oldboy.dto.jwt.JwtAuthResponse;
import me.oldboy.dto.users.UserCreateDto;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.models.entity.User;
import me.oldboy.models.entity.options.Role;
import me.oldboy.repository.PlaceRepository;
import me.oldboy.repository.ReservationRepository;
import me.oldboy.repository.SlotRepository;
import me.oldboy.repository.UserRepository;
import me.oldboy.services.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class MVCAuthControllerTest {

    @MockBean
    private UserService userService;
    @MockBean
    private ClientDetailsService clientDetailsService;
    @MockBean
    private JwtTokenGenerator jwtTokenGenerator;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ReservationRepository reservationRepository;
    @MockBean
    private PlaceRepository placeRepository;
    @MockBean
    private SlotRepository slotRepository;
    @MockBean
    private AuditRepository auditRepository;
    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;
    private JwtAuthRequest authRequest;
    private JwtAuthResponse authResponse;
    private SecurityUserDetails securityUserDetails;
    private UserCreateDto userCreateDto, userCreateNotValidDto;
    private UserReadDto userReadDto;
    private String regLogin, regPass, validJwtToken;
    private Long createdId;
    private User testUser;

    @BeforeAll
    static void setStaticContent() {
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp() {
        regLogin = "Malkolm Stone";
        regPass = "1234";

        validJwtToken = "valid-token";

        createdId = 5L;

        userCreateDto = UserCreateDto.builder().login(regLogin).password(regPass).role(Role.USER.name()).build();
        userCreateNotValidDto = UserCreateDto.builder().password("w2").build();
        userReadDto = UserReadDto.builder().userId(createdId).login(regLogin).role(Role.USER).build();

        authRequest = JwtAuthRequest.builder().login(regLogin).password(regPass).build();
        authResponse = JwtAuthResponse.builder().id(createdId).login(regLogin).accessToken(validJwtToken).build();

        testUser = User.builder().userId(createdId).login(regLogin).password(regPass).role(Role.USER).build();
        securityUserDetails = new SecurityUserDetails(testUser);
    }

    /* ------ Тестируем *.regUser() ------ */
    @Test
    @SneakyThrows
    void regUser_shouldReturnOk_successRegUser_Test() {
        when(userService.findByLogin(regLogin)).thenReturn(Optional.empty());
        when(userService.create(userCreateDto)).thenReturn(createdId);
        when(userService.findById(createdId)).thenReturn(Optional.of(userReadDto));

        String strDtoForReg = objectMapper.writeValueAsString(userCreateDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForReg))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        UserReadDto savedDto = objectMapper.readValue(strReturn, UserReadDto.class);

        assertAll(
                () -> assertThat(savedDto.login()).isEqualTo(userCreateDto.login()),
                () -> assertThat(savedDto.role().name()).isEqualTo(userCreateDto.role())
        );

        verify(userService, times(1)).findByLogin(anyString());
        verify(userService, times(1)).create(any(UserCreateDto.class));
        verify(userService, times(1)).findById(anyLong());
    }

    @Test
    @SneakyThrows
    void regUser_shouldReturnException_duplicateLogin_Test() {
        when(userService.findByLogin(regLogin)).thenReturn(Optional.of(userReadDto));

        String strDtoForReg = objectMapper.writeValueAsString(userCreateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForReg))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"exceptionMsg\":\"Пользователь с именем '" + userCreateDto.login() + "' уже существует!\"}"));

        verify(userService, times(1)).findByLogin(anyString());
    }

    @Test
    @SneakyThrows
    void regUser_shouldReturnException_unExpectedError_Test() {
        when(userService.findByLogin(regLogin)).thenReturn(Optional.empty());
        when(userService.create(userCreateDto)).thenReturn(createdId);
        when(userService.findById(createdId)).thenReturn(Optional.empty());

        String strDtoForReg = objectMapper.writeValueAsString(userCreateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForReg))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"message\": \"Check entered data!\"}"));

        verify(userService, times(1)).findByLogin(anyString());
        verify(userService, times(1)).create(any(UserCreateDto.class));
        verify(userService, times(1)).findById(anyLong());
    }

    @Test
    @SneakyThrows
    void regUser_shouldReturnValidationError_Test() {
        String strDtoForReg = objectMapper.writeValueAsString(userCreateNotValidDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForReg))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Wrong format (to short/to long)")))
                .andExpect(content().string(containsString("The user role can not be EMPTY")))
                .andExpect(content().string(containsString("The login can not be EMPTY")));
    }

    /* ------ Тестируем *.loginUser() ------ */
    @Test
    @SneakyThrows
    void loginUser_shouldReturnResponseWithToken_Test() {
        when(clientDetailsService.loadUserByUsername(authRequest.getLogin())).thenReturn(securityUserDetails);
        when(passwordEncoder.matches(authRequest.getPassword(), securityUserDetails.getPassword())).thenReturn(true);
        when(jwtTokenGenerator.getToken(securityUserDetails.getUser().getUserId(), securityUserDetails.getUsername())).thenReturn(validJwtToken);

        String strToLogin = objectMapper.writeValueAsString(authRequest);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strToLogin))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        JwtAuthResponse jwtAuthResponse = objectMapper.readValue(strReturn, JwtAuthResponse.class);

        assertAll(
                () -> assertThat(jwtAuthResponse.getLogin()).isEqualTo(authRequest.getLogin()),
                () -> assertThat(jwtAuthResponse.getAccessToken()).isEqualTo(validJwtToken)
        );

        verify(clientDetailsService, times(1)).loadUserByUsername(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(jwtTokenGenerator, times(1)).getToken(anyLong(), anyString());
    }

    @Test
    @SneakyThrows
    void loginUser_shouldReturnException_wrongPasswordEntered_Test() {
        when(clientDetailsService.loadUserByUsername(authRequest.getLogin())).thenReturn(securityUserDetails);
        when(passwordEncoder.matches(authRequest.getPassword(), securityUserDetails.getPassword())).thenReturn(false);

        String strToLogin = objectMapper.writeValueAsString(authRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strToLogin))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"exceptionMsg\":\"Введен неверный пароль!\"}"));

        verify(clientDetailsService, times(1)).loadUserByUsername(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }
}