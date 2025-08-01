package me.oldboy.auditor.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.auditor.core.auditing.AuditingAspect;
import me.oldboy.auditor.core.service.AuditService;
import me.oldboy.auditor.core.repository.AuditRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(AuditProperties.class)
@ConditionalOnClass(AuditProperties.class)
@ConditionalOnProperty(prefix = "audit.app", name = "runit", havingValue = "true")
public class AuditAutoConfiguration {

    /* Хотим видеть в логах, что происходит */
    @PostConstruct
    void init() {
        log.info("AuditorAutoConfiguration init");
    }

    @Bean
    @ConditionalOnMissingBean(AuditService.class)
    public AuditService audService(AuditRepository auditRepository) {
        return new AuditService(auditRepository);
    }

    @Bean
    @ConditionalOnMissingBean(AuditingAspect.class)
    public AuditingAspect audAspect(AuditService auditService) {
        return new AuditingAspect(auditService);
    }
}
