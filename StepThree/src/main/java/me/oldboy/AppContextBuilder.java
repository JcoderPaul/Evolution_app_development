package me.oldboy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import me.oldboy.aspects.AuditingAspect;
import me.oldboy.config.connection.ConnectionManager;
import me.oldboy.config.liquibase.LiquibaseManager;
import me.oldboy.config.util.HibernateUtil;
import me.oldboy.config.util.PropertiesUtil;
import me.oldboy.core.controllers.PlaceController;
import me.oldboy.core.controllers.ReservationController;
import me.oldboy.core.controllers.SlotController;
import me.oldboy.core.controllers.UserController;
import me.oldboy.core.model.database.repository.*;
import me.oldboy.core.model.service.*;
import me.oldboy.security.JwtTokenGenerator;
import org.hibernate.SessionFactory;

import java.sql.Connection;
import java.text.SimpleDateFormat;

/**
 * Application context for managing beans and dependencies.
 *
 * Класс с аннотацией @WebListener запускается сервлет-контейнером в первую очередь.
 */
@WebListener
public class AppContextBuilder implements ServletContextListener {

    private Connection connection;
    private SessionFactory sessionFactory;

    /**
     * Initializes the application context when the servlet context is initialized.
     *
     * @param sce the servlet context event
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final ServletContext servletContext = sce.getServletContext();

        startDatabaseMigration();
        serviceContextInit(servletContext);
        loadMappers(servletContext);
    }

    /**
     * Cleans up resources when the servlet context is destroyed.
     *
     * @param sce the servlet context event
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContextListener.super.contextDestroyed(sce);
    }

    private void startDatabaseMigration() {
        connection = ConnectionManager.getBaseConnection();

        if (Boolean.parseBoolean(PropertiesUtil.get("liquibase.enabled"))) {
            LiquibaseManager liquibaseManager = LiquibaseManager.getInstance();
            liquibaseManager.migrationsStart(connection);
        }
    }

    /**
     * Initializes service layer components and sets them as servlet context attributes.
     *
     * @param servletContext the servlet context
     */
    private void serviceContextInit(ServletContext servletContext) {

        sessionFactory = HibernateUtil.buildSessionFactory();
        servletContext.setAttribute("sessionFactory", sessionFactory);

        /* Инициализация "слоя репозиториев" */
        UserRepository userRepository = new UserRepository(sessionFactory);
        PlaceRepository placeRepository = new PlaceRepository(sessionFactory);
        SlotRepository slotRepository = new SlotRepository(sessionFactory);
        ReservationRepository reservationRepository = new ReservationRepository(sessionFactory);
        AuditRepository auditRepository = new AuditRepository(sessionFactory);

        /* Инициализация "слоя сервисов" */
        UserService userService = new UserService(userRepository);
        PlaceService placeService = new PlaceService(placeRepository);
        SlotService slotService =
                new SlotService(slotRepository, reservationRepository);
        ReservationService reservationService =
                new ReservationService(reservationRepository, slotRepository, placeRepository, userRepository);

        /* Инициализация части "раздела аспектов" */
        AuditService auditService = AuditService.getInstance(auditRepository);
        AuditingAspect.setAuditService(auditService);

        /* Инициализация "раздела безопасности" */
        JwtTokenGenerator jwtTokenGenerator = new JwtTokenGenerator(PropertiesUtil.get("jwt.secret"));
        SecurityService securityService = new SecurityService(userRepository, jwtTokenGenerator);

        /* Инициализация "слоя контроллеров" */
        UserController userController = new UserController(userService, securityService);
        PlaceController placeController = new PlaceController(placeService);
        SlotController slotController = new SlotController(slotService);
        ReservationController reservationController =
                new ReservationController(reservationService, placeService, slotService, userService);

        /* Загружаем атрибуты в контекст */
        servletContext.setAttribute("jwtTokenGenerator", jwtTokenGenerator);
        servletContext.setAttribute("securityService", securityService);

        servletContext.setAttribute("userService", userService);
        servletContext.setAttribute("placeService", placeService);
        servletContext.setAttribute("slotService", slotService);
        servletContext.setAttribute("reservationService", reservationService);

        servletContext.setAttribute("userController", userController);
        servletContext.setAttribute("placeController", placeController);
        servletContext.setAttribute("slotController", slotController);
        servletContext.setAttribute("reservationController", reservationController);
    }

    /**
     * Loads and configures object mappers, such as Jackson ObjectMapper.
     *
     * @param servletContext the servlet context
     */
    private void loadMappers(ServletContext servletContext) {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        objectMapper.registerModule(module);
        servletContext.setAttribute("objectMapper", objectMapper);
    }
}