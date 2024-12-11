package me.oldboy.servlets.slots;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.SlotController;
import me.oldboy.core.dto.slots.SlotReadUpdateDto;
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
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllSlotsServletTest {

    @Mock
    private SlotController slotController;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private HttpServletRequest req;
    @Mock
    private HttpServletResponse resp;
    @InjectMocks
    private GetAllSlotsServlet getAllSlotsServlet;
    private PrintWriter respPrintWriter;
    private ObjectWriter objectWriter;
    private List<SlotReadUpdateDto> testSlotReadList, listFromMethod;
    private String listToJson;
    private SlotReadUpdateDto firstReadSlotDto, secondReadSlotDto;

    @BeforeEach
    public void setUp() throws IOException {
        firstReadSlotDto = new SlotReadUpdateDto(1L, 10, LocalTime.of(10,00), LocalTime.of(11,00));
        secondReadSlotDto = new SlotReadUpdateDto(2L, 11, LocalTime.of(11,00), LocalTime.of(12,00));
        testSlotReadList = List.of(firstReadSlotDto, secondReadSlotDto);

        /*
        В основном модуле AppContextBuilder.java мы настраиваем наш рабочий ObjectMapper для правильного преобразования дат.
        Но тут, в тестах, мы предварительно не проделывали ничего подобного. Если запустить тест без этого - мы хапнем исключение:

        Java 8 date/time type `java.time.LocalTime` not supported by default:
        add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable handling
        (through reference chain: java.util.ImmutableCollections$List12[0]->
        me.oldboy.core.dto.slots.SlotReadUpdateDto["timeStart"])

        Что бы избежать подобных бросков проводим настройку.
        */
        ObjectMapper objectMapperForWriter = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapperForWriter.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        objectMapperForWriter.registerModule(module);

        objectWriter = objectMapperForWriter.writer().withDefaultPrettyPrinter();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer respWriter = new OutputStreamWriter(outputStream);
        respPrintWriter = new PrintWriter(respWriter);

        when(resp.getWriter()).thenReturn(respPrintWriter);
    }

    @AfterEach
    public void closeAllStream() throws IOException {
        resp.getWriter().close();
    }

    @Test
    void shouldReturnSlotList_doGetTest() throws IOException {
        when(slotController.getAllSlots()).thenReturn(testSlotReadList);

        doAnswer(invocation -> {
            PrintWriter outputPrintWriter = invocation.getArgument(0);
            listFromMethod = invocation.getArgument(1);

            listToJson = objectWriter.writeValueAsString(listFromMethod);
            outputPrintWriter.write(listToJson);

            return null;
        }).when(objectMapper).writeValue(any(PrintWriter.class), any());

        getAllSlotsServlet.doGet(req, resp);

        assertThat(listToJson).isEqualTo(objectWriter.writeValueAsString(testSlotReadList));
        assertThat(listFromMethod).contains(firstReadSlotDto, secondReadSlotDto);

        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), anyList());
        verify(slotController, times(1)).getAllSlots();
    }
}