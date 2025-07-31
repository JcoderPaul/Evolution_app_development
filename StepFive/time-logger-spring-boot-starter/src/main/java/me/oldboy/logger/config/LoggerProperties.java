package me.oldboy.logger.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "app.time.logger")
public class LoggerProperties {
    private boolean enabled;
}
