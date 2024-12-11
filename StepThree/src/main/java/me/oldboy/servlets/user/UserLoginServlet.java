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
import me.oldboy.exception.SecurityServiceException;
import me.oldboy.exception.UserControllerException;
import me.oldboy.security.JwtAuthRequest;
import me.oldboy.security.JwtAuthResponse;
import me.oldboy.validate.ValidatorDto;

import javax.validation.*;
import java.io.IOException;

@WebServlet(value = "/cw_api/v1/user/login", name = "LoginServlet")
public class UserLoginServlet extends HttpServlet {

    private UserController userController;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        userController = (UserController) getServletContext().getAttribute("userController");
        objectMapper = (ObjectMapper) getServletContext().getAttribute("objectMapper");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            JwtAuthRequest jwtAuthRequest =
                    objectMapper.readValue(req.getInputStream(), JwtAuthRequest.class);

            /*
            Валидируем входящие данные, в текущем примере мы проверяем их сразу на входе, в отличие
            от других DTO, где валидация проходила на слое контроллеров или сервисов. Как я понимаю,
            сколько разработчиков, столько и мнений, но логика подсказывает, что все же данные должны
            проверяться на входе.
            */
            ValidatorDto.getInstance().isValidData(jwtAuthRequest);

            JwtAuthResponse jwtAuthResponse =
                    userController.loginUser(jwtAuthRequest.getLogin(), jwtAuthRequest.getPassword());

            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            objectMapper.writeValue(resp.getOutputStream(), jwtAuthResponse);
        } catch (UserControllerException | SecurityServiceException e ) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new JsonFormResponse(e.getMessage()));
        } catch (ConstraintViolationException e){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new JsonFormResponse(e.getMessage()));
        }
    }
}