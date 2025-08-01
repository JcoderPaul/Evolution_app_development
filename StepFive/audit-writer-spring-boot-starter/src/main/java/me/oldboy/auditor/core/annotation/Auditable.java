package me.oldboy.auditor.core.annotation;

import me.oldboy.auditor.core.entity.operations.AuditOperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for auditing
 * Аннотация помечающая отслеживаемые методы
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Auditable {

    /**
     * Specifies the action type for auditing.
     *
     * @return ActionType enum value representing the action type.
     */
    AuditOperationType operationType();
}
