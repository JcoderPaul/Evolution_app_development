package me.oldboy.servlets.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.UserController;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.core.dto.users.UserCreateDto;
import me.oldboy.exception.UserControllerException;
import me.oldboy.servlets.MockServletInputStream;
import me.oldboy.servlets.MockServletOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserRegServletTest {

    @Mock
    private UserController userController;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private HttpServletRequest req;
    @Mock
    private HttpServletResponse resp;
    @InjectMocks
    private UserRegServlet userRegServlet;

    private UserCreateDto userCreateDto;
    private String inputJson;

    @BeforeEach
    public void setUp(){
        /* Оформляем JSON для входящего потока в виде строки */
        inputJson = "{\"userName\":\"WowUser\",\"password\":\"223344\",\"role\":\"USER\"}";

        userCreateDto = new UserCreateDto("WowUser","223344", "USER");

        MockitoAnnotations.openMocks(this);
    }

    /*
    Тут приведены два теста на успешную регистрацию и на ее провал и большую часть кода из обоих
    тестов можно вынести в общий блок помеченный @BeforeEach, например, но делать тут мы этого не
    будем для наглядности, общего понимания структуры тестов и "физики" происходящего.
    */

    @Test
    void successRegistration_doPostTest() throws IOException {

        /* Формируем подмену входящего потока на request */
        ByteArrayInputStream inputStream = new ByteArrayInputStream(inputJson.getBytes());
        MockServletInputStream mockInputStream = new MockServletInputStream(inputStream);

        /* Формируем подмену исходящего потока с response */
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MockServletOutputStream mockOutputStream = new MockServletOutputStream(outputStream);

        /*
        Настраиваем поведение MOCK объектов для запроса и ответа, для разнообразия применим вместо:

        when(req.getInputStream()).thenReturn(mockInputStream); // Входящий поток
        when(resp.getOutputStream()).thenReturn(mockOutputStream); // Исходящий поток
        when(objectMapper.readValue(mockInputStream, UserCreateDto.class)).thenReturn(userCreateDto);
        when(userController.registrationUser(userCreateDto)).thenReturn(true);

        конструкции другого формата:
        */

        doReturn(mockInputStream).when(req).getInputStream();
        doReturn(mockOutputStream).when(resp).getOutputStream();
        doReturn(userCreateDto).when(objectMapper).readValue(mockInputStream, UserCreateDto.class);
        doReturn(true).when(userController).registrationUser(userCreateDto);

        /*
        Тут возникает интересная ситуация (для меня, по крайней мере). Метод writeValue, можно использовать для
        сериализации любого значения Java в виде выходных данных JSON, используя предоставленный выходной поток
        (используя кодировку JsonEncoding.UTF8), что мы и делаем. Но, он возвращает void - это раз, и не мы его
        написали - это два. А подменить его нам очень охота, чтобы проверить работоспособность нашего сервлета.
        Правило не мокать чужие классы мы уже нарушили подменив response и request, т.ч. гуляем по-полной!

        В данной ситуации мы хотим имитировать ситуацию удачной регистрации пользователя и вернуть в ответ на
        запрос строку "Registration successful. Пользователь зарегистрирован." завернутую в JSON формат.

        Читать эту конструкцию нужно как бы с конца:
        - Шаг 1. - Когда у ObjectMapper будет вызван метод writeValue и в него прилетят, какой-то исходящий поток и
                   что-то еще неопределенное, мы получаем на вход метода 2-а аргумента, на основании которых, он
                   метод должен совершить некую нужную нам логику.
        - Шаг 2. - Именно тут в работу вступает метод doAnswer, в котором происходит считывание первого аргумента -
                   исходящего потока и второго аргумента - сообщения об успехе, из соответствующего вызова метода
                   writeValue в методе doPost нашего тестируемого сервлета, чтобы эмулировать поведение реального
                   ObjectMapper и записать в исходящий поток требуемое сообщение.
        */
        doAnswer(invocation -> {
            OutputStream outputStreamFromDoPostMethod = invocation.getArgument(0);
            JsonFormResponse response = invocation.getArgument(1);

            String jsonResponse = "{\"message\":\"" + response.message() + "\"}";
            outputStreamFromDoPostMethod.write(jsonResponse.getBytes(StandardCharsets.UTF_8));

            return null;
        }).when(objectMapper).writeValue(any(OutputStream.class), any());

        /* Вызываем тестируемый метод */
        userRegServlet.doPost(req, resp);

        assertThat(resp.getOutputStream().toString())
                .isEqualTo("{\"message\":\"Registration successful. Пользователь зарегистрирован.\"}");

        /* Проверяем поведение сервлета под тестом, сколько чего было использовано */
        verify(resp, times(1)).setContentType("application/json");
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_CREATED);
        verify(resp, times(2)).getOutputStream();
        verify(req, times(1)).getInputStream();
    }

    @Test
    void failRegistration_doPostTest() throws IOException {

        /* Формируем подмену входящего потока на request */
        ByteArrayInputStream inputStream = new ByteArrayInputStream(inputJson.getBytes());
        MockServletInputStream mockInputStream = new MockServletInputStream(inputStream);

        /* Формируем подмену исходящего потока с response */
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MockServletOutputStream mockOutputStream = new MockServletOutputStream(outputStream);

        /* Настраиваем поведение MOCK объектов для запроса и ответа: */
        doReturn(mockInputStream).when(req).getInputStream();
        doReturn(mockOutputStream).when(resp).getOutputStream();
        doReturn(userCreateDto).when(objectMapper).readValue(mockInputStream, UserCreateDto.class);
        doThrow(new UserControllerException("User with name ' " + userCreateDto.userName() + " ' is already exist! " +
                                            "Пользователь с именем ' " + userCreateDto.userName() + " ' уже существует!"))
                .when(userController).registrationUser(userCreateDto);

        doAnswer(invocation -> {
            OutputStream outputStreamFromDoPostMethod = invocation.getArgument(0);
            JsonFormResponse response = invocation.getArgument(1);

            String jsonResponse = "{\"message\":\"" + response.message() + "\"}";
            outputStreamFromDoPostMethod.write(jsonResponse.getBytes(StandardCharsets.UTF_8));

            return null;
        }).when(objectMapper).writeValue(any(OutputStream.class), any());

        /* Вызываем тестируемый метод */
        userRegServlet.doPost(req, resp);

        assertThat(resp.getOutputStream().toString())
                .isEqualTo("{\"message\":\"User with name ' WowUser ' is already exist! " +
                                                  "Пользователь с именем ' WowUser ' уже существует!\"}");

        /* Проверяем "поведение сервлета" под тестом, сколько чего было использовано */
        verify(resp, times(1)).setContentType("application/json");
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_CONFLICT);
        verify(resp, times(2)).getOutputStream();
        verify(req, times(1)).getInputStream();
    }
}