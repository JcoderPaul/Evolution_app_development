package me.oldboy.logger.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.logger.measurer.MethodSpeedCalcAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@Configuration
@EnableConfigurationProperties(LoggerProperties.class)
@ConditionalOnClass(LoggerProperties.class)
@ConditionalOnProperty(prefix = "app.time.logger", name = "enabled", havingValue = "true")
public class LoggerAutoConfiguration {
    @PostConstruct
    void init() {
        log.info("LoggerAutoConfiguration init");
    }

    @Bean
    @ConditionalOnMissingBean
    public MethodSpeedCalcAspect firstAspect() {
        return new MethodSpeedCalcAspect();
    }
}
