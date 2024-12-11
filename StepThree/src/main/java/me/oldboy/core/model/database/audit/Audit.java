package me.oldboy.core.model.database.audit;

import lombok.*;
import me.oldboy.core.model.database.audit.operations.AuditOperationResult;
import me.oldboy.core.model.database.audit.operations.AuditOperationType;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents an audit log entry capturing information about a user's actions
 * Сущность для фиксации в БД CRUD операций пользователя
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_cw", schema = "coworking")
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aud_id")
    private Long auditId;

    @Column(name = "creator")
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(name = "aud_result")
    private AuditOperationResult auditResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "aud_operation")
    private AuditOperationType operationType;

    @Column(name = "aud_entity")
    private String auditableRecord;

    @Column(name = "aud_time")
    private LocalDateTime auditTimeStamp;
}