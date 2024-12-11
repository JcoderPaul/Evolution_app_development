package me.oldboy.servlets.slots;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.SlotController;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.core.dto.slots.SlotCreateDeleteDto;
import me.oldboy.core.dto.slots.SlotReadUpdateDto;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.exception.SlotControllerException;
import me.oldboy.security.JwtAuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotsManageServletTest {

    @Mock
    private SlotController slotController;
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
    private SlotsManageServlet slotsManageServlet;

    private BufferedReader reqBufferedReader;
    private PrintWriter respPrintWriter;
    private ObjectWriter objectWriter;
    private SlotCreateDeleteDto slotCreateDeleteDto;
    private SlotReadUpdateDto slotReadUpdateDto;

    private JwtAuthUser jwtAuthUserAdmin, jwtAuthSimpleUser;
    private String jsonResponse, jsonPrintToScreenSlotData, slotId, slotNumber, notValidSlotId, notValidSlotNumber;

    @BeforeEach
    public void setUp() throws IOException {
        /* Настроим имитацию записей для CRUD операций */
        slotCreateDeleteDto = new SlotCreateDeleteDto(20, LocalTime.of(20,00), LocalTime.of(21,00));
        slotReadUpdateDto = new SlotReadUpdateDto(12L, 20, LocalTime.of(20,00), LocalTime.of(21,00));
        slotId = "12";
        slotNumber = "20";
        notValidSlotId = "-12";
        notValidSlotNumber = "-20";

        jwtAuthUserAdmin = new JwtAuthUser("WowUser", Role.ADMIN, true);
        jwtAuthSimpleUser = new JwtAuthUser("notWowUser", Role.USER, false);

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
            } else if(invocation.getArgument(1) instanceof SlotReadUpdateDto) {
                SlotReadUpdateDto slotToPrint = invocation.getArgument(1);
                jsonPrintToScreenSlotData = objectWriter.writeValueAsString(slotToPrint);
                outputPrintWriter.write(jsonPrintToScreenSlotData);
            }

            return null;
        }).when(objectMapper).writeValue(any(PrintWriter.class), any());
    }

    @AfterEach
    public void closeAllStream() throws IOException {
        resp.getWriter().close();
    }

    /* Тестируем метод doPost - с функционалом создания нового Slot-a (проверка с правами ADMIN и без них, остальные тесты на слое контроллеров) */

    @Test
    void shouldReturnCreatedSlot_doPostTest() throws IOException {
        stubBufferedReaderForCreateDeleteSlot();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, SlotCreateDeleteDto.class)).thenReturn(slotCreateDeleteDto);
        when(slotController.createNewSlot(slotCreateDeleteDto, jwtAuthUserAdmin.getLogin())).thenReturn(slotReadUpdateDto);

        slotsManageServlet.doPost(req,resp);

        assertThat(objectWriter.writeValueAsString(slotReadUpdateDto)).isEqualTo(jsonPrintToScreenSlotData);

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_CREATED);
        verify(slotController, times(1)).createNewSlot(any(SlotCreateDeleteDto.class), anyString());
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(SlotCreateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(SlotReadUpdateDto.class));
    }

    @Test
    void shouldThrowExceptionHaveNoPermission_doPostTest() throws IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthSimpleUser);

        slotsManageServlet.doPost(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"You do not have permission to access this page! У вас нет доступа!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Тестируем метод doPut - с функционалом обновления данных о существующем Slot-е */

    @Test
    void shouldReturnUpdatedSlot_doPutTest() throws IOException, SlotControllerException {
        stubBufferedReaderForReadUpdateSlot();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, SlotReadUpdateDto.class)).thenReturn(slotReadUpdateDto);
        when(slotController.updateSlot(slotReadUpdateDto, jwtAuthUserAdmin.getLogin())).thenReturn(true);

        slotsManageServlet.doPut(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"The update was successful! Обновление прошло успешно!\"}");

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(slotController,times(1)).updateSlot(any(SlotReadUpdateDto.class), anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(SlotReadUpdateDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionHaveNoPermission_doPutTest() throws IOException, SlotControllerException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthSimpleUser);

        slotsManageServlet.doPut(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"You do not have permission to access this page! У вас нет доступа!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionHaveNoRecordForUpdate_doPutTest() throws IOException, SlotControllerException {
        stubBufferedReaderForReadUpdateSlot();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, SlotReadUpdateDto.class)).thenReturn(slotReadUpdateDto);
        when(slotController.updateSlot(slotReadUpdateDto, jwtAuthUserAdmin.getLogin()))
                .thenThrow(new SlotControllerException("Slot with ID = " + slotReadUpdateDto.slotId() + " not existent! " +
                                                       "Слот с ID = "  + slotReadUpdateDto.slotId() + " не найден!"));

        slotsManageServlet.doPut(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Slot with ID = " + slotReadUpdateDto.slotId() + " not existent! "  +
                                                                  "Слот с ID = "  + slotReadUpdateDto.slotId() + " не найден!\"}");

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(slotController, times(1)).updateSlot(any(SlotReadUpdateDto.class), anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(SlotReadUpdateDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Тестируем метод doDelete - с функционалом удаления существующего Slot-a */

    @Test
    void shouldReturnTrueIfDeletedSlotSuccess_doDeleteTest() throws IOException, SlotControllerException {
        stubBufferedReaderForCreateDeleteSlot();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, SlotCreateDeleteDto.class)).thenReturn(slotCreateDeleteDto);
        when(slotController.deleteSlot(slotCreateDeleteDto, jwtAuthUserAdmin.getLogin())).thenReturn(true);

        slotsManageServlet.doDelete(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Deletion was successful! Удаление прошло успешно!\"}");

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(slotController,times(1)).deleteSlot(any(SlotCreateDeleteDto.class), anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(SlotCreateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldReturnThrowExceptionHaveNoPermission_doDeleteTest() throws IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthSimpleUser);

        slotsManageServlet.doDelete(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"You do not have permission to access this page! У вас нет доступа!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionHaveNoSlotForDelete_doDeleteTest() throws IOException, SlotControllerException {
        stubBufferedReaderForCreateDeleteSlot();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, SlotCreateDeleteDto.class)).thenReturn(slotCreateDeleteDto);
        when(slotController.deleteSlot(slotCreateDeleteDto, jwtAuthUserAdmin.getLogin()))
                .thenThrow(new SlotControllerException("Have no slot to delete! Слот для удаления не найден!"));

        slotsManageServlet.doDelete(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Have no slot to delete! Слот для удаления не найден!\"}");

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(slotController,times(1)).deleteSlot(any(SlotCreateDeleteDto.class), anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(SlotCreateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Тестируем метод doGet - с функционалом получения Slot-а по заданным параметрам (либо slotId, либо slotNumber) */

    @Test
    void shouldReturnSlotByIdParam_doGetTest() throws IOException, SlotControllerException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        /* В строке запроса мы можем передать только один параметр, в данном тесте задаем slotId */
        when(req.getParameter("slotId")).thenReturn(slotId);
        when(req.getParameter("slotNumber")).thenReturn(null);
        when(slotController.readSlotById(anyLong())).thenReturn(slotReadUpdateDto);

        slotsManageServlet.doGet(req,resp);

        assertThat(jsonPrintToScreenSlotData).isEqualTo(objectWriter.writeValueAsString(slotReadUpdateDto));

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(slotController, times(1)).readSlotById(anyLong());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(SlotReadUpdateDto.class));
    }

    @Test
    void shouldReturnSlotByNumberParam_doGetTest() throws IOException, SlotControllerException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        /* В строке запроса мы можем передать только один параметр, в данном тесте задаем slotId */
        when(req.getParameter("slotId")).thenReturn(null);
        when(req.getParameter("slotNumber")).thenReturn(slotNumber);
        when(slotController.readSlotByNumber(anyInt())).thenReturn(slotReadUpdateDto);

        slotsManageServlet.doGet(req,resp);

        assertThat(jsonPrintToScreenSlotData).isEqualTo(objectWriter.writeValueAsString(slotReadUpdateDto));

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(slotController, times(1)).readSlotByNumber(anyInt());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(SlotReadUpdateDto.class));
    }

    @Test
    void shouldThrowExceptionNotValidIdParam_doGetTest() throws IOException, SlotControllerException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        /* В строке запроса мы можем передать только один параметр, в данном тесте задаем slotId */
        when(req.getParameter("slotId")).thenReturn(notValidSlotId);
        when(req.getParameter("slotNumber")).thenReturn(null);

        slotsManageServlet.doGet(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Invalid parse parameters! ID parameter must be positive! " +
                                                   "ID не может быть отрицательным!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionNotValidNumberParam_doGetTest() throws IOException, SlotControllerException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        /* В строке запроса мы можем передать только один параметр, в данном тесте задаем slotId */
        when(req.getParameter("slotId")).thenReturn(null);
        when(req.getParameter("slotNumber")).thenReturn(notValidSlotNumber);

        slotsManageServlet.doGet(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Invalid parse parameters! Slot number must be positive! " +
                                                                  "Номер слота не может быть отрицательным!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionTowParamRequest_doGetTest() throws IOException, SlotControllerException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        /* В строке запроса мы можем передать только один параметр, в данном тесте задаем slotId */
        when(req.getParameter("slotId")).thenReturn(slotId);
        when(req.getParameter("slotNumber")).thenReturn(slotNumber);

        slotsManageServlet.doGet(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Invalid parse parameters! Invalid combination of parameters (need only slotId or slotNumber)! " +
                                                                  "Неверное сочетание параметров (достаточно ID слота или его номера)!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionHaveNoPermission_doGetTest() throws IOException, SlotControllerException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthSimpleUser);

        slotsManageServlet.doGet(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"You do not have permission to access this page! У вас нет доступа!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Stub-ы на BufferedReader, вернее на *.getReader() */

    private void stubBufferedReaderForCreateDeleteSlot() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(objectWriter.writeValueAsString(slotCreateDeleteDto).getBytes());
        Reader reqReader = new InputStreamReader(inputStream);
        reqBufferedReader = new BufferedReader(reqReader);

        when(req.getReader()).thenReturn(reqBufferedReader);
    }

    private void stubBufferedReaderForReadUpdateSlot() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(objectWriter.writeValueAsString(slotReadUpdateDto).getBytes());
        Reader reqReader = new InputStreamReader(inputStream);
        reqBufferedReader = new BufferedReader(reqReader);

        when(req.getReader()).thenReturn(reqBufferedReader);
    }
}
