package me.oldboy.servlets.places;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.PlaceController;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.core.dto.places.PlaceCreateDeleteDto;
import me.oldboy.core.dto.places.PlaceReadUpdateDto;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.core.model.database.entity.options.Species;
import me.oldboy.exception.PlaceControllerException;
import me.oldboy.security.JwtAuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PlacesManageServletTest {

    @Mock
    private PlaceController placeController;
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
    private PlacesManageServlet placesManageServlet;

    private BufferedReader reqBufferedReader;
    private PrintWriter respPrintWriter;
    private ObjectWriter objectWriter;
    private PlaceReadUpdateDto placeReadUpdateDto;
    private PlaceCreateDeleteDto placeCreateDeleteDto;

    private JwtAuthUser jwtAuthUserAdmin, jwtAuthSimpleUser;
    private String jsonResponse, jsonPrintToScreenPlaceData,
                   placeId, notValidPlaceId,
                   placeNumber, notValidPlaceNumber,
                   placeSpecies, notValidPlaceSpecies;

    @BeforeEach
    public void setUp() throws IOException {
        /* Настроим имитацию записей для CRUD операций */
        placeCreateDeleteDto = new PlaceCreateDeleteDto(Species.HALL, 4);
        placeReadUpdateDto = new PlaceReadUpdateDto(12L, Species.HALL, 4);
        placeId = "12";
        notValidPlaceId = "-12";
        placeNumber = "4";
        notValidPlaceNumber = "-4";
        placeSpecies = "HALL";
        notValidPlaceSpecies = "GARAGE";

        jwtAuthUserAdmin = new JwtAuthUser("WowUser", Role.ADMIN, true);
        jwtAuthSimpleUser = new JwtAuthUser("notWowUser", Role.USER, true);

        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

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
            } else if(invocation.getArgument(1) instanceof PlaceReadUpdateDto) {
                PlaceReadUpdateDto placeToPrint = invocation.getArgument(1);
                jsonPrintToScreenPlaceData = objectWriter.writeValueAsString(placeToPrint);
                outputPrintWriter.write(jsonPrintToScreenPlaceData);
            }

            return null;
        }).when(objectMapper).writeValue(any(PrintWriter.class), any());
    }

    @AfterEach
    public void closeAllStream() throws IOException {
        resp.getWriter().close();
    }

    /* Тестируем метод doPost - с функционалом создания нового Place-a */

    @Test
    void shouldReturnCreatedDtoPlace_doPostTest() throws IOException, PlaceControllerException {
        stubBufferedReaderForCreateDeletePlace();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, PlaceCreateDeleteDto.class)).thenReturn(placeCreateDeleteDto);
        when(placeController.createNewPlace(placeCreateDeleteDto, jwtAuthUserAdmin.getLogin())).thenReturn(placeReadUpdateDto);

        placesManageServlet.doPost(req,resp);

        assertThat(objectWriter.writeValueAsString(placeReadUpdateDto)).isEqualTo(jsonPrintToScreenPlaceData);

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_CREATED);
        verify(placeController, times(1)).createNewPlace(any(PlaceCreateDeleteDto.class), anyString());
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(PlaceCreateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(PlaceReadUpdateDto.class));
    }

    @Test
    void shouldThrowExceptionHaveNoPermission_doPostTest() throws IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthSimpleUser);

        placesManageServlet.doPost(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"You do not have permission to access this page! У вас нет доступа!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionCreateDuplicate_doPostTest() throws IOException, PlaceControllerException {
        stubBufferedReaderForCreateDeletePlace();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, PlaceCreateDeleteDto.class)).thenReturn(placeCreateDeleteDto);
        when(placeController.createNewPlace(placeCreateDeleteDto, jwtAuthUserAdmin.getLogin()))
                .thenThrow(new PlaceControllerException("Try to create duplicate place! " +
                                                        "Попытка создать дубликат рабочего места/зала!"));

        placesManageServlet.doPost(req,resp);

        assertThat("{\"message\":\"Try to create duplicate place! Попытка создать дубликат рабочего места/зала!\"}").isEqualTo(jsonResponse);

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(placeController, times(1)).createNewPlace(any(PlaceCreateDeleteDto.class), anyString());
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(PlaceCreateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Тестируем метод doPut - с функционалом изменения существующего Place-a */

    @Test
    void shouldReturnTrueIfUpdateSuccess_doPutTest() throws IOException, PlaceControllerException {
        stubBufferedReaderForReadUpdatePlace();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, PlaceReadUpdateDto.class)).thenReturn(placeReadUpdateDto);
        when(placeController.updatePlace(placeReadUpdateDto, jwtAuthUserAdmin.getLogin())).thenReturn(true);

        placesManageServlet.doPut(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"The update was successful! Обновление прошло успешно!\"}");

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(placeController,times(1)).updatePlace(any(PlaceReadUpdateDto.class), anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(PlaceReadUpdateDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionHaveNoPermission_doPutTest() throws IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthSimpleUser);

        placesManageServlet.doPut(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"You do not have permission to access this page! У вас нет доступа!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionHaveNoPlaceForUpdate_doPutTest() throws IOException, PlaceControllerException {
        stubBufferedReaderForReadUpdatePlace();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, PlaceReadUpdateDto.class)).thenReturn(placeReadUpdateDto);
        when(placeController.updatePlace(placeReadUpdateDto, jwtAuthUserAdmin.getLogin()))
                .thenThrow(new PlaceControllerException("Have no place for update! Место или зал для обновления не найдены!"));

        placesManageServlet.doPut(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Have no place for update! Место или зал для обновления не найдены!\"}");

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(placeController,times(1)).updatePlace(any(PlaceReadUpdateDto.class), anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(PlaceReadUpdateDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionDuplicateDataAfterUpdate_doPutTest() throws IOException, PlaceControllerException {
        stubBufferedReaderForReadUpdatePlace();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, PlaceReadUpdateDto.class)).thenReturn(placeReadUpdateDto);
        when(placeController.updatePlace(placeReadUpdateDto, jwtAuthUserAdmin.getLogin()))
                .thenThrow(new PlaceControllerException("Updates will result in data duplication! " +
                                                        "Обновления приведут к дублированию данных!"));

        placesManageServlet.doPut(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Updates will result in data duplication! Обновления приведут к дублированию данных!\"}");

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(placeController,times(1)).updatePlace(any(PlaceReadUpdateDto.class), anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(PlaceReadUpdateDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Тестируем метод doDelete - с функционалом удаления существующего Place-a */

    @Test
    void shouldReturnTrueIfDeleteSuccess_doDeleteTest() throws IOException, PlaceControllerException {
        stubBufferedReaderForCreateDeletePlace();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, PlaceCreateDeleteDto.class)).thenReturn(placeCreateDeleteDto);
        when(placeController.deletePlace(placeCreateDeleteDto, jwtAuthUserAdmin.getLogin())).thenReturn(true);

        placesManageServlet.doDelete(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Deletion was successful! Удаление прошло успешно!\"}");

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(placeController,times(1)).deletePlace(any(PlaceCreateDeleteDto.class), anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(PlaceCreateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldReturnThrowExceptionHaveNoPermission_doDeleteTest() throws IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthSimpleUser);

        placesManageServlet.doDelete(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"You do not have permission to access this page! У вас нет доступа!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionHaveNoPlaceForDelete_doDeleteTest() throws IOException, PlaceControllerException {
        stubBufferedReaderForCreateDeletePlace();
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);
        when(objectMapper.readValue(reqBufferedReader, PlaceCreateDeleteDto.class)).thenReturn(placeCreateDeleteDto);
        when(placeController.deletePlace(placeCreateDeleteDto, jwtAuthUserAdmin.getLogin()))
                .thenThrow(new PlaceControllerException("Have no place to delete! Нет рабочего места/зала для удаления!"));

        placesManageServlet.doDelete(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Have no place to delete! Нет рабочего места/зала для удаления!\"}");

        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(2)).getAttribute(anyString());
        verify(req, times(1)).getReader();
        verify(placeController,times(1)).deletePlace(any(PlaceCreateDeleteDto.class), anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).readValue(any(BufferedReader.class), eq(PlaceCreateDeleteDto.class));
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Тестируем метод doGet - с функционалом получения Place-a по выбранным параметрам */

    @Test
    void shouldReturnPlaceByIdParam_doGetTest() throws IOException, PlaceControllerException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        when(req.getParameter("placeId")).thenReturn(placeId);
        when(req.getParameter("species")).thenReturn(null);
        when(req.getParameter("placeNumber")).thenReturn(null);
        when(placeController.readPlaceById(Long.parseLong(placeId))).thenReturn(placeReadUpdateDto);

        placesManageServlet.doGet(req,resp);

        assertThat(jsonPrintToScreenPlaceData).isEqualTo(objectWriter.writeValueAsString(placeReadUpdateDto));

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(placeController, times(1)).readPlaceById(anyLong());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(PlaceReadUpdateDto.class));
    }

    @Test
    void shouldReturnPlaceBySpeciesAndNumber_doGetTest() throws IOException, PlaceControllerException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        when(req.getParameter("placeId")).thenReturn(null);
        when(req.getParameter("species")).thenReturn(placeSpecies);
        when(req.getParameter("placeNumber")).thenReturn(placeNumber);
        when(placeController.readPlaceBySpeciesAndNumber(Species.valueOf(placeSpecies.toUpperCase()),
                                                         Integer.parseInt(placeNumber))).thenReturn(placeReadUpdateDto);

        placesManageServlet.doGet(req,resp);

        assertThat(jsonPrintToScreenPlaceData).isEqualTo(objectWriter.writeValueAsString(placeReadUpdateDto));

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(placeController, times(1)).readPlaceBySpeciesAndNumber(any(Species.class), anyInt());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(PlaceReadUpdateDto.class));
    }

    @Test
    void shouldThrowExceptionHaveNoPermission_doGetTest() throws IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthSimpleUser);

        placesManageServlet.doGet(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"You do not have permission to access this page! У вас нет доступа!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionThreeParamRequest_doGetTest() throws IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        when(req.getParameter("placeId")).thenReturn(placeId);
        when(req.getParameter("species")).thenReturn(placeSpecies);
        when(req.getParameter("placeNumber")).thenReturn(placeNumber);

        placesManageServlet.doGet(req,resp);

        assertThat(jsonResponse)
                .isEqualTo("{\"message\":\"Invalid combination of parameters (need only placeId or placeSpecies and placeNumber pair)! " +
                                                  "Неверное сочетание параметров (достаточно ID места или его вид и номер)!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionNotValidPlaceId_doGetTest() throws IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        when(req.getParameter("placeId")).thenReturn(notValidPlaceId);
        when(req.getParameter("species")).thenReturn(null);
        when(req.getParameter("placeNumber")).thenReturn(null);

        placesManageServlet.doGet(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"ID parameter must be positive! ID не может быть отрицательным!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionNotValidPlaceNumber_doGetTest() throws IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        when(req.getParameter("placeId")).thenReturn(null);
        when(req.getParameter("species")).thenReturn(placeSpecies);
        when(req.getParameter("placeNumber")).thenReturn(notValidPlaceNumber);

        placesManageServlet.doGet(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Invalid species or place number parameters! " +
                                                                  "Неверно заданы вид (ЗАЛ/РАБОЧЕЕ МЕСТО) и/или номер (МЕСТА/ЗАЛА)!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    @Test
    void shouldThrowExceptionNotValidPlaceSpecies_doGetTest() throws IOException {
        when(servletContext.getAttribute("authentication")).thenReturn(jwtAuthUserAdmin);

        when(req.getParameter("placeId")).thenReturn(null);
        when(req.getParameter("species")).thenReturn(notValidPlaceSpecies);
        when(req.getParameter("placeNumber")).thenReturn(placeNumber);

        placesManageServlet.doGet(req,resp);

        assertThat(jsonResponse).isEqualTo("{\"message\":\"Invalid species or place number parameters! " +
                                                                  "Неверно заданы вид (ЗАЛ/РАБОЧЕЕ МЕСТО) и/или номер (МЕСТА/ЗАЛА)!\"}");

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(anyString());
        verify(resp, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), any(JsonFormResponse.class));
    }

    /* Stub-ы на BufferedReader, вернее на *.getReader() */

    private void stubBufferedReaderForCreateDeletePlace() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(objectWriter.writeValueAsString(placeCreateDeleteDto).getBytes());
        Reader reqReader = new InputStreamReader(inputStream);
        reqBufferedReader = new BufferedReader(reqReader);

        when(req.getReader()).thenReturn(reqBufferedReader);
    }

    private void stubBufferedReaderForReadUpdatePlace() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(objectWriter.writeValueAsString(placeReadUpdateDto).getBytes());
        Reader reqReader = new InputStreamReader(inputStream);
        reqBufferedReader = new BufferedReader(reqReader);

        when(req.getReader()).thenReturn(reqBufferedReader);
    }
}