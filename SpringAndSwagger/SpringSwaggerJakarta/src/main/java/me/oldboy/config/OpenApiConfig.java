package me.oldboy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@EnableWebMvc
@EnableAutoConfiguration
@ComponentScan(basePackages = "me.oldboy.controller")
public class OpenApiConfig implements WebMvcConfigurer {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                            .title("Coworking API (User manipulation only)")
                            .contact(new Contact().name("Ермолаев Павел")
                                                  .url("https://t.me/Paul_J_Writer")
                                                  .email("Jcoder.Paul@gmail.com"))
                            .version("0.0.1")
                            .description("Small demo api (Spring and OpenAPI configuration example)")
        );
    }
}
