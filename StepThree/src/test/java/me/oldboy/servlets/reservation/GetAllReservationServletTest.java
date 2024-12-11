package me.oldboy.servlets.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.ReservationController;
import me.oldboy.core.dto.reservations.ReservationReadDto;
import me.oldboy.exception.ReservationServiceException;
import org.junit.jupiter.api.AfterEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class GetAllReservationServletTest {

    @Mock
    private ReservationController reservationController;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private HttpServletRequest req;
    @Mock
    private HttpServletResponse resp;
    @InjectMocks
    private GetAllReservationServlet getAllReservationServlet;
    private PrintWriter respPrintWriter;
    private ObjectWriter objectWriter;
    private List<ReservationReadDto> testReservationReadList, reservationListFromMethod;
    private String listToJson;
    private ReservationReadDto firstReservation, secondReservation;

    @BeforeEach
    public void setUp() throws IOException {
        firstReservation = new ReservationReadDto(1L, LocalDate.of(2034,11,11), 1L, 3L, 5L);
        secondReservation = new ReservationReadDto(2L, LocalDate.of(2035,11,11), 2L, 4L, 1L);
        testReservationReadList = List.of(firstReservation, secondReservation);

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
    void shouldReturnReservationDtoList_doGetTest() throws ReservationServiceException, IOException {
        when(reservationController.readAllReservation()).thenReturn(testReservationReadList);

        doAnswer(invocation -> {
            PrintWriter outputPrintWriter = invocation.getArgument(0);
            reservationListFromMethod = invocation.getArgument(1);

            listToJson = objectWriter.writeValueAsString(reservationListFromMethod);
            outputPrintWriter.write(listToJson);

            return null;
        }).when(objectMapper).writeValue(any(PrintWriter.class), any());

        getAllReservationServlet.doGet(req, resp);

        assertThat(listToJson).isEqualTo(objectWriter.writeValueAsString(testReservationReadList));
        assertThat(reservationListFromMethod).contains(firstReservation, secondReservation);

        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), anyList());
        verify(reservationController, times(1)).readAllReservation();
    }
}