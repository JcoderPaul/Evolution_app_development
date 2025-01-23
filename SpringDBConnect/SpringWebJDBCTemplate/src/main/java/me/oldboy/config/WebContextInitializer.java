package me.oldboy.config;

import jakarta.servlet.*;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
/*
А вот эта аннотация весьма интересна, если ее не использовать, то приложение будет работать и даже тянуть данные
из БД, но запрос из нашего UserRestController.java на добавление пользователя, метод *.saveUser() в БД будет выкидывать:
org.springframework.web.HttpMediaTypeNotSupportedException: Content type 'text/plain;charset=UTF-8' not supported

При этом у нас должна быть задействована зависимость: com.fasterxml.jackson.core:jackson-databind
*/
@EnableWebMvc
@ComponentScan(basePackages = "me.oldboy")
public class WebContextInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext webContext) throws ServletException {
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(AppContextConfig.class);

        applicationContext.setServletContext(webContext);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext);
        ServletRegistration.Dynamic servlet = webContext.addServlet("dispatcher", dispatcherServlet);
        servlet.setLoadOnStartup(1);
        servlet.addMapping("/");
    }
}