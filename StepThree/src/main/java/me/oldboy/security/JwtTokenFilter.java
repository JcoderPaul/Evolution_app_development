package me.oldboy.security;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import me.oldboy.core.dto.users.UserReadDto;
import me.oldboy.exception.TokenFilterException;
import me.oldboy.core.model.service.UserService;

import java.io.IOException;
import java.util.Optional;

/**
 * Filter intercepts all incoming HTTP requests and checks for the presence of a JWT in the Authorization header.
 * Фильтр проверяет все входящие HTTP запросы на наличие JWT токена в заголовке Authorization
 *
 * If a valid JWT found, it authenticates the user and stores the JwtAuthUser in the servlet context.
 * Если найден действующий JWT, он аутентифицирует пользователя и сохраняет эти сведения в сервлет-контексте приложения.
 *
 * If no JWT is found / or is invalid, it stores an unauthenticated JwtAuthUser object in the servlet context.
 * Если JWT не найден / или он недействителен, то в сервлет-контексте сохраняет не (пустой) аутентифицированный объект.
 */
@WebFilter(servletNames = {"AllAvailableSlots", "AllAvailablePlaces", "AllAudits", "AllReservation",
                           "ReservationManageServlet", "PlaceManageServlet", "SlotManageServlet", "UserManageServlet",
                           "FreeSlotByDateServlet"},
                           initParams = @WebInitParam(name = "order", value = "1"))
public class JwtTokenFilter implements Filter {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private JwtTokenGenerator jwtTokenGenerator;
    private ServletContext servletContext;
    private UserService userService;

    /**
     * Initializes the filter / Инициализация фильтра
     *
     * @param config the filter configuration
     */
    @Override
    public void init(FilterConfig config) {
        this.servletContext = config.getServletContext();
        jwtTokenGenerator = (JwtTokenGenerator) servletContext.getAttribute("jwtTokenGenerator");
        userService = (UserService) servletContext.getAttribute("userService");
    }

    /**
     * Checks for the presence of a JWT in the Authorization header of the incoming request.
     * Проверяет наличие JWT в заголовке Authorization входящего запроса, паттерн задан в параметрах @WebFilter
     *
     * If a valid JWT is found, it authenticates the user and stores the JwtAuthUser in the servlet context.
     * Если найден действующий/валидный JWT, аутентифицирует пользователя и сохраняет в текущем контексте приложения.
     *
     * If no JWT is found or the JWT is invalid, it stores an unauthenticated Authentication object in the servlet context.
     * Если JWT не найден / недействителен, в контексте приложения сохраняется "пустой" объект JwtUserContext.
     *
     * @param servletRequest the incoming request / входящий запрос
     * @param servletResponse the outgoing response / ответ приложения
     * @param filterChain the filter chain / цепочка применяемых к сервлетам фильтров
     * @throws IOException if an I/O error occurs during this filter's processing of the request
     * @throws ServletException if the processing fails for any other reason
     */
    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) {
        String bearerToken = ((HttpServletRequest) servletRequest).getHeader(HEADER_NAME);
        String token = null;
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            token = bearerToken.substring(BEARER_PREFIX.length());
        } else {
            throw new TokenFilterException("Have no JWT token! / Не передан токен JWT! ");
        }
        try {
            String login = jwtTokenGenerator.extractUserName(token);
            Optional<UserReadDto> userReadDto = userService.findByUserName(login);
            if (userReadDto.isPresent() && jwtTokenGenerator.isValid(token, userReadDto.get())) {
                JwtAuthUser authentication =
                        jwtTokenGenerator.authentication(token, userReadDto.get());
                servletContext.setAttribute("authentication", authentication);
            } else {
                servletContext.setAttribute("authentication", new JwtAuthUser(null, null, false));
            }
        } catch (Exception e) {
            throw new TokenFilterException(e.getMessage());
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}