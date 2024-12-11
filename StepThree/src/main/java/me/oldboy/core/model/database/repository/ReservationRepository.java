package me.oldboy.core.model.database.repository;

import me.oldboy.core.model.criteria.expander.CriteriaQueryResultExpander;
import me.oldboy.core.model.database.entity.Place;
import me.oldboy.core.model.database.entity.Reservation;
import me.oldboy.core.model.database.entity.Slot;
import me.oldboy.core.model.database.entity.User;
import me.oldboy.core.model.database.entity.options.CwEntity;
import me.oldboy.core.model.database.repository.crud.RepositoryBase;
import org.hibernate.SessionFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationRepository extends RepositoryBase<Long, Reservation> {

    public ReservationRepository(SessionFactory sessionFactory) {
        super(Reservation.class, sessionFactory);
    }

    public Optional<List<Reservation>> findReservationByDate(LocalDate date) {
        /* Получаем экземпляр CriteriaBuilder из EntityManager - это наша точка входа для создания элементов запрос */
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();

        /* Создаем CriteriaQuery - он нужен для построения других частей запроса: критериев, выражений, предикатов, выборок */
        CriteriaQuery<Reservation> rCriteriaQuery = criteriaBuilder.createQuery(Reservation.class);

        /* Задаем корневой объект критериев для которого выполняем запрос */
        Root<Reservation> rRoot = rCriteriaQuery.from(Reservation.class);

        /* Формируем выборку select и условия выборки */
        rCriteriaQuery.select(rRoot);
        Predicate condition = criteriaBuilder.equal(rRoot.get("reservationDate"), date);
        rCriteriaQuery.where(condition);

        /* Возвращаем результат критериального запроса */
        return CriteriaQueryResultExpander.getOptionalResultList(this.getEntityManager().createQuery(rCriteriaQuery));
    }

    /* Ниже идут методы, которые дублируют во многом друг друга, но моя задача показать возможности Criteria API, пусть и не все */
    public <E extends CwEntity> Optional<List<Reservation>> findReservationByCwEntity(E entity) {
        /* Получаем экземпляр CriteriaBuilder из EntityManager - это наша точка входа для создания элементов запрос */
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();

        /* Создаем CriteriaQuery - он нужен для построения других частей запроса: критериев, выражений, предикатов, выборок */
        CriteriaQuery<Reservation> rCriteriaQuery = criteriaBuilder.createQuery(Reservation.class);

        /* Задаем корневой объект критериев для которого выполняем запрос */
        Root<Reservation> rRoot = rCriteriaQuery.from(Reservation.class);
        rCriteriaQuery.select(rRoot);

        /* Фактически тут бы хватило и простой предикативной переменной, но для демонстрации применим коллекцию предикатов */
        List<Predicate> predicates = new ArrayList<>();
        /* Формируем выборку 'select' и условия этой выборки 'where' */
        if(entity instanceof Place) {
            predicates.add(criteriaBuilder.equal(rRoot.get("place").get("placeId"), ((Place) entity).getPlaceId()));
        }
        if(entity instanceof Slot) {
            predicates.add(criteriaBuilder.equal(rRoot.get("slot").get("slotId"), ((Slot) entity).getSlotId()));
        }
        if(entity instanceof User) {
            predicates.add(criteriaBuilder.equal(rRoot.get("user").get("userId"), ((User) entity).getUserId()));
        }
        /* Как уже было сказано выше переменной было бы достаточно, т.к. в итоге у нас получается список с единственным элементом */
        rCriteriaQuery.where(predicates.toArray(Predicate[]::new));

        /* Возвращаем результат критериального запроса */
        return CriteriaQueryResultExpander.getOptionalResultList(this.getEntityManager().createQuery(rCriteriaQuery));
    }

    public Optional<List<Reservation>> findReservationByPlaceId(Long placeId) {
        /* Получаем экземпляр CriteriaBuilder из EntityManager - это наша точка входа для создания элементов запрос */
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();

        /* Создаем CriteriaQuery - он нужен для построения других частей запроса: критериев, выражений, предикатов, выборок */
        CriteriaQuery<Reservation> rCriteriaQuery = criteriaBuilder.createQuery(Reservation.class);

        /* Задаем корневой объект критериев для которого выполняем запрос */
        Root<Reservation> rRoot = rCriteriaQuery.from(Reservation.class);

        /* Формируем выборку select и условия выборки */
        rCriteriaQuery.select(rRoot);
        Predicate condition = criteriaBuilder.equal(rRoot.get("place").get("placeId"), placeId);
        rCriteriaQuery.where(condition);

        /* Возвращаем результат критериального запроса */
        return CriteriaQueryResultExpander.getOptionalResultList(this.getEntityManager().createQuery(rCriteriaQuery));
    }

    public Optional<List<Reservation>> findReservationBySlotId(Long slotId) {
        /* Получаем экземпляр CriteriaBuilder из EntityManager - это наша точка входа для создания элементов запрос */
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();

        /* Создаем CriteriaQuery - он нужен для построения других частей запроса: критериев, выражений, предикатов, выборок */
        CriteriaQuery<Reservation> rCriteriaQuery = criteriaBuilder.createQuery(Reservation.class);

        /* Задаем корневой объект критериев для которого выполняем запрос */
        Root<Reservation> rRoot = rCriteriaQuery.from(Reservation.class);

        /* Формируем выборку select и условия выборки */
        rCriteriaQuery.select(rRoot);
        Predicate condition = criteriaBuilder.equal(rRoot.get("slot").get("slotId"), slotId);
        rCriteriaQuery.where(condition);

        /* Возвращаем результат критериального запроса */
        return CriteriaQueryResultExpander.getOptionalResultList(this.getEntityManager().createQuery(rCriteriaQuery));
    }

    public Optional<List<Reservation>> findReservationByUserId(Long userId) {
        /* Получаем экземпляр CriteriaBuilder из EntityManager - это наша точка входа для создания элементов запрос */
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();

        /* Создаем CriteriaQuery - он нужен для построения других частей запроса: критериев, выражений, предикатов, выборок */
        CriteriaQuery<Reservation> rCriteriaQuery = criteriaBuilder.createQuery(Reservation.class);

        /* Задаем корневой объект критериев для которого выполняем запрос */
        Root<Reservation> rRoot = rCriteriaQuery.from(Reservation.class);

        /* Формируем выборку select и условия выборки */
        rCriteriaQuery.select(rRoot);
        Predicate condition = criteriaBuilder.equal(rRoot.get("user").get("userId"), userId);
        rCriteriaQuery.where(condition);

        /* Возвращаем результат критериального запроса */
        return CriteriaQueryResultExpander.getOptionalResultList(this.getEntityManager().createQuery(rCriteriaQuery));
    }

    public Optional<Reservation> findReservationByDatePlaceAndSlot(LocalDate date,
                                                                   Long placeId,
                                                                   Long slotId) {
        /* Получаем экземпляр CriteriaBuilder из EntityManager - это наша точка входа для создания элементов запрос */
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();

        /* Создаем CriteriaQuery - он нужен для построения других частей запроса: критериев, выражений, предикатов, выборок */
        CriteriaQuery<Reservation> rCriteriaQuery = criteriaBuilder.createQuery(Reservation.class);

        /* Задаем корневой объект критериев для которого выполняем запрос */
        Root<Reservation> rRoot = rCriteriaQuery.from(Reservation.class);
        rCriteriaQuery.select(rRoot);
        Predicate dateCondition = criteriaBuilder.equal(rRoot.get("reservationDate"), date);
        Predicate placeIdCondition = criteriaBuilder.equal(rRoot.get("place").get("placeId"), placeId);
        Predicate slotIdCondition = criteriaBuilder.equal(rRoot.get("slot").get("slotId"), slotId);
        rCriteriaQuery.where(criteriaBuilder.and(dateCondition, placeIdCondition, slotIdCondition));

        /* Возвращаем результат критериального запроса, в Criteria API нет метода Query.getOptionalResult(), создадим его */
        return CriteriaQueryResultExpander.getOptionalResult(this.getEntityManager().createQuery(rCriteriaQuery));
    }
}