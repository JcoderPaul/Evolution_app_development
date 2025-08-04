package me.oldboy.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * The aspect is used to measure the execution
 * time of a method annotated with @Measurable
 *
 * It records the method name, the start of the
 * method, the end of the method, and the execution t
 * ime.
 */
@Slf4j
@Aspect
@Component
public class MethodSpeedCalcAspect {

    @Pointcut("@annotation(me.oldboy.annotations.Measurable) && execution(* *(..))")
    public void speedMeasurable(){}

    /**
     * Advice that wraps around the methods matched by the pointcut.
     * It logs the start and end of the method execution, as well as the execution time.
     *
     * @param joinPoint the join point
     * @return the result of the method execution
     * @throws Throwable if the invoked proceed throws anything
     */
    @Around("speedMeasurable()")
    public Object loggableMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        /*
        Можно использовать вариант конкатенации строк: log.info("Calling method (начало метода): " + methodName);
        Но, в реальных приложениях для ускорения процесса рекомендуется применять вариант с аргументами см. ниже.
        */
        log.info("Calling method (начало метода): {}", methodName);

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        log.info("Execution of method (обработка и завершение метода) {} finished. " +
                "Execution time is (время работы метода в мс.) {} ms.", methodName, (endTime - startTime));
        return result;
    }
}
