package me.oldboy.listeners;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import me.oldboy.config.AppContextConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@WebListener
public class ContextBuilder implements ServletContextListener {

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ApplicationContext springContext = new AnnotationConfigApplicationContext(AppContextConfig.class);
        ServletContext servletContext = servletContextEvent.getServletContext();
        servletContext.setAttribute("springContext", springContext);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ServletContextListener.super.contextDestroyed(servletContextEvent);
    }
}
