package me.oldboy.servlets.slots;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.ReservationController;
import me.oldboy.exception.ReservationControllerException;
import me.oldboy.exception.ReservationServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class GetFreeSlotsByDateServletTest {

    @Mock
    private ReservationController reservationController;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private HttpServletRequest req;
    @Mock
    private HttpServletResponse resp;
    @InjectMocks
    private GetFreeSlotsByDateServlet getFreeSlotsByDateServlet;
    private PrintWriter respPrintWriter;
    private ObjectWriter objectWriter;
    private String reservationDate, mapToJson;
    private Map<Long, List<Long>> testFreeSlotMap, printFreeSlotMap;

    @BeforeEach
    public void setUp() throws IOException {
        testFreeSlotMap = Map.of(1L, List.of(1L, 2L, 3L, 4L), 2L, List.of(1L, 3L), 3L, List.of(3L, 4L));
        reservationDate = "2027-01-02";

        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer respWriter = new OutputStreamWriter(outputStream);
        respPrintWriter = new PrintWriter(respWriter);

        when(resp.getWriter()).thenReturn(respPrintWriter);

        doAnswer(invocation -> {
            PrintWriter outputPrintWriter = invocation.getArgument(0);
            printFreeSlotMap = invocation.getArgument(1);

            mapToJson = objectWriter.writeValueAsString(printFreeSlotMap);
            outputPrintWriter.write(mapToJson);

            return null;
        }).when(objectMapper).writeValue(any(PrintWriter.class), anyMap());
    }

    @AfterEach
    public void closeAllStream() throws IOException {
        resp.getWriter().close();
    }

    @Test
    void shouldReturnFreeSlotMap_doGetTest() throws IOException, ReservationControllerException, ReservationServiceException {
        when(req.getParameter("date")).thenReturn(reservationDate);
        when(reservationController.getFreeSlotsByDate(reservationDate)).thenReturn(testFreeSlotMap);

        getFreeSlotsByDateServlet.doGet(req, resp);

        assertThat(mapToJson).isEqualTo(objectWriter.writeValueAsString(testFreeSlotMap));
        assertThat(printFreeSlotMap).containsKeys(1L, 2L, 3L);

        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), anyMap());
        verify(reservationController, times(1)).getFreeSlotsByDate(anyString());
    }
}