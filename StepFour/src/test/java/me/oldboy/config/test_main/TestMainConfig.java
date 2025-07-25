package me.oldboy.config.test_main;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan({
        "me.oldboy.config.test_data_source"
        ,"me.oldboy.config.test_security_config"
        ,"me.oldboy.config.security_details"
        ,"me.oldboy.config.jwt_config"
        ,"me.oldboy.controllers"
        ,"me.oldboy.services"
        ,"me.oldboy.repository"
        ,"me.oldboy.exception"
})
public class TestMainConfig {
}
