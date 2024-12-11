package me.oldboy.servlets.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.ReservationController;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.core.dto.reservations.ReservationCreateDto;
import me.oldboy.core.dto.reservations.ReservationReadDto;
import me.oldboy.core.dto.reservations.ReservationUpdateDeleteDto;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.exception.NotValidArgumentException;
import me.oldboy.exception.ReservationControllerException;
import me.oldboy.security.JwtAuthUser;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;

/**
 * Servlet for reservation managing, partial access for owner and full access for ADMIN
 * Сервлет для CRUD операций с бронями доступен для создателя брони и администратора
 */
@WebServlet(value = "/cw_api/v1/reservations/", name = "ReservationManageServlet")
public class ReservationManageServlet extends HttpServlet {

    private ReservationController reservationController;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        reservationController = (ReservationController) getServletContext().getAttribute("reservationController");
        objectMapper = (ObjectMapper) getServletContext().getAttribute("objectMapper");
    }

    /**
     * Handles POST requests to create a new place.
     *
     * @param req the HTTP servlet request containing place data in JSON format for create
     * @param resp the HTTP servlet response
     * @throws IOException if an I/O error occurs during request handling
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        /* Методы структурно походят один на другой, с небольшими отличиями см. ниже */
        try {
            /*
            1 - В отличие от сервлетов управляющих жизненным циклом Slot-ов и Place-ов при создании брони статус
                пользователя не важен, это может быть USER и ADMIN. Но нам важно, чтобы бронь была зафиксирована
                на аутентифицированного пользователя, именно он или администратор смогут ее удалить в случае
                необходимости.
            */
            String forWhomReservation = getAuthUserName(req);

            /* 2 - Получаем из запроса "расшифровку" соответствующего DTO */
            ReservationCreateDto createReservation = objectMapper.readValue(req.getReader(), ReservationCreateDto.class);

            /* 3 - Отправляем полученный DTO на слой контроллеров */
            ReservationReadDto isReserved = reservationController.createReservation(forWhomReservation, createReservation);

            /* 4 - Если нет бросков исключений - возвращаем соответствующий HTTP статус */
            resp.setStatus(HttpServletResponse.SC_CREATED);

            /* 5 - Отдаем информацию в ответ на запрос */
            objectMapper.writeValue(resp.getWriter(), isReserved);

            /* 6 - Если что-то пошло не так бросаем исключение и соответствующий HTTP статус */
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (ReservationControllerException | ConstraintViolationException | InvalidFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse("Invalid entered data: " + e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }

    /**
     * Handles PUT requests to update places.
     *
     * @param req the HTTP servlet request containing place data in JSON format for update
     * @param resp the HTTP servlet response
     * @throws IOException if an I/O error occurs during request handling
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        /* Обновить сведения о брони могут только ее создатель и ADMIN */
        try {
            /* 1 - Получаем имя пользователя и статус (USER или ADMIN) */
            String ownerName = getAuthUserName(req);
            Boolean isAdmin = isAdmin(req);

            /* 2 - Получаем из запроса "расшифровку" соответствующего DTO */
            ReservationUpdateDeleteDto updateReservation =
                    objectMapper.readValue(req.getReader(), ReservationUpdateDeleteDto.class);

            /* 3 - Отправляем полученный DTO на слой контроллеров все проверки проводим на нем или на слое сервисов */
            Boolean isUpdated = reservationController.updateReservation(ownerName, isAdmin, updateReservation);

            /* 4 - Возвращаем ответ пользователю или сервису */
            if (isUpdated){
                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), new JsonFormResponse("The update was successful! " +
                                                                               "Обновление прошло успешно!"));
            }
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (ReservationControllerException | ConstraintViolationException | InvalidFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse("Invalid entered data: " + e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }

    /**
     * Handles DELETE requests to remove place from database.
     *
     * @param req the HTTP servlet request containing place data in JSON format for remove
     * @param resp the HTTP servlet response
     * @throws IOException if an I/O error occurs during request handling
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        /* Удалить бронь может только ее создатель или ADMIN */
        try {
            /* 1 - Получаем имя пользователя и статус (USER или ADMIN) */
            String ownerName = getAuthUserName(req);
            Boolean isAdmin = isAdmin(req);

            /* 2 - Получаем из запроса "расшифровку" соответствующего DTO */
            ReservationUpdateDeleteDto deleteReservation =
                    objectMapper.readValue(req.getReader(), ReservationUpdateDeleteDto.class);

            /* 3 - Отправляем полученный DTO на слой контроллеров все проверки проводим на нем или на слое сервисов */
            Boolean isDeleted = reservationController.deleteReservation(ownerName, isAdmin, deleteReservation);

            if (isDeleted){
                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(),
                        new JsonFormResponse("Deletion was successful! Удаление прошло успешно!"));
            }
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (ReservationControllerException | ConstraintViolationException | InvalidFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse("Invalid entered data: " + e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }

    /*
    В отличие от предыдущих типов запросов, тут мы будем передавать параметры не в теле запроса,
    а через саму строку запроса, например:
    - http:// ... адрес сервера ... /cw_api/v1/reservations/?reservationDate=2029-07-28 - получить все бронирования на дату;
    - http:// ... адрес сервера ... /cw_api/v1/reservations/?userId=3 - получить все бронирования относящиеся к конкретному пользователю;
    - http:// ... адрес сервера ... /cw_api/v1/reservations/?placeId=3 - получить все бронирования по конкретному месту;

    Фактически мы можем принять сразу все три параметра и далее на слое контроллеров или сервисов, грубо запросить все
    доступные бронирования, и уже затем исходя из сочетания параметров используя, скажем Stream API проводить нужную
    выборку и возвращать результат. Но с нас такового не требовали или требования были изложены столь туманно, что мы
    пошли простым путем - фильтрация по одному параметру, а сами запросы сделаны на слое репозиториев через Criteria API.
    */

    /**
     * Handles GET requests to read reservations.
     *
     * @param req the HTTP servlet request containing place data for read
     * @param resp the HTTP servlet response
     * @throws IOException if an I/O error occurs during request handling
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if(getAuthUserName(req) == null) {
                throw new AccessDeniedException ("Register and log in! Зарегистрируйтесь и войдите в систему!");
            }

            String reservationDate = req.getParameter("reservationDate");
            String userId = req.getParameter("userId");
            String placeId = req.getParameter("placeId");

            List<ReservationReadDto> reservationsByParam =
                    reservationController.getReservationByParam(reservationDate, userId, placeId);

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), reservationsByParam);
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (NotValidArgumentException e) {
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
    private boolean isAdmin(HttpServletRequest req) throws AccessDeniedException {
        boolean isAdmin = true;
        JwtAuthUser jwtAuthUser= (JwtAuthUser) getServletContext().getAttribute("authentication");
        if (jwtAuthUser.getRole() != Role.ADMIN) {
            isAdmin = false;
        }
        return isAdmin;
    }

    /**
     * Get owner name from auth param request.
     *
     * @param req the HTTP servlet request containing data with or no permission
     */
    private String getAuthUserName(HttpServletRequest req) {
        JwtAuthUser jwtAuthUser= (JwtAuthUser) getServletContext().getAttribute("authentication");
        return jwtAuthUser.getLogin();
    }
}