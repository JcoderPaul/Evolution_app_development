package me.oldboy.integration.controllers.admin_scope;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.controllers.admin_scope.AdminUserController;
import me.oldboy.dto.jwt.JwtAuthRequest;
import me.oldboy.dto.jwt.JwtAuthResponse;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.dto.users.UserUpdateDeleteDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.entity.options.Role;
import me.oldboy.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class AdminUserControllerIT extends TestContainerInit {

    @Autowired
    private AdminUserController userController;
    @Autowired
    private UserService userService;
    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;
    private JwtAuthRequest adminJwtRequest;
    private ObjectMapper objectMapper;
    private String goodJwtToken, notValidToken;
    private UserUpdateDeleteDto deleteUser, deleteNotExistUser,
            deleteNonCongruentData, deleteNotValidData,
            goodUpdateUserDto, badUpdateUserDto, updateNotValidData;
    private String BEARER_PREFIX = "Bearer ";
    private String HEADER_NAME = "Authorization";

    @BeforeEach
    @SneakyThrows
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();

        objectMapper = new ObjectMapper();

        adminJwtRequest = JwtAuthRequest.builder()
                .login("Admin")
                .password("1234")
                .build();

        deleteUser = UserUpdateDeleteDto.builder()
                .userId(4L)
                .login("UserTwo")
                .password("1234")
                .role(Role.USER.name())
                .build();
        deleteNotExistUser = UserUpdateDeleteDto.builder()
                .userId(6L)
                .login("UserForDelete")
                .password("1234")
                .role(Role.USER.name())
                .build();
        deleteNonCongruentData = UserUpdateDeleteDto.builder()
                .userId(4L)
                .login("User")
                .password("1234")
                .role(Role.USER.name())
                .build();
        deleteNotValidData = UserUpdateDeleteDto.builder()
                .userId(4L)
                .login("Us")
                .password("1e")
                .role(Role.ADMIN.name())
                .build();
        goodUpdateUserDto = UserUpdateDeleteDto.builder()
                .userId(2L)
                .login("UserIsUpdated")
                .password("1645676")
                .role(Role.ADMIN.name())
                .build();
        badUpdateUserDto = UserUpdateDeleteDto.builder()
                .userId(6L)
                .login("badUpdated")
                .password("097809")
                .role(Role.ADMIN.name())
                .build();
        updateNotValidData = deleteNotValidData;

        String requestBodyData = objectMapper.writeValueAsString(adminJwtRequest);

        /* Имитируем запрос */
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyData))
                .andExpect(status().isOk())
                .andReturn();

        /* Получим результат запроса, обработаем и извлекаем токен */
        String strReturn = result.getResponse().getContentAsString();
        JwtAuthResponse jwtAuthResponse = objectMapper.readValue(strReturn, JwtAuthResponse.class);
        goodJwtToken = BEARER_PREFIX + jwtAuthResponse.getAccessToken();
        notValidToken = "not_valid_token";
    }

    @Test
    @SneakyThrows
    void deleteUser_shouldReturnSuccessDelete_Test() {
        String forDeleteUser = objectMapper.writeValueAsString(deleteUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/delete")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeleteUser))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Пользователь удален!")));
    }

    @Test
    @SneakyThrows
    void deleteUser_shouldReturnException_haveNoDeleteUser_Test() {
        String forDeleteUser = objectMapper.writeValueAsString(deleteNotExistUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/delete")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeleteUser))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Пользователь для удаления не найден!\"}"));
    }

    @Test
    @SneakyThrows
    void deleteUser_shouldReturnException_wrongData_Test() {
        String forDeleteUser = objectMapper.writeValueAsString(deleteNonCongruentData);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/delete")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeleteUser))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Вы уверенны что хотите удалить именно этого пользователя?\"}"));
    }

    @Test
    @SneakyThrows
    void deleteUser_shouldReturnException_notValidData_Test() {
        String forDeleteUser = objectMapper.writeValueAsString(deleteNotValidData);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/delete")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forDeleteUser))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"password\" : \"Wrong format (to short/to long)\"")))
                .andExpect(content().string(containsString("\"login\" : \"Wrong format (to short/to long)\"")));
    }

    @Test
    @SneakyThrows
    void deleteUser_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/delete")
                        .header(HEADER_NAME, notValidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    /* Тут мы не будет генерировать JwtToken, а применим @WithUserDetails для проверки работы аутентификации */
    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void deleteUser_shouldReturnForbidden_notAdminAuth_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/delete")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    void updateUser_shouldReturnOk_Test() {
        String forUpdateUser = objectMapper.writeValueAsString(goodUpdateUserDto);
        Optional<UserReadDto> beforeUpdateUser = userService.findById(goodUpdateUserDto.userId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/update")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdateUser))
                .andExpect(status().isOk());

        Optional<UserReadDto> afterUpdateUser = userService.findById(goodUpdateUserDto.userId());

        if (beforeUpdateUser.isPresent() && afterUpdateUser.isPresent()) {
            assertThat(beforeUpdateUser.get().login()).isNotEqualTo(goodUpdateUserDto.login());
            assertThat(afterUpdateUser.get().login()).isEqualTo(goodUpdateUserDto.login());
        }
    }

    @Test
    @SneakyThrows
    void updateUser_shouldReturnException_haveNoUserId_Test() {
        String forUpdateUser = objectMapper.writeValueAsString(badUpdateUserDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/update")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdateUser))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"User id - " + badUpdateUserDto.userId() + " not found!\"}"));
    }

    @Test
    @SneakyThrows
    void updateUser_shouldReturnException_noValidUserDto_Test() {
        String forUpdateUser = objectMapper.writeValueAsString(updateNotValidData);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/update")
                        .header(HEADER_NAME, goodJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forUpdateUser))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"password\" : \"Wrong format (to short/to long)\"")))
                .andExpect(content().string(containsString("\"login\" : \"Wrong format (to short/to long)\"")));
    }

    @Test
    @SneakyThrows
    void updateUser_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/update")
                        .header(HEADER_NAME, notValidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    /* Тут мы не будет генерировать JwtToken, а применим @WithUserDetails для проверки работы аутентификации */
    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void updateUser_shouldReturnForbidden_userNotAdmin_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/update")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    void getAllUser_shouldReturnListSize_Test() {
        List<UserReadDto> allUsersList = userController.getAllUser();
        assertThat(allUsersList.size()).isGreaterThan(3);
    }
}