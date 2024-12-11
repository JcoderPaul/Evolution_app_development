package me.oldboy.servlets.slots;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.ReservationController;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.exception.ReservationControllerException;
import me.oldboy.exception.ReservationServiceException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

/**
 * Servlet for managing time slots, access only for ADMIN
 * Сервлет для CRUD операций с диапазонами времени бронирования
 */
@WebServlet(value = "/cw_api/v1/slots/free-by-date", name = "FreeSlotByDateServlet")
public class GetFreeSlotsByDateServlet extends HttpServlet {

    private ReservationController reservationController;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        reservationController = (ReservationController) getServletContext().getAttribute("reservationController");
        objectMapper = (ObjectMapper) getServletContext().getAttribute("objectMapper");
    }

    /*
    Тут мы передаем параметры не в теле запроса, а через саму строку запроса, например:
    - http://..адрес сервера../cw_api/v1/slots/free-by-date?date=2027-01-02 - получить данные о доступных слотах на дату;
    */

    /**
     * Handles GET requests to read places.
     *
     * @param req the HTTP servlet request containing place data for read
     * @param resp the HTTP servlet response
     * @throws IOException if an I/O error occurs during request handling
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        /* Доступ к просмотру данных есть только у аутентифицированных пользователей */
        try {
            /* Получаем параметр через строку HTTP запроса */
            String reservationDate = req.getParameter("date");

            /* Решили вернуть для отображения пользователю только краткую информацию placeId и его список свободных слотов */
            Map<Long, List<Long>> getFreeSlotByDate =
                    reservationController.getFreeSlotsByDate(reservationDate);

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), getFreeSlotByDate);
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (ReservationControllerException | ReservationServiceException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }
}