package me.oldboy.unit.controllers.web_mvc.admin_scope;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.auditor.core.repository.AuditRepository;
import me.oldboy.controllers.admin_scope.AdminUserController;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.dto.users.UserUpdateDeleteDto;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@WithMockUser(authorities = {"ADMIN"})
class MVCAdminUserControllerTest {

    @MockBean
    private UserService userService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private PlaceRepository placeRepository;
    @MockBean
    private SlotRepository slotRepository;
    @MockBean
    private ReservationRepository reservationRepository;
    @MockBean
    private AuditRepository auditRepository;
    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;

    private UserUpdateDeleteDto userDeleteDto, userUpdateDto, userNoValidDelete;
    private UserReadDto userReadDto, anotherUserDto;
    private Long existId, nonExistId;

    @BeforeAll
    static void setStaticContent() {
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp() {
        existId = 1L;
        nonExistId = 100L;

        userDeleteDto = UserUpdateDeleteDto.builder().userId(existId).login("Malkolm Stone").password("1234").role(Role.ADMIN.name()).build();
        userReadDto = UserReadDto.builder().userId(existId).login("Malkolm Stone").role(Role.ADMIN).build();
        anotherUserDto = UserReadDto.builder().userId(nonExistId).login("Duglas Lind").role(Role.USER).build();
        userNoValidDelete = UserUpdateDeleteDto.builder().userId(-existId).login("er").password("54").role(Role.USER.name()).build();
        userUpdateDto = userDeleteDto;
    }

    /* ------ Тестируем метод *.deleteUser() ------ */
    @Test
    @SneakyThrows
    void deleteUser_shouldReturnOk_afterDeleteUser_Test() {
        when(userService.findByLogin(userDeleteDto.login())).thenReturn(Optional.of(userReadDto));
        when(userService.delete(userDeleteDto.userId())).thenReturn(true);

        String strDtoForRemove = objectMapper.writeValueAsString(userDeleteDto);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForRemove))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(("{\"message\": \"User removed!\"}")));

        /* Верифицируем вызовы */
        verify(userService, times(1)).findByLogin(anyString());
        verify(userService, times(1)).delete(anyLong());
    }

    @Test
    @SneakyThrows
    void deleteUser_shouldReturnException_haveNoUserForDeleter_Test() {
        when(userService.findByLogin(userDeleteDto.login())).thenReturn(Optional.empty());

        String strDtoForRemove = objectMapper.writeValueAsString(userDeleteDto);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForRemove))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content()
                        .json(("{\"exceptionMsg\":\"Пользователь для удаления не найден!\"}")));

        /* Верифицируем вызовы */
        verify(userService, times(1)).findByLogin(anyString());
    }

    @Test
    @SneakyThrows
    void deleteUser_shouldReturnException_notCongruentData_Test() {
        when(userService.findByLogin(userDeleteDto.login())).thenReturn(Optional.of(anotherUserDto));

        String strDtoForRemove = objectMapper.writeValueAsString(userDeleteDto);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForRemove))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content()
                        .json(("{\"exceptionMsg\":\"Вы уверенны что хотите удалить именно этого пользователя?\"}")));

        /* Верифицируем вызовы */
        verify(userService, times(1)).findByLogin(anyString());
    }

    @Test
    @SneakyThrows
    void deleteUser_shouldReturnBadRequest_unExpectedError_Test() {
        when(userService.findByLogin(userDeleteDto.login())).thenReturn(Optional.of(userReadDto));
        when(userService.delete(userDeleteDto.userId())).thenReturn(false);

        String strDtoForRemove = objectMapper.writeValueAsString(userDeleteDto);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForRemove))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().json(("{\"message\": \"Remove failed!\"}")));

        /* Верифицируем вызовы */
        verify(userService, times(1)).findByLogin(anyString());
        verify(userService, times(1)).delete(anyLong());
    }

    @Test
    @SneakyThrows
    void deleteUser_shouldReturnValidationError_Test() {
        String strDtoForRemove = objectMapper.writeValueAsString(userNoValidDelete);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForRemove))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("\"login\" : \"Wrong format (to short/to long)\"")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("\"userId\" : \"User ID must be positive\"")))
                .andExpect(MockMvcResultMatchers.content().string(containsString("\"password\" : \"Wrong format (to short/to long)\"")));
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"USER"})
    void deleteUser_shouldReturnForbidden_Test() {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/users/delete"))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string(("")));
    }

    /* ------ Тестируем метод *.updateUser() ------ */
    @Test
    @SneakyThrows
    void updateUser_shouldReturnTrue_Test() {
        when(userService.update(userUpdateDto)).thenReturn(true);

        String strDtoForUpdate = objectMapper.writeValueAsString(userUpdateDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strDtoForUpdate))
                .andExpect(status().isOk());

        verify(userService, times(1)).update(userUpdateDto);
    }

    @Test
    @SneakyThrows
    void getAllUser_shouldReturnUsersList_Test() {
        List<UserReadDto> testList = List.of(UserReadDto.builder().userId(existId).build(), UserReadDto.builder().userId(nonExistId).build());
        when(userService.findAll()).thenReturn(testList);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users/all"))
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<UserReadDto> listFromResponse = objectMapper.readValue(strResult, new TypeReference<List<UserReadDto>>() {
        });

        assertThat(listFromResponse.size()).isEqualTo(testList.size());

        verify(userService, times(1)).findAll();
    }
}