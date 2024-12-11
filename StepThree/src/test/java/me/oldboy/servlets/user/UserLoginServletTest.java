package me.oldboy.servlets.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.UserController;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.exception.UserControllerException;
import me.oldboy.security.JwtAuthRequest;
import me.oldboy.security.JwtAuthResponse;
import me.oldboy.servlets.MockServletInputStream;
import me.oldboy.servlets.MockServletOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

/*
Попробуем аннотировать тестовый класс расширением, вместо того что бы инициализировать объекты,
аннотированные с помощью аннотаций Mockito для заданного тестового класса: @Mock, @Spy, @Captor,
@InjectMocks с использованием метода: MockitoAnnotations.openMocks(this), как это делали ранее.
*/
@ExtendWith(MockitoExtension.class)
class UserLoginServletTest {

    @Mock
    private UserController userController;
    /*
    Еще раз, в данной ситуации мы пытаемся "мокать" классы не написанные нами, возможно я чего-то
    недопонимаю, но в документации по Mockito есть некое предупреждения-пожелание не делать этого,
    хотя на https://stackoverflow.com/ можно найти массу примеров игнорирующих данную рекомендацию.
    */
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private HttpServletRequest req;
    @Mock
    private HttpServletResponse resp;
    @InjectMocks
    private UserLoginServlet userLoginServlet;

    private MockServletInputStream mockInputStream;
    private MockServletOutputStream mockOutputStream;

    private JwtAuthRequest jwtAuthRequest;
    private JwtAuthResponse jwtAuthResponse;
    private String strRequestBody;
    private ObjectWriter objectWriter;

    /* Большая часть кода, для тестирования разных ситуаций с вызовом doPost, дублируется, вынесем его в метод "преднастройки" тестов */
    @BeforeEach
    public void setUpTests() throws IOException {
        /* Оформляем JSON для входящего потока в виде строки, она при помощи ObjectMapper будет преобразована в JwtAuthRequest */
        strRequestBody = "{\"login\":\"WowUser\",\"password\":\"223344\"}";

        /* Готовим имитацию запроса и ответа */
        jwtAuthRequest = new JwtAuthRequest("WowUser","223344");
        jwtAuthResponse = new JwtAuthResponse(7L, "WowUser", Role.USER, "generatedAccessToken");

        /* Нам нужно качественно переписать JSON в String наш возвращаемый "response" объект - используем функционал ObjectWriter */
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

        /* Формируем подмену входящего потока на request */
        ByteArrayInputStream inputStream = new ByteArrayInputStream(strRequestBody.getBytes());
        mockInputStream = new MockServletInputStream(inputStream);

        /* Формируем подмену исходящего потока с response */
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mockOutputStream = new MockServletOutputStream(outputStream);

        /* Формируем стандартные stub-ы для тестируемого метода */
        doReturn(mockInputStream).when(req).getInputStream();
        doReturn(mockOutputStream).when(resp).getOutputStream();
        doReturn(jwtAuthRequest).when(objectMapper).readValue(mockInputStream, JwtAuthRequest.class);
        doAnswer(invocation -> {
            OutputStream outputStreamFromDoPostMethod = invocation.getArgument(0);
            /* У нас может быть несколько ситуаций: нормальная работа метода и бросок исключения при различных входящих данных из response */
            if(invocation.getArgument(1) instanceof  JwtAuthResponse || resp.getStatus() ==  202) {
                /* Обрабатываем штатную ситуацию с нормальным входом в приложение */
                JwtAuthResponse jwtAuthResponseToWriter = invocation.getArgument(1);
                String jwtResponseAsJson = objectWriter.writeValueAsString(jwtAuthResponseToWriter);
                /* Записываем результат в исходящий поток */
                outputStreamFromDoPostMethod.write(jwtResponseAsJson.getBytes(StandardCharsets.UTF_8));
            } else if(invocation.getArgument(1) instanceof  JsonFormResponse || resp.getStatus() ==  409 || resp.getStatus() == 400) {
                /* Обрабатываем имитацию броска исключения в тестах */
                JsonFormResponse response = invocation.getArgument(1);
                String jsonResponse = "{\"message\":\"" + response.message() + "\"}";
                outputStreamFromDoPostMethod.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        }).when(objectMapper).writeValue(any(OutputStream.class), any());
    }

    @AfterEach
    public void closeAllStream() throws IOException {
        req.getInputStream().close();
        resp.getOutputStream().close();
    }

    @Test
    void successLoginIn_doPostTest() throws IOException, ServletException {
        /* Формируем stub для успешного прохождения теста */
        doReturn(jwtAuthResponse).when(userController).loginUser(jwtAuthRequest.getLogin(), jwtAuthRequest.getPassword());

        /* Вызываем тестируемый метод */
        userLoginServlet.doPost(req, resp);

        /* Проверяем утверждение */
        assertThat(resp.getOutputStream().toString())
                .isEqualTo(objectWriter.writeValueAsString(jwtAuthResponse));

        /* Проверяем поведение сервлета под тестом, сколько чего было использовано */
        verify(resp, times(1)).setContentType("application/json");
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_ACCEPTED);
        verify(resp, times(2)).getOutputStream();
        verify(req, times(1)).getInputStream();
    }

    @Test
    void failLoginIn_HaveNoLoginInBase_doPostTest() throws IOException, ServletException {
        /* Формируем stub для возвращения исключения - имитируем ситуацию с неверным логином */
        doThrow(new UserControllerException("Login '" + jwtAuthRequest.getLogin() + "' not found! " +
                                            "Пользователь с логином '" + jwtAuthRequest.getLogin() + "' не найден!"))
                .when(userController).loginUser(jwtAuthRequest.getLogin(), jwtAuthRequest.getPassword());

        /* Вызываем тестируемый метод */
        userLoginServlet.doPost(req, resp);

        /* Проверяем утверждение */
        assertThat(resp.getOutputStream().toString())
                .isEqualTo("{\"message\":\"Login 'WowUser' not found! Пользователь с логином 'WowUser' не найден!\"}");

        /* Проверяем поведение сервлета под тестом, сколько чего было использовано */
        verify(resp, times(1)).setContentType("application/json");
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_CONFLICT);
        verify(resp, times(2)).getOutputStream();
        verify(req, times(1)).getInputStream();
    }
}