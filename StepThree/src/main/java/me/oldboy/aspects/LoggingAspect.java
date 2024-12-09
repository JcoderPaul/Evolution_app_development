package me.oldboy.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Aspect for logging methods annotated with {@link me.oldboy.annotations.Loggable}
 * Аспектный класс для обработки методов помеченных аннотацией @Loggable
 */

/*
Вместо конструкции: private static final Logger log = LoggerFactory.getLogger(OurClassName.class);
где, OurClassName - имя нашего класса, например PlaceController, будем использовать декларативный
вариант через аннотацию. Тут мы задействовали универсальный @Slf4j, а не конкретный @Log4J / @Log4J2
*/
@Slf4j
@Aspect
public class LoggingAspect {

    /**
     * Pointcut definition to match methods annotated with {@link me.oldboy.annotations.Loggable}.
     * Точка среза фиксирующая аннотированные @Loggable методы
     */
    @Pointcut("@annotation(me.oldboy.annotations.Loggable) && execution(* *(..))")
    public void loggableMethods(){}

    /**
     * Advice to log method entry, execution time, and exit.
     * Адвайс-метод запускающий логирование при старте метода, его финише и рассчитывающий время работы метода
     *
     * @param joinPoint The ProceedingJoinPoint for the intercepted method.
     * @return The result of the intercepted method.
     * @throws Throwable If an error occurs during method execution.
     */
    @Around("loggableMethods()")
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