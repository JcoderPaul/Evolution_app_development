package me.oldboy.servlets.slots;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.SlotController;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.core.dto.slots.SlotReadUpdateDto;

import java.io.IOException;
import java.util.List;

/**
 * Servlet for view all available slots, access for authenticate user
 *
 * Сервлет для просмотра всех доступных временных диапазонов,
 * доступен для зарегистрированных пользователей
 */
@WebServlet(value = "/cw_api/v1/slots/available", name = "AllAvailableSlots")
public class GetAllSlotsServlet extends HttpServlet {

    private SlotController slotController;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        slotController = (SlotController) getServletContext().getAttribute("slotController");
        objectMapper = (ObjectMapper) getServletContext().getAttribute("objectMapper");
    }

    /**
     * Handles GET requests to retrieve currently available time slots
     * Обработка запроса на получение всех доступных слотов времени для резервирования
     *
     * @param req the HTTP servlet request / запрос
     * @param resp the HTTP servlet response / ответ
     * @throws IOException if an I/O error occurs during request handling / возможные ошибки в ходе обработки запроса
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            /* Запрашиваем соответствующий метод на слое контроллеров, для получения списка всех слотов */
            List<SlotReadUpdateDto> allSlots = slotController.getAllSlots();

            /* Возвращаем HTTP статус 200 */
            resp.setStatus(HttpServletResponse.SC_OK);

            /* Возвращаем список слотов в response */
            objectMapper.writeValue(resp.getWriter(), allSlots);
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }
}