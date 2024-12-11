package me.oldboy.servlets.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.ReservationController;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.core.dto.reservations.ReservationCreateDto;
import me.oldboy.core.dto.reservations.ReservationReadDto;
import me.oldboy.core.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.exception.NotValidArgumentException;
import me.oldboy.exception.ReservationControllerException;
import me.oldboy.security.JwtAuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ReservationManageServletTest {

    @Mock
    private ReservationController reservationController;
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
    private ReservationManageServlet reservationManageServlet;

    private BufferedReader reqBufferedReader;
    private PrintWriter respPrintWriter;
    private ObjectWriter objectWriter;
    private ReservationReadDto reservationReadDto;
    private ReservationCreateDto reservationCreateDto;
    private ReservationUpdateDeleteDto reservationUpdateDeleteDto;
    private static JwtAuthUser jwtAuthUserAdmin, jwtAuthSimpleUser;
    private String jsonResponse, jsonPrintReservationData, jsonReservationList;
    private static String reservationDate, notValidDate, userId, notValidUserId, placeId, notValidPlaceId;
    private static List<ReservationReadDto> selectedReservationList;

    @BeforeAll
    public static void initUser(){
        jwtAuthUserAdmin = new JwtAuthUser("WowUser", Role.ADMIN, true);
        jwtAuthSimpleUser = new JwtAuthUser("notWowUser", Role.USER, true);
        reservationDate = "2023-10-10";
        notValidDate = "20-12-2034";
        userId = "5";
        notValidUserId = "-5";
        placeId = "6";
        notValidPlaceId = "-6";
        selectedReservationList = List.of(new ReservationReadDto(1L, LocalDate.of(2034,10,10), 2L, 3L, 4L),
                                          new ReservationReadDto(2L, LocalDate.of(2034,10,10), 3L, 4L, 5L));
    }

    @BeforeEach
    public void setUp() throws IOException, NotValidArgumentException {
        /* Настроим имитацию записей для CRUD операций */
        reservationCreateDto = new ReservationCreateDto(LocalDate.of(2034,10,10), 3L, 4L,5L);
        reservationReadDto = new ReservationReadDto(12L, LocalDate.of(2034,10,10), 3L, 4L,5L);
        reservationUpdateDeleteDto = new ReservationUpdateDeleteDto(12L, LocalDate.of(2033,01,01), 1L, 1L,1L);

        /* "Объясним" ObjectWriter-у, как работать с датами */
        ObjectMapper objectMapperForWriter = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapperForWriter.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        objectMapperForWriter.registerModule(module);

        objectWriter = objectMapperForWriter.writer().withDefaultPrettyPrinter();

        /* Stub-им конфигурацию сервлетов */
        when(servletConfig.getServletContext()).thenReturn(servletContext);

        /* Формируем подмену для метода *.writeValue() класса ObjectMapper */
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer respWriter = new OutputStreamWriter(outputStream);
        respPrintWriter = new PrintWriter(respWriter);

        /* Stub-им PrintWriter */
        when(resp.getWriter()).thenReturn(respPrintWriter);

        doAnswer(invocation -> {
            PrintWriter outputPrintWriter = invocation.getArgument(0);
            /* И снова у нас возникает задача вывести на экран разные объекты - список UserReadDto и UserUpdateDeleteDto */
            if(invocation.getArgument(1) instanceof JsonFormResponse) {
                JsonFormResponse response = invocation.getArgument(1);
                jsonResponse = "{\"message\":\"" + response.message() + "\"}";
                outputPrintWriter.write(jsonResponse);
            } else if(invocation.getArgument(1) instanceof ReservationReadDto) {
                ReservationReadDto reservationToPrint = invocation.getArgument(1);
                jsonPrintReservationData = objectWriter.writeValueAsString(reservationToPrint);
                outputPrintWriter.write(jsonPrintReservationData);
            } else if(invocation.getArgument(1) instanceof List) {
                List<ReservationReadDto> listToPrint = invocation.getArgument(1);
                jsonReservationList = objectWriter.writeValueAsString(listToPrint);
                outputPrintWriter.write(jsonReservationList);
        }
            return null;
        }).when(objectMapper).writeValue(any(PrintWriter.class), any());
    }

    @AfterEach
    public void closeAllStream() throws IOException {
        resp.getWriter().close();
    }

    /* Тестируем метод doPost - с функционалом создания новой брони Reservation */

    @Test
    void shouldReturnCreatedReservation_doPostTest() throws IOException, ReservationControllerException {
        stubBufferedReaderForCreateReservation();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, ReservationCreateDto.class)).thenReturn(reservationCreateDto);
        when(reservationController.createReservation(jwtAuthUserAdmin.getLogin(), reservationCreateDto))
                .thenReturn(reservationReadDto);

        reservationManageServlet.doPost(req,resp);

        /* assertThat(сюда закидываем "was" или "actual").isEqualsTo(сюда заливаем "expected") */
        assertThat(objectWriter.writeValueAsString(reservationReadDto)).isEqualTo(jsonPrintReservationData);

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_CREATED);
        verify(reservationController, times(1)).createReservation(anyString(), any(ReservationCreateDto.class));
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(ReservationCreateDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(ReservationReadDto.class));
    }

    @Test
    void shouldThrowExceptionNotCorrectPlace_doPostTest() throws IOException, ReservationControllerException {
        stubBufferedReaderForCreateReservation();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, ReservationCreateDto.class)).thenReturn(reservationCreateDto);
        when(reservationController.createReservation(jwtAuthUserAdmin.getLogin(), reservationCreateDto))
                .thenThrow(new ReservationControllerException("Try to use non-existent place! " +
                        "Попытка использовать несуществующее место/зал!"));

        reservationManageServlet.doPost(req,resp);

        /* assertThat(сюда закидываем "was" или "actual").isEqualsTo(сюда заливаем "expected") */
        assertThat(jsonResponse).isEqualTo("{\"message\":\"Invalid entered data: Try to use non-existent place! " +
                "Попытка использовать несуществующее место/зал!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(reservationController, times(1)).createReservation(anyString(), any(ReservationCreateDto.class));
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(ReservationCreateDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionNotCorrectSlot_doPostTest() throws IOException, ReservationControllerException {
        stubBufferedReaderForCreateReservation();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, ReservationCreateDto.class)).thenReturn(reservationCreateDto);
        when(reservationController.createReservation(jwtAuthUserAdmin.getLogin(), reservationCreateDto))
                .thenThrow(new ReservationControllerException("Try to use non-existent slot! " +
                        "Попытка использовать несуществующий слот времени!"));

        reservationManageServlet.doPost(req,resp);

        /* assertThat(сюда закидываем "was" или "actual").isEqualsTo(сюда заливаем "expected") */
        assertThat(jsonResponse).isEqualTo("{\"message\":\"Invalid entered data: Try to use non-existent slot! " +
                "Попытка использовать несуществующий слот времени!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(reservationController, times(1)).createReservation(anyString(), any(ReservationCreateDto.class));
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(ReservationCreateDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionDuplicateReservation_doPostTest() throws IOException, ReservationControllerException {
        stubBufferedReaderForCreateReservation();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, ReservationCreateDto.class)).thenReturn(reservationCreateDto);
        when(reservationController.createReservation(jwtAuthUserAdmin.getLogin(), reservationCreateDto))
                .thenThrow(new ReservationControllerException("Duplicate reservation! Дублирование брони!"));

        reservationManageServlet.doPost(req,resp);

        /* assertThat(сюда закидываем "was" или "actual").isEqualsTo(сюда заливаем "expected") */
        assertThat(jsonResponse).isEqualTo("{\"message\":\"Invalid entered data: Duplicate reservation! Дублирование брони!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(reservationController, times(1)).createReservation(anyString(), any(ReservationCreateDto.class));
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(ReservationCreateDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Тестируем метод doPut - с функционалом обновления существующей брони Reservation */

    @Test
    void shouldReturnTrueIfUpdateSuccess_doPutTest() throws IOException, ReservationControllerException {
        stubBufferedReaderForUpdateDeleteReservation();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        boolean isAdmin = jwtAuthUserAdmin.getRole() == Role.ADMIN;

        when(objectMapper.readValue(reqBufferedReader, ReservationUpdateDeleteDto.class)).thenReturn(reservationUpdateDeleteDto);
        when(reservationController.updateReservation(jwtAuthUserAdmin.getLogin(), isAdmin, reservationUpdateDeleteDto)).thenReturn(true);

        reservationManageServlet.doPut(req,resp);

        /* assertThat(сюда закидываем "was" или "actual").isEqualsTo(сюда заливаем "expected") */
        assertThat("{\"message\":\"The update was successful! Обновление прошло успешно!\"}").isEqualTo(jsonResponse);

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(reservationController, times(1)).updateReservation(anyString(), anyBoolean(), any(ReservationUpdateDeleteDto.class));
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(ReservationUpdateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /*
    В данной ситуации, как и ранее в тестах метода *.doPost(), со слоя контроллеров из метода *.updateReservation()
    класса ReservationController могут прилетать исключения, поясняющие пользователю (сервису), что пошло не так при
    передаче запроса, например: применен не валидный или не существующий ID пользователя, слота, места, произошло
    дублирование брони в ходе обновления, обновление пытается делать не хозяин записи, т.е. не тот, кто ее создал и не
    ADMIN.

    Отсюда все эти броски исключений будут обрабатываться в сервлете по одному алгоритму, и тестироваться тоже:
    - stub на reservationController.updateReservation() будет имитировать бросок;
    - утверждение будет сравнивать бросок с результатом обработанным в stub-е doAnswer на objectMapper.writeValue()
    в блоке setUp(), аннотированным как @BeforeEach;

    Поэтому тест приведенный ниже является неким шаблоном и будет приведен в единственном экземпляре, поскольку
    возможные оставшиеся броски проверяются так же и оттестированы в тестах слоя контроллеров класса ReservationController
    и тут это будет хардкод если только ..., а давайте сделаем параметризованный тест, см. ниже. Конечно это очень грубая
    имитация (подстановка), поскольку не берется во внимание реальная работа приватных методов, т.к. они протестированы в
    тестах на слое контроллеров, а тут идет проверка логики работы сервлета.
    */

    @ParameterizedTest
    @MethodSource("me.oldboy.servlets.reservation.ReservationManageServletTest#getExceptionArgsForTest")
    void shouldReturnThrowException_doPutTest(JwtAuthUser jwtAuthUser, Throwable throwable, String jsonActualResponse) throws IOException, ReservationControllerException {
        stubBufferedReaderForUpdateDeleteReservation();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUser);

        boolean isAdmin = jwtAuthUser.getRole() == Role.ADMIN;

        when(objectMapper.readValue(reqBufferedReader, ReservationUpdateDeleteDto.class)).thenReturn(reservationUpdateDeleteDto);
        when(reservationController.updateReservation(jwtAuthUser.getLogin(), isAdmin, reservationUpdateDeleteDto))
                .thenThrow(throwable);

        reservationManageServlet.doPut(req,resp);

        /* assertThat(сюда закидываем "was" или "actual").isEqualsTo(сюда заливаем "expected") */
        assertThat(jsonActualResponse).isEqualTo(jsonResponse);

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(reservationController, times(1)).updateReservation(anyString(), anyBoolean(), any(ReservationUpdateDeleteDto.class));
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(ReservationUpdateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Тестируем метод doDelete - с функционалом удаления существующей брони Reservation */

    @Test
    void shouldReturnTrueIfDeleteSuccess_doDeleteTest() throws IOException, ReservationControllerException {
        stubBufferedReaderForUpdateDeleteReservation();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        boolean isAdmin = jwtAuthUserAdmin.getRole() == Role.ADMIN;

        when(objectMapper.readValue(reqBufferedReader, ReservationUpdateDeleteDto.class)).thenReturn(reservationUpdateDeleteDto);
        when(reservationController.deleteReservation(jwtAuthUserAdmin.getLogin(), isAdmin, reservationUpdateDeleteDto)).thenReturn(true);

        reservationManageServlet.doDelete(req,resp);

        /* assertThat(сюда закидываем "was" или "actual").isEqualsTo(сюда заливаем "expected") */
        assertThat("{\"message\":\"Deletion was successful! Удаление прошло успешно!\"}").isEqualTo(jsonResponse);

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(reservationController, times(1)).deleteReservation(anyString(), anyBoolean(), any(ReservationUpdateDeleteDto.class));
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(ReservationUpdateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Тут параметризация применена в качестве демонстрации, т.к. половина бросков в запросе *.doDelete() просто не появятся */

    @ParameterizedTest
    @MethodSource("me.oldboy.servlets.reservation.ReservationManageServletTest#getExceptionArgsForTest")
    void shouldReturnThrowException_doDeleteTest(JwtAuthUser jwtAuthUser, Throwable throwable, String jsonActualResponse) throws IOException, ReservationControllerException {
        stubBufferedReaderForUpdateDeleteReservation();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUser);

        boolean isAdmin = jwtAuthUser.getRole() == Role.ADMIN;

        when(objectMapper.readValue(reqBufferedReader, ReservationUpdateDeleteDto.class)).thenReturn(reservationUpdateDeleteDto);
        when(reservationController.deleteReservation(jwtAuthUser.getLogin(), isAdmin, reservationUpdateDeleteDto))
                .thenThrow(throwable);

        reservationManageServlet.doDelete(req,resp);

        /* assertThat(сюда закидываем "was" или "actual").isEqualsTo(сюда заливаем "expected") */
        assertThat(jsonActualResponse).isEqualTo(jsonResponse);

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(reservationController, times(1))
                .deleteReservation(anyString(), anyBoolean(), any(ReservationUpdateDeleteDto.class));
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(ReservationUpdateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Тестируем метод doGet - с функционалом выборки данных по параметрам брони Reservation */

    @Test
    void shouldReturnReservationList_GetReservationByUserId_doGetTest() throws NotValidArgumentException, IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        /* В строке запроса мы можем передать только один параметр, в данном тесте задаем userId */
        when(req.getParameter("userId")).thenReturn(userId);
        when(req.getParameter("placeId")).thenReturn(null);
        when(req.getParameter("reservationDate")).thenReturn(null);

        stubGetReservationByParam();

        reservationManageServlet.doGet(req,resp);

        assertThat(jsonReservationList).isEqualTo(objectWriter.writeValueAsString(selectedReservationList));

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(reservationController, times(1)).getReservationByParam(any(), any(), any());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), anyList());
    }

    @Test
    void shouldReturnReservationList_GetReservationByPlaceId_doGetTest() throws NotValidArgumentException, IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        /* В строке запроса мы можем передать только один параметр, в данном тесте задаем placeId */
        when(req.getParameter("userId")).thenReturn(null);
        when(req.getParameter("placeId")).thenReturn(placeId);
        when(req.getParameter("reservationDate")).thenReturn(null);

        stubGetReservationByParam();

        reservationManageServlet.doGet(req,resp);

        assertThat(jsonReservationList).isEqualTo(objectWriter.writeValueAsString(selectedReservationList));

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(reservationController, times(1)).getReservationByParam(any(), any(), any());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), anyList());
    }

    @Test
    void shouldReturnReservationList_GetReservationByDate_doGetTest() throws NotValidArgumentException, IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        /* В строке запроса мы можем передать только один параметр, в данном тесте задаем reservationDate */
        when(req.getParameter("userId")).thenReturn(null);
        when(req.getParameter("placeId")).thenReturn(null);
        when(req.getParameter("reservationDate")).thenReturn(reservationDate);

        stubGetReservationByParam();

        reservationManageServlet.doGet(req,resp);

        assertThat(jsonReservationList).isEqualTo(objectWriter.writeValueAsString(selectedReservationList));

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(reservationController, times(1)).getReservationByParam(any(), any(), any());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), anyList());
    }

    @Test
    void shouldThrowException_NotValidUserId_doGetTest() throws NotValidArgumentException, IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        when(req.getParameter("userId")).thenReturn(notValidUserId);
        when(req.getParameter("placeId")).thenReturn(null);
        when(req.getParameter("reservationDate")).thenReturn(null);

        stubGetReservationByParam();

        reservationManageServlet.doGet(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Check parameter - must be positive! " +
                                                                  "Проверьте введенный параметр - не может быть отрицательным!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(reservationController, times(1)).getReservationByParam(any(), any(), any());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowException_NotValidPlaceId_doGetTest() throws NotValidArgumentException, IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        when(req.getParameter("userId")).thenReturn(null);
        when(req.getParameter("placeId")).thenReturn(notValidPlaceId);
        when(req.getParameter("reservationDate")).thenReturn(null);

        stubGetReservationByParam();

        reservationManageServlet.doGet(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Check parameter - must be positive! " +
                                                                  "Проверьте введенный параметр - не может быть отрицательным!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(reservationController, times(1)).getReservationByParam(any(), any(), any());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowException_NotValidRegistrationDate_doGetTest() throws NotValidArgumentException, IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        when(req.getParameter("userId")).thenReturn(null);
        when(req.getParameter("placeId")).thenReturn(null);
        when(req.getParameter("reservationDate")).thenReturn(notValidDate);

        stubGetReservationByParam();

        reservationManageServlet.doGet(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Date value is empty or invalid, expected, for example - 'YYYY-MM-DD'! " +
                                                                  "Значение даты пустое или не верно, ожидается, например - '2007-12-03' !\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(reservationController, times(1)).getReservationByParam(any(), any(), any());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowException_MoreThenOneParameter_doGetTest() throws NotValidArgumentException, IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        when(req.getParameter("userId")).thenReturn(userId);
        when(req.getParameter("placeId")).thenReturn(placeId);
        when(req.getParameter("reservationDate")).thenReturn(reservationDate);

        stubGetReservationByParam();

        reservationManageServlet.doGet(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Invalid combination of parameters " +
                "(need only reservationDate or placeId or placeSpecies, not combination)! " +
                "Неверное сочетание параметров (достаточно одного параметра, комбинация не принимается)!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(reservationController, times(1)).getReservationByParam(any(), any(), any());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Stub-ы на BufferedReader, вернее на *.getReader() */

    private void stubBufferedReaderForCreateReservation() throws IOException {
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(objectWriter.writeValueAsString(reservationCreateDto).getBytes());
        Reader reqReader = new InputStreamReader(inputStream);
        reqBufferedReader = new BufferedReader(reqReader);

        when(req.getReader()).thenReturn(reqBufferedReader);
    }

    private void stubBufferedReaderForUpdateDeleteReservation() throws IOException {
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(objectWriter.writeValueAsString(reservationUpdateDeleteDto).getBytes());
        Reader reqReader = new InputStreamReader(inputStream);
        reqBufferedReader = new BufferedReader(reqReader);

        when(req.getReader()).thenReturn(reqBufferedReader);
    }

    /*
    Было любопытно, как поведет себя метод *.doAnswer(), который рекомендуется применять при тестировании void методов. Так же
    хотелось получить функциональную заглушку - stub на метод *.getReservationByParam() класса ReservationController. При первом
    прогоне теста была получена рекомендация использовать конструкцию when().thenReturn(), т.е. для каждой конкретной ситуации
    писать отдельный тест и заглушку - stub, но желание изучить, хотя бы приблизительно конструкцию doAnswer().when() пересилило.
    Для этого, фактически, пришлось перенести функционал заглушаемого метода в заглушку, с некими упрощениями и изменениями, но
    необходимый результат я получил, как и хотел. Хотя сам метод *.getReservationByParam() был протестирован на слое контроллеров,
    тут мы тестировали функционал сервлета при взаимодействии со слоем контроллеров.
    */
    private void stubGetReservationByParam() throws NotValidArgumentException {
        doAnswer(invocation -> {
            String reservationDate = invocation.getArgument(0);
            String userId = invocation.getArgument(1);
            String placeId = invocation.getArgument(2);

            List<ReservationReadDto> reservationByParam = selectedReservationList;
            if(reservationDate != null && userId == null && placeId == null) {
                if(!reservationDate.matches("^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$") || reservationDate.length() == 0){
                    throw new NotValidArgumentException("Date value is empty or invalid, expected, for example - 'YYYY-MM-DD'! " +
                            "Значение даты пустое или не верно, ожидается, например - '2007-12-03' !");
                }
                return reservationByParam;
            } else if (reservationDate == null && userId != null && placeId == null) {
                Long userIdToFindReservation = Long.parseLong(userId);
                if (userIdToFindReservation >= 0) {
                    return reservationByParam;
                } else {
                    throw new NotValidArgumentException("Check parameter - must be positive! " +
                            "Проверьте введенный параметр - не может быть отрицательным!");
                }
            } else if (reservationDate == null && userId == null && placeId != null){
                Long placeIdToFindReservation = Long.parseLong(placeId);
                if (placeIdToFindReservation >= 0) {
                    return reservationByParam;
                } else {
                    throw new NotValidArgumentException("Check parameter - must be positive! " +
                            "Проверьте введенный параметр - не может быть отрицательным!");
                }
            } else {
                throw new NotValidArgumentException("Invalid combination of parameters (need only reservationDate or placeId or placeSpecies, not combination)! " +
                        "Неверное сочетание параметров (достаточно одного параметра, комбинация не принимается)!");
            }
        }).when(reservationController).getReservationByParam(any(), any(), any());
    }

    /*
    Универсальный набор параметров для параметризованного теста (авторизированный пользователь, броски и ответы).
    Например, для тестирования метода *.doPut() на исключения - набор полный, а вот для тестов на исключения метода
    *.doDelete() - актуальны только первый и два последних аргумента потока, просто в силу специфики метода.
    Фактически тесты на исключения с подставленными параметрами демонстрируют работоспособность сервлета в принципе,
    и одного прогона было бы достаточно, но повторение материала - лучшее закрепление!
    */
    static Stream<Arguments> getExceptionArgsForTest(){
        return Stream.of(
                Arguments.of(jwtAuthUserAdmin, new ReservationControllerException("Try to use non-existent userId! Применен несуществующий идентификатор пользователя!"),
                        "{\"message\":\"Invalid entered data: Try to use non-existent userId! Применен несуществующий идентификатор пользователя!\"}"),
                Arguments.of(jwtAuthUserAdmin, new ReservationControllerException("Try to use non-existent place! Попытка использовать несуществующее место/зал!"),
                        "{\"message\":\"Invalid entered data: Try to use non-existent place! Попытка использовать несуществующее место/зал!\"}"),
                Arguments.of(jwtAuthUserAdmin, new ReservationControllerException("Try to use non-existent slot! Попытка использовать несуществующий слот времени!"),
                        "{\"message\":\"Invalid entered data: Try to use non-existent slot! Попытка использовать несуществующий слот времени!\"}"),
                Arguments.of(jwtAuthUserAdmin, new ReservationControllerException("Duplicate reservation! Дублирование брони!"),
                        "{\"message\":\"Invalid entered data: Duplicate reservation! Дублирование брони!\"}"),
                Arguments.of(jwtAuthUserAdmin, new ReservationControllerException("Have no reservation for update or delete! Бронь для обновления или удаления не найдена!"),
                        "{\"message\":\"Invalid entered data: Have no reservation for update or delete! Бронь для обновления или удаления не найдена!\"}"),
                Arguments.of(jwtAuthSimpleUser, new ReservationControllerException("Have no permission to update or delete reservation! Недостаточно прав на обновление или удаление брони!"),
                        "{\"message\":\"Invalid entered data: Have no permission to update or delete reservation! Недостаточно прав на обновление или удаление брони!\"}")
        );
    }
}