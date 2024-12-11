package me.oldboy.servlets.user;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.controllers.UserController;
import me.oldboy.core.dto.users.UserCreateDto;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.exception.UserControllerException;

import javax.validation.ConstraintViolationException;
import java.io.IOException;

@WebServlet(value = "/cw_api/v1/user/register", name = "RegServlet")
public class  UserRegServlet extends HttpServlet {

    private UserController userController;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        userController = (UserController) getServletContext().getAttribute("userController");
        objectMapper = (ObjectMapper) getServletContext().getAttribute("objectMapper");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            UserCreateDto userCreateDto = objectMapper.readValue(req.getInputStream(), UserCreateDto.class);

            if (userController.registrationUser(userCreateDto)) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getOutputStream(),
                    new JsonFormResponse("Registration successful. Пользователь зарегистрирован."));
            }
        } catch (JsonParseException | UserControllerException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new JsonFormResponse(e.getMessage()));
        } catch (ConstraintViolationException e){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new JsonFormResponse(e.getMessage()));
        }
    }
}