package me.oldboy.aspects;

import lombok.extern.slf4j.Slf4j;
import me.oldboy.annotations.Auditable;
import me.oldboy.models.audit.operations.AuditOperationResult;
import me.oldboy.models.audit.operations.AuditOperationType;
import me.oldboy.services.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Aspect for auditing annotated methods with {@link Auditable} annotation.
 */
@Slf4j
@Aspect
@Component
public class AuditingAspect {

    @Autowired
    private AuditService auditService;

    @Pointcut("@annotation(me.oldboy.annotations.Auditable) && execution(* *(..))")
    public void auditOperation() {
    }

    /**
     * Advice to perform auditing around methods annotated with {@link Auditable}.
     * @param joinPoint The ProceedingJoinPoint for the intercepted method.
     * @return The result of the intercepted method.
     * @throws Throwable If an error occurs during method execution.
     */
    @Around("auditOperation()")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Auditable auditAnnotationContain = methodSignature.getMethod().getAnnotation(Auditable.class);

        AuditOperationType operationTypeType = auditAnnotationContain.operationType();

        Object[] args = joinPoint.getArgs();
        String auditableRecord = args[0].toString();

        try {
            Object result = joinPoint.proceed();
            auditService.saveAudRecord(operationTypeType, auditableRecord, AuditOperationResult.SUCCESS);
            return result;
        } catch (Throwable ex) {
            auditService.saveAudRecord(operationTypeType, auditableRecord, AuditOperationResult.FAIL);
            throw ex;
        }
    }
}