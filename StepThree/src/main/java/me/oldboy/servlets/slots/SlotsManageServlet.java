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
import me.oldboy.core.dto.slots.SlotCreateDeleteDto;
import me.oldboy.core.dto.slots.SlotReadUpdateDto;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.exception.NotValidArgumentException;
import me.oldboy.exception.SlotControllerException;
import me.oldboy.exception.SlotServiceException;
import me.oldboy.security.JwtAuthUser;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;

/**
 * Servlet for managing time slots, access only for ADMIN / Сервлет для CRUD операций с временем бронирования
 */
@WebServlet(value = "/cw_api/v1/slots/", name = "SlotManageServlet")
public class SlotsManageServlet extends HttpServlet {

    private SlotController slotController;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        slotController = (SlotController) getServletContext().getAttribute("slotController");
        objectMapper = (ObjectMapper) getServletContext().getAttribute("objectMapper");
    }

    /**
     * Handles POST requests to create a new slot.
     *
     * @param req the HTTP servlet request containing slot data in JSON format for create
     * @param resp the HTTP servlet response
     * @throws IOException if an I/O error occurs during request handling
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        /* Методы структурно походят один на другой, с небольшими отличиями см. ниже */
        try {
            /* 1 - Проверяем имеет ли пользователь (сервис) соответствующий (ADMIN) приоритет для действий */
            isAdmin(req);
            String userName = getAuthUserName(req);

            /* 2 - Получаем из запроса "расшифровку" соответствующего DTO (Read, Delete, Create, Update) */
            SlotCreateDeleteDto slotForCreate = objectMapper.readValue(req.getReader(), SlotCreateDeleteDto.class);

            /* 3 - Отправляем полученный DTO на слой контроллеров */
            SlotReadUpdateDto slotCreated = slotController.createNewSlot(slotForCreate, userName);

            /* 4 - Если нет бросков исключений - возвращаем соответствующий HTTP статус */
            resp.setStatus(HttpServletResponse.SC_CREATED);

            /* 5 - Отдаем информацию в ответ на запрос */
            objectMapper.writeValue(resp.getWriter(), slotCreated);

            /* 6 - Если что-то пошло не так бросаем исключение и соответствующий HTTP статус */
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (ConstraintViolationException | SlotServiceException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }

    /**
     * Handles PUT requests to update slot data.
     *
     * @param req the HTTP servlet request containing slot data in JSON format for update
     * @param resp the HTTP servlet response
     * @throws IOException if an I/O error occurs during request handling
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            isAdmin(req);
            String userName = getAuthUserName(req);

            SlotReadUpdateDto updatedSlot = objectMapper.readValue(req.getReader(), SlotReadUpdateDto.class);
            if (slotController.updateSlot(updatedSlot, userName)){
                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), new JsonFormResponse("The update was successful! Обновление прошло успешно!"));
            }
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (SlotServiceException | ConstraintViolationException | SlotControllerException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }

    /**
     * Handles DELETE requests to remove slot from database.
     *
     * @param req the HTTP servlet request containing slot param in JSON format for remove
     * @param resp the HTTP servlet response
     * @throws IOException if an I/O error occurs during request handling
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            isAdmin(req);
            String userName = getAuthUserName(req);

            SlotCreateDeleteDto deleteSlot = objectMapper.readValue(req.getReader(), SlotCreateDeleteDto.class);
            if (slotController.deleteSlot(deleteSlot, userName)){
                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), new JsonFormResponse("Deletion was successful! Удаление прошло успешно!"));
            }
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (SlotControllerException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }

    /*
    В отличие от предыдущих типов запросов, тут мы будем передавать параметры не в теле запроса,
    а через саму строку запроса, например:
    - /cw_api/v1/places/?placeId=1 - получить сведения по ID рабочего места/зала;
    - /cw_api/v1/places/?species=HALL&placeNumber=3 - получить сведения по виду (рабочее место -
                                                      WORKPLACE или зал - HALL) и его номеру;
    - /cw_api/v1/places/?placeId=1&species=HALL&placeNumber=3 - вернет ошибку, поскольку "не понятно"
                                                                какое сочетание параметров использовать;
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
        try {
            isAdmin(req);

            String slotId = req.getParameter("slotId");
            String slotNumber = req.getParameter("slotNumber");

            SlotReadUpdateDto getSlot = getSlotByParam(slotId, slotNumber);
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), getSlot);
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (SlotControllerException | NotValidArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }

    /* Проверочные методы */

    /**
     * Check admin permission of request.
     *
     * @param req the HTTP servlet request containing data with or no permission
     * @throws AccessDeniedException if user or service have no permission to request data
     */
    private void isAdmin(HttpServletRequest req) throws AccessDeniedException {
        JwtAuthUser jwtAuthUser= (JwtAuthUser) getServletContext().getAttribute("authentication");
        if (jwtAuthUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You do not have permission to access this page! " +
                                            "У вас нет доступа!");
        }
    }

    private String getAuthUserName(HttpServletRequest req) {
        JwtAuthUser jwtAuthUser= (JwtAuthUser) getServletContext().getAttribute("authentication");
        return jwtAuthUser.getLogin();
    }

    /**
     * Check request param and get places data.
     *
     * @param slotId the slot ID
     * @param slotNumber the slot number
     *
     * @return found slot
     *
     * @throws NotValidArgumentException if the data has incorrect values
     */
    private SlotReadUpdateDto getSlotByParam(String slotId,
                                             String slotNumber) throws NotValidArgumentException,
                                                                       SlotControllerException {
        SlotReadUpdateDto slotToFind = null;
        try {
            if ((slotId != null && slotNumber == null)) {
                Long slotIdParse = Long.parseLong(slotId);
                if (slotIdParse >= 0) {
                    slotToFind = slotController.readSlotById(slotIdParse);
                } else {
                        throw new NotValidArgumentException("ID parameter must be positive! " +
                                                            "ID не может быть отрицательным!");
                }
            } else if ((slotId == null && slotNumber != null)) {
                Integer slotNumberParse = Integer.parseInt(slotNumber);
                if (slotNumberParse >= 0) {
                    slotToFind = slotController.readSlotByNumber(slotNumberParse);
                } else {
                        throw new NotValidArgumentException("Slot number must be positive! " +
                                                            "Номер слота не может быть отрицательным!");
                }
            } else {
                throw new NotValidArgumentException("Invalid combination of parameters (need only slotId or slotNumber)! " +
                                                    "Неверное сочетание параметров (достаточно ID слота или его номера)!");
            }
        } catch (NotValidArgumentException e) {
            throw new NotValidArgumentException("Invalid parse parameters! " + e.getMessage());
        }
        return slotToFind;
    }
}