package me.oldboy.servlets.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.UserController;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.core.dto.users.UserReadDto;
import me.oldboy.core.dto.users.UserUpdateDeleteDto;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.exception.UserControllerException;
import me.oldboy.security.JwtAuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManageServletTest {

    @Mock
    private UserController userController;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private HttpServletResponse resp;
    @Mock
    private HttpServletRequest req;
    @Mock
    private static ServletContext servletContext;
    @Mock
    private static ServletConfig servletConfig;
    @InjectMocks
    private UserManageServlet userManageServlet;
    private BufferedReader reqBufferedReader;
    private PrintWriter respPrintWriter;
    private ObjectWriter objectWriter;
    private UserUpdateDeleteDto userUpdateDeleteDto;
    private JwtAuthUser jwtAuthUserAdmin, jwtAuthSimpleUser;
    private String jsonResponse, listToJson;
    private List<UserReadDto> listForRead, listToPrint;

    /* Данный набор тестов не интеграционный и фактически мы mock-аем и stub-им все подряд и наши классы и не наши */
    @BeforeEach
    public void setUpTest() throws IOException {
        /* Формируем JSON для входящего потока в виде строки, она при помощи ObjectMapper будет преобразована в UserUpdateDeleteDto */
        String jsonInRequestBody = "{\"userId\":\"8\",\"userName\":\"ToUpdateUser\",\"password\":\"654321\",\"role\":\"USER\"}";
        listForRead = List.of(new UserReadDto(1L, "User_1", Role.ADMIN),
                              new UserReadDto(2L, "User_2", Role.USER));

        /* Формируем подстановку для методов изменения и удаления User из БД */
        userUpdateDeleteDto = new UserUpdateDeleteDto(8L, "ToUpdateUser", "654321", "USER");
        jwtAuthUserAdmin = new JwtAuthUser("WowUser", Role.ADMIN, true);
        jwtAuthSimpleUser = new JwtAuthUser("notWowUser", Role.USER, false);

        /* Нам нужно качественно переписать JSON в String наш возвращаемый "response" объект - используем функционал ObjectWriter */
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

        /* Stub-им объект ServletConfig используемый контейнером сервлетов для передачи информации сервлету во время инициализации */
        when(servletConfig.getServletContext()).thenReturn(servletContext);

        /* Формируем подмену для методов *.readValue() и *.writeValue() класса ObjectMapper */
        ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonInRequestBody.getBytes());
        Reader reqReader = new InputStreamReader(inputStream);
        reqBufferedReader = new BufferedReader(reqReader);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer respWriter = new OutputStreamWriter(outputStream);
        respPrintWriter = new PrintWriter(respWriter);

        /* Формируем стандартные stub-ы для тестируемых методов */
        when(req.getReader()).thenReturn(reqBufferedReader);
        when(resp.getWriter()).thenReturn(respPrintWriter);

        doAnswer(invocation -> {
            PrintWriter outputPrintWriter = invocation.getArgument(0);
            /* И снова у нас возникает задача вывести на экран разные объекты - список UserReadDto и UserUpdateDeleteDto */
            if(invocation.getArgument(1) instanceof JsonFormResponse) {
            JsonFormResponse response = invocation.getArgument(1);
            jsonResponse = "{\"message\":\"" + response.message() + "\"}";
            outputPrintWriter.write(jsonResponse);
            } else if(invocation.getArgument(1) instanceof List) {
                listToPrint = invocation.getArgument(1);
                listToJson = objectWriter.writeValueAsString(listToPrint);
                outputPrintWriter.write(listToJson);
            }

            return null;
        }).when(objectMapper).writeValue(any(PrintWriter.class), any());
    }

    @AfterEach
    public void closeAllStream() throws IOException {
        req.getReader().close();
        resp.getWriter().close();
    }

    /* Тестируем метод *.doPut() - попытку обновить запись о User */

    @Test
    void shouldReturnSuccess_AdminAccess_doPutTest() throws IOException {
        /* Stub-им объект ServletContext, который содержится в объекте ServletConfig, его веб-сервер предоставляет сервлету при его инициализации */
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        /*
        Предполагается, что тут сервлет отработает все четко без бросков исключений, значит нам понадобится задействовать:
        - преобразование полученного в теле запроса JSON объекта в UserUpdateDeleteDto объект;
        - вызов метода *.updateUser() класса UserController;
        */
        when(objectMapper.readValue(reqBufferedReader, UserUpdateDeleteDto.class)).thenReturn(userUpdateDeleteDto);
        when(userController.updateUser(userUpdateDeleteDto, jwtAuthUserAdmin.getLogin())).thenReturn(true);

        /* Вызываем тестируемый метод */
        userManageServlet.doPut(req,resp);

        /* Проверяем утверждение о возвращаемом сообщении */
        assertThat(jsonResponse).isEqualTo("{\"message\":\"Update success! Данные обновлены!\"}");

        /* Верифицируем вызываемые методы в ходе теста */
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(req, times(1)).getReader();

        /* Сервлет конфигурация и сервлет контекст вызывались по два раза каждый за тест: в приватном методе *.isAdmin() и в *.getAuthUserName() */
        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(UserUpdateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldReturnException_NoAdminPermission_doPutTest() throws IOException {
        /* Stub-им объект ServletContext на возврат пользователя без должного доступа к функционалу приложения - стимулируем бросок исключения */
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthSimpleUser);

        /* Поскольку в данном тесте мы получаем бросок исключения, то фактически у нас не будет задействован метод *.readValue() это видно при верификации */
        userManageServlet.doPut(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"You do not have permission to access this page! У вас нет доступа!\"}");

        /* В отличие от успешного теста, в данной ситуации, у нас есть два метода которые не разу не запускались - проверяем это */
        verify(req, never()).getReader();
        verify(objectMapper, never()).readValue(any(BufferedReader.class), eq(UserUpdateDeleteDto.class));

        /* В данном случае был единожды задействован приватный метод *.isAdmin(), отсюда вызов этих методов по разу */
        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());

        /* Тут все более-менее понятно - сообщаем пользователю об ошибке статусом и описывающим текстом */
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldReturnException_HaveNoUserForUpdate_doPutTest() throws IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, UserUpdateDeleteDto.class)).thenReturn(userUpdateDeleteDto);
        when(userController.updateUser(userUpdateDeleteDto, jwtAuthUserAdmin.getLogin())).thenReturn(false);

        userManageServlet.doPut(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Have no user for update! В базе нет данных для обновления!\"}");

        verify(resp, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(resp, times(1)).getWriter();
        verify(req, times(1)).getReader();
        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(UserUpdateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Тестируем метод *.doDelete() - попытку удалить запись о User */

    @Test
    void shouldReturnTrue_doDeleteTest() throws IOException {
        /* Stub-им объект ServletContext, который содержится в объекте ServletConfig, его веб-сервер предоставляет сервлету при его инициализации */
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, UserUpdateDeleteDto.class)).thenReturn(userUpdateDeleteDto);
        when(userController.deleteUser(userUpdateDeleteDto, jwtAuthUserAdmin.getLogin())).thenReturn(true);

        userManageServlet.doDelete(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Remove success! Запись удалена!\"}");

        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(req, times(1)).getReader();
        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(UserUpdateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowException_HaveNoPermission_doDeleteTest() throws IOException {
        /* Stub-им объект ServletContext, который содержится в объекте ServletConfig, его веб-сервер предоставляет сервлету при его инициализации */
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthSimpleUser);

        userManageServlet.doDelete(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"You do not have permission to access this page! У вас нет доступа!\"}");

        verify(req, never()).getReader();
        verify(objectMapper, never()).readValue(any(BufferedReader.class), eq(UserUpdateDeleteDto.class));
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(resp, times(1)).getWriter();
        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Данный тест демонстрирует работу сервлета при броске исключения со слоя контроллеров, из метода *.deleteUser() класса UserController */
    @Test
    void shouldThrowException_HaveNoUserToDelete_doDeleteTest() throws IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, UserUpdateDeleteDto.class)).thenReturn(userUpdateDeleteDto);
        when(userController.deleteUser(userUpdateDeleteDto, jwtAuthUserAdmin.getLogin()))
                .thenThrow(new UserControllerException("Have no user to remove! Пользователь для удаления не найден!"));

        userManageServlet.doDelete(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Have no user to remove! Пользователь для удаления не найден!\"}");

        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(req, times(1)).getReader();
        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(UserUpdateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Тестируем метод *.doGet() - попытку получить полный список User */

    @Test
    void shouldReturnUsersList_doGetTest() throws ServletException, IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(userController.getAllUser()).thenReturn(listForRead);

        userManageServlet.doGet(req, resp);

        assertThat(listToJson).isEqualTo(objectWriter.writeValueAsString(listForRead));

        /* Тут с запросом от пользователя ничего не прилетает и для неких преобразований req.getReader() нам не нужен - его и нет */
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();

        /* Тут мы только проверяем права User-a и имя его ненужно, т.к. данный запрос не "аудируется" - обращений к контексту одно */
        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), anyList());
    }

    @Test
    void shouldThrowException_HaveNoPermission_doGetTest() throws ServletException, IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthSimpleUser);

        userManageServlet.doGet(req, resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"You do not have permission to access this page! У вас нет доступа!\"}");

        /* Тут с запросом от пользователя ничего не прилетает и для неких преобразований req.getReader() нам не нужен - его и нет */
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(resp, times(1)).getWriter();

        /* Тут мы только проверяем права User-a и имя его ненужно, т.к. данный запрос не "аудируется" - обращений к контексту одно */
        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }
}