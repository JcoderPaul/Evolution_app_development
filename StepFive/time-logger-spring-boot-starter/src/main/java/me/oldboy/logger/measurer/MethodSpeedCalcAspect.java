package me.oldboy.logger.measurer;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Slf4j
@Aspect
public class MethodSpeedCalcAspect {

    @Pointcut("@annotation(me.oldboy.logger.annotation.Measurable) && execution(* *(..))")
    public void speedMeasurable() {
    }

    @Around("speedMeasurable()")
    public Object loggableMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();

        log.info("Calling method (начало метода): {}", methodName);

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        log.info("Execution of method (обработка и завершение метода) {} finished. " +
                "Execution time is (время работы метода в мс.) {} ms.", methodName, (endTime - startTime));
        return result;
    }
}
