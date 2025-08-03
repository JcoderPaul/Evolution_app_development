package me.oldboy.integration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.dto.jwt.JwtAuthRequest;
import me.oldboy.dto.jwt.JwtAuthResponse;
import me.oldboy.dto.users.UserCreateDto;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.integration.ITBaseStarter;
import me.oldboy.models.entity.options.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthControllerIT extends ITBaseStarter {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;
    private UserCreateDto userCreateNewDto, userCreateNotValidDto, userCreateDuplicateDto;
    private JwtAuthRequest normalRequest, wrongPassRequest, noValidRequest;
    private String regLogin, duplicateLogin, regPass;
    private Long existId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        regLogin = "Malcolm Stone";
        duplicateLogin = "User";
        regPass = "1234";

        existId = 1L;

        userCreateNewDto = UserCreateDto.builder().login(regLogin).password(regPass).role(Role.USER.name()).build();
        userCreateNotValidDto = UserCreateDto.builder().login("sa").password("w2").build();
        userCreateDuplicateDto = UserCreateDto.builder().login("Admin").password(regPass).role(Role.ADMIN.name()).build();

        normalRequest = JwtAuthRequest.builder().login("Admin").password(regPass).build();
        wrongPassRequest = JwtAuthRequest.builder().login("Admin").password("765432").build();
        noValidRequest = JwtAuthRequest.builder().login("we").build();

    }

    @Test
    @SneakyThrows
    void regUser_shouldReturn_RegisteredUser_Test() {
        String strDtoForReg = objectMapper.writeValueAsString(userCreateNewDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForReg))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        UserReadDto savedDto = objectMapper.readValue(strReturn, UserReadDto.class);

        assertAll(
                () -> assertThat(savedDto.login()).isEqualTo(userCreateNewDto.login()),
                () -> assertThat(savedDto.role().name()).isEqualTo(userCreateNewDto.role())
        );
    }

    @Test
    @SneakyThrows
    void regUser_shouldReturnException_duplicateLogin_Test() {
        String strDtoForReg = objectMapper.writeValueAsString(userCreateDuplicateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForReg))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"exceptionMsg\":\"Пользователь с именем '" + userCreateDuplicateDto.login() + "' уже существует!\"}"));
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
                .andExpect(content().string(containsString("The user role can not be EMPTY")));
    }

    @Test
    @SneakyThrows
    void loginUser_shouldReturnAuthResponse_Test() {
        String strToLogin = objectMapper.writeValueAsString(normalRequest);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strToLogin))
                .andExpect(status().isOk())
                .andReturn();

        String strReturn = result.getResponse().getContentAsString();
        JwtAuthResponse jwtAuthResponse = objectMapper.readValue(strReturn, JwtAuthResponse.class);

        assertAll(
                () -> assertThat(jwtAuthResponse.getLogin()).isEqualTo(normalRequest.getLogin()),
                () -> assertThat(jwtAuthResponse.getId()).isEqualTo(existId),
                () -> assertThat(jwtAuthResponse.getAccessToken()).isNotNull()
        );
    }

    @Test
    @SneakyThrows
    void loginUser_shouldReturnException_wrongPassword_Test() {
        String strToLogin = objectMapper.writeValueAsString(wrongPassRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strToLogin))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json("{\"exceptionMsg\":\"Введен неверный пароль!\"}"));
    }

    @Test
    @SneakyThrows
    void loginUser_shouldReturnValidationError_Test() {
        String strToLogin = objectMapper.writeValueAsString(noValidRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strToLogin))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("must not be empty")))
                .andExpect(content().string(containsString("Wrong format (to short/to long)")));
    }
}