package me.oldboy.aspects;

import lombok.extern.slf4j.Slf4j;
import me.oldboy.core.model.database.repository.crud.RepositoryBase;
import me.oldboy.core.model.service.ServiceBase;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Session;
import org.hibernate.Transaction;

/* Данный аспект отвечает за открытие и закрытие транзакций с уровня сервисов и репозиториев */

@Slf4j
@Aspect
public class TransactionAspect {

    /*
    Процесс открытия и закрытия транзакций, в конечном итоге, важен на уровне взаимодействующем с базой данных,
    в нашем приложении транзакции будут запускаться с двух уровней в основном с уровня сервисов и в особом
    случае с уровня репозиториев. Поэтому нужно две точки среза - pointcut применим одну стандартную аннотацию.
    */

    /**
     * Pointcut definition to match methods annotated with {@link javax.transaction.Transactional}.
     */
    @Pointcut("@annotation(javax.transaction.Transactional) && execution(* me.oldboy.core.model.service..*.*(..))")
    public void serviceTransactionalMethods(){}

    @Pointcut("@annotation(javax.transaction.Transactional) && execution(* me.oldboy.core.model.database.repository..*.*(..))")
    public void repositoryTransactionalMethods(){}

    /**
     * Advice to open transaction if necessary and close it after the method is executed.
     * @param joinPoint The ProceedingJoinPoint for the intercepted method.
     * @return The result of the intercepted method.
     * @throws Throwable If an error occurs during method execution.
     */
    @Around("serviceTransactionalMethods()")
    public Object serviceTransactionOpenAndCloseMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        Object targetMethodResult = null;

        ServiceBase serviceBase = (ServiceBase) joinPoint.getTarget();
        Session currentSession = serviceBase.getRepositoryBase().getSessionFactory().getCurrentSession();
        serviceBase.getRepositoryBase().setEntityManager(currentSession);
        Transaction transaction = currentSession.getTransaction();

        return transactionManager(joinPoint, transaction);
    }

    /**
     * Advice to open transaction if necessary and close it after the method is executed.
     * @param joinPoint The ProceedingJoinPoint for the intercepted method.
     * @return The result of the intercepted method.
     * @throws Throwable If an error occurs during method execution.
     */
    @Around("repositoryTransactionalMethods()")
    public Object repositoryTransactionOpenAndCloseMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        Object targetMethodResult = null;

        RepositoryBase repositoryBase = (RepositoryBase) joinPoint.getTarget();
        Session currentSession = repositoryBase.getSessionFactory().getCurrentSession();
        repositoryBase.setEntityManager(currentSession);
        Transaction transaction = currentSession.getTransaction();

        return transactionManager(joinPoint, transaction);
    }

    /* Метод управляющий (анализирующий) состоянием транзакций */
    private static Object transactionManager(ProceedingJoinPoint joinPoint,
                                             Transaction transaction) throws Throwable {
        Object targetMethodResult;
        boolean transactionStarted = false;

        if (!transaction.isActive()) {
            transaction.begin();
            transactionStarted = true;
        }

        try {
            targetMethodResult = joinPoint.proceed(); // Запускаем целевой - target метод
            if (transactionStarted) {
                transaction.commit();
            }
        } catch (Exception exception) {
            if (transactionStarted) {
                transaction.rollback();
            }
            throw exception;
        }
        return targetMethodResult;
    }
}
