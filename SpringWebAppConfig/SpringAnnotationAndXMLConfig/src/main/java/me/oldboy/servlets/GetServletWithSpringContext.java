package me.oldboy.servlets;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.service_imitation.UserService;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@WebServlet("/user")
public class GetServletWithSpringContext extends HttpServlet {

    ApplicationContext springContext;

    @Override
    public void init(ServletConfig config) {
        ServletContext context = config.getServletContext();
        springContext = (ApplicationContext) context.getAttribute("springContext");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("charset=UTF-8");

        String userId = req.getParameter("userId");
        UserService userService = springContext.getBean(UserService.class);
        String userName = userService.getUser(Long.parseLong(userId)).name();

        resp.getWriter().write("<H1> USER NAME: " + userName + " </H1>");
    }
}
