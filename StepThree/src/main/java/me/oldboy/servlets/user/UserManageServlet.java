package me.oldboy.servlets.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.UserController;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.core.dto.users.UserReadDto;
import me.oldboy.core.dto.users.UserUpdateDeleteDto;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.exception.UserControllerException;
import me.oldboy.exception.UserServiceException;
import me.oldboy.security.JwtAuthUser;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;

@WebServlet(value = "/cw_api/v1/users", name = "UserManageServlet")
public class UserManageServlet extends HttpServlet {
    private UserController userController;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        userController = (UserController) getServletContext().getAttribute("userController");
        objectMapper = (ObjectMapper) getServletContext().getAttribute("objectMapper");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try{
            /* Во всех сервлетах, где идет процесс управления записями в БД (CRUD процесс) мы делаем одну проверку - админ ли "балуется", если да дозволяем */
            isAdmin(req);

            /* А так же, поскольку, мы ведем аудит команд вносящих изменения в БД, нам нужно знать имя админа который сотворил "безобразие" в БД */
            String userName = getAuthUserName(req);

            UserUpdateDeleteDto updateUser = objectMapper.readValue(req.getReader(), UserUpdateDeleteDto.class);

            /* Имя админа который вносит изменения в БД (обновляет, удаляет, возможно создает новую запись), мы должны зафиксировать функционалом аудита */
            if(userController.updateUser(updateUser, userName)){
                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), new JsonFormResponse("Update success! Данные обновлены!"));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), new JsonFormResponse("Have no user for update! В базе нет данных для обновления!"));
            }
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (UserControllerException | UserServiceException | ConstraintViolationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            isAdmin(req);
            String userName = getAuthUserName(req);

            UserUpdateDeleteDto updateUser = objectMapper.readValue(req.getReader(), UserUpdateDeleteDto.class);

            if(userController.deleteUser(updateUser, userName)){
                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), new JsonFormResponse("Remove success! Запись удалена!"));
            }
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (UserControllerException | UserServiceException | ConstraintViolationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            isAdmin(req);

            List<UserReadDto> toScreen = userController.getAllUser();

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), toScreen);
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }

    /* Вспомогательные методы */

    private boolean isAdmin(HttpServletRequest req) throws AccessDeniedException {
        JwtAuthUser jwtAuthUser= (JwtAuthUser) getServletContext().getAttribute("authentication");
        if (jwtAuthUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You do not have permission to access this page! " +
                                            "У вас нет доступа!");
        }
        return true;
    }

    private String getAuthUserName(HttpServletRequest req) {
        JwtAuthUser jwtAuthUser= (JwtAuthUser) getServletContext().getAttribute("authentication");
        return jwtAuthUser.getLogin();
    }
}