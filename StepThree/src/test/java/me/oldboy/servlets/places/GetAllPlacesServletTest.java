package me.oldboy.servlets.places;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.PlaceController;
import me.oldboy.core.dto.places.PlaceReadUpdateDto;
import me.oldboy.core.model.database.entity.options.Species;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class GetAllPlacesServletTest {

    @Mock
    private PlaceController placeController;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private HttpServletRequest req;
    @Mock
    private HttpServletResponse resp;
    @InjectMocks
    private GetAllPlacesServlet getAllPlacesServlet;

    private PrintWriter respPrintWriter;
    private ObjectWriter objectWriter;
    private List<PlaceReadUpdateDto> testPlaceReadList, listFromMethod;
    private String listToJson;
    private PlaceReadUpdateDto firstPlaceReadDto, secondPlaceReadDto;

    @BeforeEach
    public void setUp() throws IOException {
        firstPlaceReadDto = new PlaceReadUpdateDto(1L, Species.WORKPLACE, 3);
        secondPlaceReadDto = new PlaceReadUpdateDto(2L, Species.HALL, 2);
        testPlaceReadList = List.of(firstPlaceReadDto, secondPlaceReadDto);

        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

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
    void shouldReturnPlaceList_doGetTest() throws IOException {
        when(placeController.getAllPlaces()).thenReturn(testPlaceReadList);

        doAnswer(invocation -> {
            PrintWriter outputPrintWriter = invocation.getArgument(0);
            listFromMethod = invocation.getArgument(1);

            listToJson = objectWriter.writeValueAsString(listFromMethod);
            outputPrintWriter.write(listToJson);

            return null;
        }).when(objectMapper).writeValue(any(PrintWriter.class), anyList());

        getAllPlacesServlet.doGet(req, resp);

        assertThat(listToJson).isEqualTo(objectWriter.writeValueAsString(testPlaceReadList));
        assertThat(listFromMethod).contains(firstPlaceReadDto, secondPlaceReadDto);

        verify(resp, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(resp, times(1)).getWriter();
        verify(objectMapper, times(1)).writeValue(any(PrintWriter.class), anyList());
        verify(placeController, times(1)).getAllPlaces();
    }
}