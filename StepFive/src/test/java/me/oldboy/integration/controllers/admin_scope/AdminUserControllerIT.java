package me.oldboy.integration.controllers.admin_scope;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.dto.users.UserUpdateDeleteDto;
import me.oldboy.integration.ITBaseStarter;
import me.oldboy.models.entity.options.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithUserDetails(value = "Admin", userDetailsServiceBeanName = "clientDetailsService")
class AdminUserControllerIT extends ITBaseStarter {

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;

    private UserUpdateDeleteDto userDeleteExistDto, userNoValidDelete,
            userDeleteNotExistentDto, userDeleteWithNotCorrectDataDto,
            userExistUpdateDto, userNonExistUpdateDto,
            userNoValidUpdateDto;
    private Long existId, nonExistId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        existId = 1L;
        nonExistId = 100L;

        userDeleteExistDto = UserUpdateDeleteDto.builder().userId(existId).login("Admin").password("1234").role(Role.ADMIN.name()).build();
        userDeleteNotExistentDto = UserUpdateDeleteDto.builder().userId(nonExistId).login("Malcolm Stone").password("4321").role(Role.USER.name()).build();
        userDeleteWithNotCorrectDataDto = UserUpdateDeleteDto.builder().userId(existId).login("User").password("1234").role(Role.USER.name()).build();
        userNoValidDelete = UserUpdateDeleteDto.builder().userId(-existId).login("wq").password("e3").build();

        userExistUpdateDto = UserUpdateDeleteDto.builder().userId(existId + 1).login("BigAdmin").password("44332211").role(Role.ADMIN.name()).build();
        userNonExistUpdateDto = UserUpdateDeleteDto.builder().userId(nonExistId).login("Douglas Lind").password("54345").role(Role.USER.name()).build();
        userNoValidUpdateDto = userNoValidDelete;
    }

    /* ----- Тестируем *.deleteUser() ----- */
    @Test
    @SneakyThrows
    void deleteUser_shouldReturnOk_afterSuccessRemove_Test() {
        String strDtoForRemove = objectMapper.writeValueAsString(userDeleteExistDto);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForRemove))
                .andExpect(status().isOk())
                .andExpect(content().json(("{\"message\": \"User removed!\"}")));
    }

    @Test
    @SneakyThrows
    void deleteUser_shouldReturnException_removeNonExistUser_Test() {
        String strDtoForRemove = objectMapper.writeValueAsString(userDeleteNotExistentDto);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForRemove))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json(("{\"exceptionMsg\": \"Пользователь для удаления не найден!\"}")));
    }

    @Test
    @SneakyThrows
    void deleteUser_shouldReturnException_removeUserWithNonCongruentData_Test() {
        String strDtoForRemove = objectMapper.writeValueAsString(userDeleteWithNotCorrectDataDto);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForRemove))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json(("{\"exceptionMsg\": \"Вы уверенны что хотите удалить именно этого пользователя?\"}")));
    }

    @Test
    @SneakyThrows
    void deleteUser_shouldReturnValidationError_Test() {
        String strDtoForRemove = objectMapper.writeValueAsString(userNoValidDelete);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForRemove))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Wrong format (to short/to long)")))
                .andExpect(content().string(containsString("The user role can not be EMPTY")))
                .andExpect(content().string(containsString("User ID must be positive")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void deleteUser_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/users/delete"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    /* ----- Тестируем *.updateUser() ----- */
    @Test
    @SneakyThrows
    void updateUser_shouldReturnOk_afterSuccessUpdate_Test() {
        String strDtoForUpdate = objectMapper.writeValueAsString(userExistUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void updateUser_shouldReturnException_haveNoUserForUpdate_Test() {
        String strDtoForUpdate = objectMapper.writeValueAsString(userNonExistUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json("{\"exceptionMsg\":\"User id - " + userNonExistUpdateDto.userId() + " not found!\"}"));
    }

    @Test
    @SneakyThrows
    void updateUser_shouldReturnValidationError_Test() {
        String strDtoForUpdate = objectMapper.writeValueAsString(userNoValidUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Wrong format (to short/to long)")))
                .andExpect(content().string(containsString("The user role can not be EMPTY")))
                .andExpect(content().string(containsString("User ID must be positive")));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void updateUser_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/update"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    /* ----- Тестируем *.getAllUser() ----- */
    @Test
    @SneakyThrows
    void getAllUser_shouldReturnUserList_Test() {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users/all"))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<UserReadDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<UserReadDto>>() {
        });

        assertThat(listFromResponse.size()).isEqualTo(4);
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = "User", userDetailsServiceBeanName = "clientDetailsService")
    void getAllUser_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users/all"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }
}