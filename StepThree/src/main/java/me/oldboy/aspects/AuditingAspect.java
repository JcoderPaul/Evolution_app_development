package me.oldboy.aspects;

import lombok.extern.slf4j.Slf4j;
import me.oldboy.annotations.Auditable;
import me.oldboy.core.model.database.audit.operations.AuditOperationResult;
import me.oldboy.core.model.database.audit.operations.AuditOperationType;
import me.oldboy.core.model.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Aspect for auditing annotated methods with {@link Auditable} annotation.
 */

@Slf4j
@Aspect
public class AuditingAspect {

    private static AuditService auditService;

    public static void setAuditService(AuditService auditService) {
        AuditingAspect.auditService = auditService;
    }

    /*
    Данная точка среза нужна для отключения аудита или вернее advice-a при проведении Unit-тестов.
    Поскольку у нас Web - приложение и инициализация происходит весьма экстравагантно, возникает
    ситуация при тестировании, когда аспекты включаются в работу, но контекст активен частично и
    AuditService не инициализирован - получаем NullPointerException. Есть вариант, как и на слое
    сервисов или репозиториев собрать тест - контейнер, полностью поднять контекст и тогда даже
    действия тестов будут аудироваться - двух зайцев из одной рогатки. Но мы любим разнообразие и
    узнавать что-то новое - отключаем аудит при Unit-тестах слоя контроллеров.
    */
    @Pointcut("if()")
    public static boolean isActive() {
        return auditService != null;
    }

    @Pointcut("@annotation(me.oldboy.annotations.Auditable) && execution(* *(..))")
    public void auditOperation() {
    }

    /**
     * Advice to perform auditing around methods annotated with {@link Auditable}.
     * @param joinPoint The ProceedingJoinPoint for the intercepted method.
     * @return The result of the intercepted method.
     * @throws Throwable If an error occurs during method execution.
     */
    @Around("isActive() && auditOperation()")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Auditable auditAnnotationContain = methodSignature.getMethod().getAnnotation(Auditable.class);

        AuditOperationType operationTypeType = auditAnnotationContain.operationType();
        String userName = "";
        String auditableRecord = "";

        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            if(methodSignature.getParameterNames()[i].equals("userName")){
                userName = (String) args[i];
            }
            if(methodSignature.getParameterNames()[i].equals("createDto")){
                auditableRecord = args[i].toString();
            }
            if(methodSignature.getParameterNames()[i].equals("updateDto")){
                auditableRecord = args[i].toString();
            }
            if(methodSignature.getParameterNames()[i].equals("deleteDto")){
                auditableRecord = args[i].toString();
            }
        }

        try {
            Object result = joinPoint.proceed();
            auditService.saveAudRecord(userName, operationTypeType, auditableRecord, AuditOperationResult.SUCCESS);
            return result;
        } catch (Throwable ex) {
            auditService.saveAudRecord(userName, operationTypeType, auditableRecord, AuditOperationResult.FAIL);
            throw ex;
        }
    }
}