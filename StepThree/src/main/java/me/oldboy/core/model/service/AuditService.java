package me.oldboy.core.model.service;

import me.oldboy.core.model.database.audit.Audit;
import me.oldboy.core.model.database.audit.operations.AuditOperationResult;
import me.oldboy.core.model.database.audit.operations.AuditOperationType;
import me.oldboy.core.model.database.repository.crud.RepositoryBase;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing audits.
 */
public class AuditService extends ServiceBase<Long, Audit>{

    private static AuditService instance;

    private AuditService(RepositoryBase<Long, Audit> repositoryBase) {
        super(repositoryBase);
    }

    public static AuditService getInstance(RepositoryBase<Long, Audit> repositoryBase){
        if(instance == null){
            instance = new AuditService(repositoryBase);
        }
        return instance;
    }

    /**
     * Performs an audit for a specific action.
     *
     * @param userName the userName associated with the operation
     * @param operationType the type of operation (CREATE, DELETE ...)
     * @param operationResult the type of audit (SUCCESS or FAIL)
     * @param auditableRecord string representation of auditable entity
     * @return audit entity for DB record
     */
    @Transactional
    public Audit saveAudRecord(String userName,
                               AuditOperationType operationType,
                               String auditableRecord,
                               AuditOperationResult operationResult) {

        Audit audit = Audit.builder()
                .userName(userName)
                .auditResult(operationResult)
                .operationType(operationType)
                .auditableRecord(auditableRecord)
                .auditTimeStamp(LocalDateTime.now())
                .build();

        return getRepositoryBase().create(audit);
    }

    @Transactional
    public List<Audit> getAllAudit(){
        return getRepositoryBase().findAll();
    }
}
