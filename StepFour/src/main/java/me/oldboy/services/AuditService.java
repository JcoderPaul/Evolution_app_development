package me.oldboy.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.models.audit.Audit;
import me.oldboy.models.audit.operations.AuditOperationResult;
import me.oldboy.models.audit.operations.AuditOperationType;
import me.oldboy.repository.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing audits.
 */
@Service
@AllArgsConstructor
@NoArgsConstructor
@Transactional(readOnly = true)
public class AuditService {

    @Autowired
    private AuditRepository auditRepository;

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

    /**
     * Return all audit records
     *
     * @return List of Audit object (audit record)
     */
    public List<Audit> getAllAudit() {
        return auditRepository.findAll();
    }
}