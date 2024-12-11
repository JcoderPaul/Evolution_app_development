package me.oldboy.servlets.places;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.PlaceController;
import me.oldboy.core.dto.places.PlaceCreateDeleteDto;
import me.oldboy.core.dto.places.PlaceReadUpdateDto;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.core.model.database.entity.options.Species;
import me.oldboy.exception.NotValidArgumentException;
import me.oldboy.exception.PlaceControllerException;
import me.oldboy.exception.PlaceServiceException;
import me.oldboy.security.JwtAuthUser;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;

/**
 * Servlet for managing places, access only for ADMIN / Сервлет для CRUD операций с рабочими местами/залами
 */
@WebServlet(value = "/cw_api/v1/places/", name = "PlaceManageServlet")
public class PlacesManageServlet extends HttpServlet {

    private PlaceController placeController;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        placeController = (PlaceController) getServletContext().getAttribute("placeController");
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
            /* 1 - Проверяем имеет ли пользователь (сервис) соответствующий (ADMIN) приоритет для действий */
            isAdmin(req);
            String creatorName = getAdminName(req);

            /* 2 - Получаем из запроса "расшифровку" соответствующего DTO */
            PlaceCreateDeleteDto placeCreate = objectMapper.readValue(req.getReader(), PlaceCreateDeleteDto.class);

            /* 3 - Отправляем полученный DTO на слой контроллеров */
            PlaceReadUpdateDto createdPlace = placeController.createNewPlace(placeCreate, creatorName);

            /* 4 - Если нет бросков исключений - возвращаем соответствующий HTTP статус */
            resp.setStatus(HttpServletResponse.SC_CREATED);

            /* 5 - Отдаем информацию в ответ на запрос */
            objectMapper.writeValue(resp.getWriter(), createdPlace);

            /* 6 - Если что-то пошло не так бросаем исключение и соответствующий HTTP статус */
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (PlaceControllerException | ConstraintViolationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
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
        try {
            isAdmin(req);
            String updaterName = getAdminName(req);

            PlaceReadUpdateDto updatedPlace = objectMapper.readValue(req.getReader(), PlaceReadUpdateDto.class);
            if (placeController.updatePlace(updatedPlace, updaterName)){
                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), new JsonFormResponse("The update was successful! Обновление прошло успешно!"));
            }
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (PlaceServiceException | ConstraintViolationException | PlaceControllerException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
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
        try {
            isAdmin(req);
            String deleterName = getAdminName(req);

            PlaceCreateDeleteDto deletePlace = objectMapper.readValue(req.getReader(), PlaceCreateDeleteDto.class);
            if (placeController.deletePlace(deletePlace, deleterName)){
                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(),
                        new JsonFormResponse("Deletion was successful! Удаление прошло успешно!"));
            }
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (PlaceControllerException | PlaceServiceException e) {
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

            String placeId = req.getParameter("placeId");
            String species = req.getParameter("species");
            String placeNumber = req.getParameter("placeNumber");

            PlaceReadUpdateDto getPlace = getPlaceByParam(placeId, species, placeNumber);
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), getPlace);
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (PlaceControllerException | NotValidArgumentException e) {
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

    private String getAdminName(HttpServletRequest req){
        JwtAuthUser jwtAuthUser= (JwtAuthUser) getServletContext().getAttribute("authentication");
        return jwtAuthUser.getLogin();
    }

    /**
     * Check request param and get places data.
     *
     * @param placeId the place ID
     * @param placeSpecies the place species (HALL or WORKPLACE)
     * @param placeNumber the place number
     *
     * @return PlaceReadUpdateDto found place
     *
     * @throws NotValidArgumentException if the data has incorrect values
     * @throws PlaceControllerException if there are no records in the database for such parameters
     */
    private PlaceReadUpdateDto getPlaceByParam(String placeId,
                                               String placeSpecies,
                                               String placeNumber) throws NotValidArgumentException,
                                                                          PlaceControllerException {
        PlaceReadUpdateDto placeReadUpdateDto = null;
        try {
            if ((placeId != null && placeSpecies == null && placeNumber == null)) {
                if (Long.parseLong(placeId) >= 0) {
                        placeReadUpdateDto = placeController.readPlaceById(Long.parseLong(placeId));
                } else {
                        throw new NotValidArgumentException("ID parameter must be positive! " +
                                                            "ID не может быть отрицательным!");
                }
            } else if ((placeId == null && placeSpecies != null && placeNumber != null)) {
                Integer placeNumberToFind = Integer.parseInt(placeNumber);
                if (placeSpecies.toUpperCase().matches("HALL|WORKPLACE") && placeNumberToFind >= 0) {
                    Species placeSpeciesToFind = Species.valueOf(placeSpecies.toUpperCase());
                    placeReadUpdateDto = placeController.readPlaceBySpeciesAndNumber(placeSpeciesToFind, placeNumberToFind);
                } else {
                        throw new NotValidArgumentException("Invalid species or place number parameters! " +
                                                            "Неверно заданы вид (ЗАЛ/РАБОЧЕЕ МЕСТО) и/или " +
                                                            "номер (МЕСТА/ЗАЛА)!");
                }
            } else {
                throw new NotValidArgumentException("Invalid combination of parameters (need only placeId or placeSpecies and placeNumber pair)! " +
                                                    "Неверное сочетание параметров (достаточно ID места или его вид и номер)!");
            }
        } catch (Exception e) {
            throw new NotValidArgumentException("Invalid parse parameters! " + e.getMessage());
        }
        return placeReadUpdateDto;
    }
}