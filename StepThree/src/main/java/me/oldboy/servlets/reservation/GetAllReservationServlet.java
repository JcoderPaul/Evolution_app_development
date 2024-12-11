package me.oldboy.servlets.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.ReservationController;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.core.dto.reservations.ReservationReadDto;
import me.oldboy.exception.ReservationServiceException;

import java.io.IOException;
import java.util.List;

@WebServlet(value = "/cw_api/v1/reservations/available", name = "AllReservation")
public class GetAllReservationServlet extends HttpServlet {

    private ReservationController reservationController;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        reservationController = (ReservationController) getServletContext().getAttribute("reservationController");
        objectMapper = (ObjectMapper) getServletContext().getAttribute("objectMapper");
    }

    /**
     * Handles GET requests to retrieve currently available reservations
     * Обработка запроса на получение всех доступных броней
     *
     * @param req  the HTTP servlet request / запрос
     * @param resp the HTTP servlet response / ответ
     * @throws IOException if an I/O error occurs during request handling / возможные ошибки в ходе обработки запроса
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<ReservationReadDto> allReservation = reservationController.readAllReservation();
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), allReservation);
        } catch (ReservationServiceException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }
}