package me.oldboy.core.model.criteria.expander;

/*
При работе с Criteria API возникла ситуация ловли NoResultException, а метода
решающего эту задачу нет, ну или в текущей версии пакета нет - допишем.
Пример и обсуждение см. https://github.com/jakartaee/persistence/issues/298
*/

import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class CriteriaQueryResultExpander {

    public static Object getSingleResultOrNull(Query query) {
        try {
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public static <T> Optional<T> getOptionalResult(TypedQuery<T> query) {
        try {
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    public static <T> Optional<List<T>> getOptionalResultList(TypedQuery<T> query) {
        try {
            return Optional.ofNullable(query.getResultList());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }
}
