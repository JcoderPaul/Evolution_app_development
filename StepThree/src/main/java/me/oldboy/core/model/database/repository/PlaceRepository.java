package me.oldboy.core.model.database.repository;

import me.oldboy.core.model.criteria.expander.CriteriaQueryResultExpander;
import me.oldboy.core.model.database.entity.Place;
import me.oldboy.core.model.database.entity.options.Species;
import me.oldboy.core.model.database.repository.crud.RepositoryBase;
import org.hibernate.SessionFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class PlaceRepository extends RepositoryBase<Long, Place> {

    public PlaceRepository(SessionFactory sessionFactory) {
        super(Place.class, sessionFactory);
    }

    public Optional<Place> findPlaceBySpeciesAndNumber(Species species, Integer placeNumber){
        /* Получаем экземпляр CriteriaBuilder из EntityManager - это наша точка входа для создания элементов запрос */
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();

        /* Создаем CriteriaQuery - он нужен для построения других частей запроса: критериев, выражений, предикатов, выборок */
        CriteriaQuery<Place> pCriteriaQuery = criteriaBuilder.createQuery(Place.class);

        /* Задаем корневой объект критериев для которого выполняем запрос */
        Root<Place> pRoot = pCriteriaQuery.from(Place.class);
        pCriteriaQuery.select(pRoot);
        Predicate speciesCondition = criteriaBuilder.equal(pRoot.get("species"), species);
        Predicate placeNumberCondition = criteriaBuilder.equal(pRoot.get("placeNumber"), placeNumber);
        pCriteriaQuery.where(criteriaBuilder.and(speciesCondition,placeNumberCondition));

        /* Возвращаем результат критериального запроса, в Criteria API нет метода Query.getOptionalResult(), создадим его */
        return CriteriaQueryResultExpander.getOptionalResult(this.getEntityManager().createQuery(pCriteriaQuery));
    }

    public Optional<List<Place>> findAllPlacesBySpecies(Species species){
        /* Получаем экземпляр CriteriaBuilder из EntityManager - это наша точка входа для создания элементов запрос */
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();

        /* Создаем CriteriaQuery - он нужен для построения других частей запроса: критериев, выражений, предикатов, выборок */
        CriteriaQuery<Place> pCriteriaQuery = criteriaBuilder.createQuery(Place.class);

        /* Задаем корневой объект критериев для которого выполняем запрос */
        Root<Place> pRoot = pCriteriaQuery.from(Place.class);

        /* Формируем выборку select и условия выборки */
        pCriteriaQuery.select(pRoot);
        Predicate condition = criteriaBuilder.equal(pRoot.get("species"), species);
        pCriteriaQuery.where(condition);

        /* Возвращаем результат критериального запроса */
        return CriteriaQueryResultExpander.getOptionalResultList(this.getEntityManager().createQuery(pCriteriaQuery));
    }
}