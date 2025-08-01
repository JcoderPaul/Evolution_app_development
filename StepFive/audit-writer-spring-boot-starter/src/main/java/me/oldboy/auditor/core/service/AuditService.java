package me.oldboy.auditor.core.service;

import me.oldboy.auditor.core.entity.Audit;
import me.oldboy.auditor.core.entity.operations.AuditOperationResult;
import me.oldboy.auditor.core.entity.operations.AuditOperationType;
import me.oldboy.auditor.core.repository.AuditRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing audits.
 */
@Transactional(readOnly = true)
public class AuditService {

    private AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Performs an audit for a specific action.
     *
     * @param operationType   the type of operation (CREATE, DELETE ...)
     * @param operationResult the type of audit (SUCCESS or FAIL)
     * @param auditableRecord string representation of auditable entity
     * @return audit entity for DB record
     */
    @Transactional
    public Audit saveAudRecord(AuditOperationType operationType,
                               String auditableRecord,
                               AuditOperationResult operationResult) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        Audit audit = Audit.builder()
                .userName(userName)
                .auditResult(operationResult)
                .operationType(operationType)
                .auditableRecord(auditableRecord)
                .auditTimeStamp(LocalDateTime.now())
                .build();

        return auditRepository.save(audit);
    }

    public List<Audit> getAllAudit() {
        return auditRepository.findAll();
    }
}