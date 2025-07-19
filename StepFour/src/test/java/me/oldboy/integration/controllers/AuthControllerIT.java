package me.oldboy.integration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.controllers.AuthController;
import me.oldboy.dto.jwt.JwtAuthRequest;
import me.oldboy.dto.jwt.JwtAuthResponse;
import me.oldboy.dto.users.UserCreateDto;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.entity.options.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class AuthControllerIT extends TestContainerInit {

    @Autowired
    private AuthController authController;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;
    private UserCreateDto userCreateDto, notValidDto, tryToDuplicateDto;
    private JwtAuthRequest goodJwtAuthRequest, badJwtAuthRequest, goodLoginBadPassword, notValidAuthRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        objectMapper = new ObjectMapper();

        userCreateDto = UserCreateDto.builder()
                .login("Mikael")
                .password("1234")
                .role(Role.USER.name())
                .build();
        notValidDto = UserCreateDto.builder()
                .login("Du")
                .password("1")
                .role(Role.ADMIN.name())
                .build();
        tryToDuplicateDto = UserCreateDto.builder()
                .login("Admin")
                .password("1234")
                .role(Role.USER.name())
                .build();

        goodJwtAuthRequest = JwtAuthRequest.builder()
                .login("Admin")
                .password("1234")
                .build();
        badJwtAuthRequest = JwtAuthRequest.builder()
                .login("Mikael")
                .password("1234")
                .build();
        goodLoginBadPassword = JwtAuthRequest.builder()
                .login("Admin")
                .password("666777")
                .build();
        notValidAuthRequest = JwtAuthRequest.builder()
                .login("Ty")
                .password("4")
                .build();
    }

    @Test
    @SneakyThrows
    void regUser_shouldReturnRegisterReadDto_Test() throws JsonProcessingException {
        String requestBodyData = objectMapper.writeValueAsString(userCreateDto);

        /* Имитируем запрос */
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyData))
                .andExpect(status().isOk())
                .andReturn();

        /* Получим результат запроса, обработаем до нужного формата и сравним с исходным */
        String strReturn = result.getResponse().getContentAsString();

        UserReadDto userReadDto = objectMapper.readValue(strReturn, UserReadDto.class);
        assertAll(
                () -> assertThat(strReturn.contains("userId")).isTrue(),
                () -> assertThat(strReturn.contains("login")).isTrue(),
                () -> assertThat(strReturn.contains("role")).isTrue(),
                () -> assertThat(userCreateDto.login()).isEqualTo(userReadDto.login())
        );
    }

    @Test
    @SneakyThrows
    void regUser_shouldReturnBadRequest_notValidData_Test() throws JsonProcessingException {
        String requestBodyData = objectMapper.writeValueAsString(notValidDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyData))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"password\" : \"Wrong format (to short/to long)\"")))
                .andExpect(content().string(containsString("\"login\" : \"Wrong format (to short/to long)\"")));
    }

    @Test
    @SneakyThrows
    void regUser_shouldReturnBadRequest_duplicateData_Test() throws JsonProcessingException {
        String requestBodyData = objectMapper.writeValueAsString(tryToDuplicateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyData))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("{\"exceptionMsg\":\"Пользователь с именем 'Admin' уже существует!\"}")));
    }

    @Test
    @SneakyThrows
    void loginUser_shouldReturn_JwtAuthResponse_Test() {
        String requestBodyData = objectMapper.writeValueAsString(goodJwtAuthRequest);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyData))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();

        JwtAuthResponse jwtAuthResponse = objectMapper.readValue(strReturn, JwtAuthResponse.class);
        assertAll(
                () -> assertThat(strReturn.contains("id")).isTrue(),
                () -> assertThat(strReturn.contains("login")).isTrue(),
                () -> assertThat(strReturn.contains("accessToken")).isTrue(),
                () -> assertThat(goodJwtAuthRequest.getLogin()).isEqualTo(jwtAuthResponse.getLogin())
        );
    }

    @Test
    @SneakyThrows
    void loginUser_shouldReturn_loginNotFound_Test() {
        String requestBodyData = objectMapper.writeValueAsString(badJwtAuthRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyData))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("{\"exceptionMsg\":\"User : " + badJwtAuthRequest.getLogin() + " not found!\"}")));
    }

    @Test
    @SneakyThrows
    void loginUser_shouldReturn_wrongPasswordEnter_Test() {
        String requestBodyData = objectMapper.writeValueAsString(goodLoginBadPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyData))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("{\"exceptionMsg\":\"Введен неверный пароль!\"}")));
    }

    @Test
    @SneakyThrows
    void loginUser_shouldReturn_validationError_Test() {
        String requestBodyData = objectMapper.writeValueAsString(notValidAuthRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyData))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"password\" : \"Wrong format (to short/to long)\"")))
                .andExpect(content().string(containsString("\"login\" : \"Wrong format (to short/to long)\"")));
    }
}