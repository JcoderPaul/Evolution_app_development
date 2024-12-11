package me.oldboy.core.model.database.repository;

import me.oldboy.core.model.criteria.expander.CriteriaQueryResultExpander;
import me.oldboy.core.model.database.entity.User;
import me.oldboy.core.model.database.repository.crud.RepositoryBase;
import org.hibernate.SessionFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Optional;

public class UserRepository extends RepositoryBase<Long, User> {

    public UserRepository(SessionFactory sessionFactory) {
        super(User.class, sessionFactory);
    }

    public Optional<User> findUserByLogin(String login) {
        /* Получаем экземпляр CriteriaBuilder из EntityManager - это наша точка входа для создания элементов запрос */
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();

        /* Создаем CriteriaQuery - он нужен для построения других частей запроса: критериев, выражений, предикатов, выборок */
        CriteriaQuery<User> uCriteriaQuery = criteriaBuilder.createQuery(User.class);

        /* Задаем корневой объект критериев для которого выполняем запрос */
        Root<User> uRoot = uCriteriaQuery.from(User.class);
        uCriteriaQuery.select(uRoot);
        Predicate condition = criteriaBuilder.equal(uRoot.get("userName"), login);
        uCriteriaQuery.where(condition);

        /* Возвращаем результат критериального запроса, в Criteria API нет метода Query.getOptionalResult(), создадим его */
        return CriteriaQueryResultExpander.getOptionalResult(this.getEntityManager().createQuery(uCriteriaQuery));
    }

    public Optional<User> findUserByLoginAndPassword(String login, String password) {
        /* Получаем экземпляр CriteriaBuilder из EntityManager - это наша точка входа для создания элементов запрос */
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();

        /* Создаем CriteriaQuery - он нужен для построения других частей запроса: критериев, выражений, предикатов, выборок */
        CriteriaQuery<User> uCriteriaQuery = criteriaBuilder.createQuery(User.class);

        /* Задаем корневой объект критериев для которого выполняем запрос */
        Root<User> uRoot = uCriteriaQuery.from(User.class);
        uCriteriaQuery.select(uRoot);
        Predicate loginCondition = criteriaBuilder.equal(uRoot.get("userName"), login);
        Predicate passCondition = criteriaBuilder.equal(uRoot.get("password"), password);
        uCriteriaQuery.where(criteriaBuilder.and(loginCondition, passCondition));

        /* Возвращаем результат критериального запроса, в Criteria API нет метода Query.getOptionalResult(), создадим его */
        return CriteriaQueryResultExpander.getOptionalResult(this.getEntityManager().createQuery(uCriteriaQuery));
    }
}
