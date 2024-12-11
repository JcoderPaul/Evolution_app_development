package me.oldboy.servlets.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.oldboy.core.dto.JsonFormResponse;
import me.oldboy.core.model.database.audit.Audit;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.core.model.service.AuditService;
import me.oldboy.security.JwtAuthUser;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;

@WebServlet(value = "/cw_api/v1/audit", name = "AllAudits")
public class GetAllAuditsServlet extends HttpServlet {

    private AuditService auditService;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        auditService = (AuditService) getServletContext().getAttribute("auditService");
        objectMapper = (ObjectMapper) getServletContext().getAttribute("objectMapper");
    }

    /**
     * Handles GET requests to retrieve currently available places
     * Обработка запроса на получение всех доступных рабочих мест и залов
     *
     * @param req  the HTTP servlet request / запрос
     * @param resp the HTTP servlet response / ответ
     * @throws IOException if an I/O error occurs during request handling / возможные ошибки в ходе обработки запроса
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            isAdmin(req);

            List<Audit> allAudit = auditService.getAllAudit();
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), allAudit);
        } catch (AccessDeniedException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new JsonFormResponse(e.getMessage()));
        }
    }

    /**
     * Check admin permission of request.
     *
     * @param req the HTTP servlet request containing data with or no permission
     * @throws AccessDeniedException if user or service have no permission to request data
     */
    private boolean isAdmin(HttpServletRequest req) throws AccessDeniedException {
        JwtAuthUser jwtAuthUser= (JwtAuthUser) getServletContext().getAttribute("authentication");
        if (jwtAuthUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You do not have permission to access this page! У вас нет доступа!");
        }
        return true;
    }
}