package me.oldboy.config.main_config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Main configuration class that defines the basic capabilities of the application
 */
@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {
        "me.oldboy.config.data_source"
        ,"me.oldboy.config.security_config"
        ,"me.oldboy.config.security_details"
        ,"me.oldboy.config.jwt_config"
        ,"me.oldboy.config.swagger"
        ,"me.oldboy.aspects"
        ,"me.oldboy.controllers"
        ,"me.oldboy.services"
        ,"me.oldboy.repository"
        ,"me.oldboy.exception"
})
public class MainAppConfig {

}
