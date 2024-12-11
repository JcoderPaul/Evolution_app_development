package me.oldboy.core.model.database.repository.crud;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.SessionFactory;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public abstract class RepositoryBase<K extends Serializable, T> implements Repository<K, T> {

    private final Class<T> clazz;
    @Getter
    private final SessionFactory sessionFactory;
    @Getter
    @Setter
    private EntityManager entityManager;

    public RepositoryBase(Class<T> clazz, SessionFactory sessionFactory) {
        this.clazz = clazz;
        this.sessionFactory = sessionFactory;
        this.entityManager = sessionFactory.getCurrentSession();
    }

    @Override
    public T create(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    @Override
    public void delete(K id) {
        entityManager.remove(entityManager.find(clazz, id));
        entityManager.flush();
    }

    @Override
    public void update(T entity) {
        entityManager.merge(entity);
        entityManager.flush();
    }

    @Override
    @Transactional
    public Optional<T> findById(K id) {
        return Optional.ofNullable(entityManager.find(clazz, id));
    }

    @Override
    @Transactional
    public List<T> findAll() {
        /* Получаем экземпляр CriteriaBuilder из EntityManager - это наша точка входа для создания элементов запрос */
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        /* Создаем CriteriaQuery - он нужен для построения других частей запроса: критериев, выражений, предикатов, выборок */
        CriteriaQuery<T> tCriteriaQuery = criteriaBuilder.createQuery(clazz);

        /* Задаем корневой объект критериев для которого выполняем запрос */
        Root<T> tRoot = tCriteriaQuery.from(clazz);
        tCriteriaQuery.select(tRoot);

        /* Возвращаем результат критериального запроса */
        return entityManager.createQuery(tCriteriaQuery).getResultList();
    }
}