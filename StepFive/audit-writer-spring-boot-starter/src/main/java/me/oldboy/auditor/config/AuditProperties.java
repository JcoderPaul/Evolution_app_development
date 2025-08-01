package me.oldboy.auditor.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "audit.app")
public class AuditProperties {
    private boolean runit;
}
