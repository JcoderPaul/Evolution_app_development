package me.oldboy.servlets.places;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.PlaceController;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.core.dto.places.PlaceReadUpdateDto;

import java.io.IOException;
import java.util.List;

@WebServlet(value = "/cw_api/v1/places/available", name = "AllAvailablePlaces")
public class GetAllPlacesServlet extends HttpServlet {

    private PlaceController placeController;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        placeController = (PlaceController) getServletContext().getAttribute("placeController");
        objectMapper = (ObjectMapper) getServletContext().getAttribute("objectMapper");
    }

    /**
     * Handles GET requests to retrieve currently available places
     * Обработка запроса на получение всех доступных рабочих мест и залов
     *
     * @param req  the HTTP servlet request / запрос
     * @param resp the HTTP servlet response / ответ
     * @throws IOException if an I/O error occurs during request handling / возможные ошибки в ходе обработки запроса
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<PlaceReadUpdateDto> allPlaces = placeController.getAllPlaces();
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), allPlaces);
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }
}