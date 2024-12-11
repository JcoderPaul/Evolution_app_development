package me.oldboy.core.model.database.repository;

import me.oldboy.core.model.criteria.expander.CriteriaQueryResultExpander;
import me.oldboy.core.model.database.entity.Slot;
import me.oldboy.core.model.database.repository.crud.RepositoryBase;
import org.hibernate.SessionFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Optional;

public class SlotRepository extends RepositoryBase<Long, Slot> {

    public SlotRepository(SessionFactory sessionFactory) {
        super(Slot.class, sessionFactory);
    }

    public Optional<Slot> findSlotByNumber(Integer slotNumber){
        /* Получаем экземпляр CriteriaBuilder из EntityManager - это наша точка входа для создания элементов запрос */
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();

        /* Создаем CriteriaQuery - он нужен для построения других частей запроса: критериев, выражений, предикатов, выборок */
        CriteriaQuery<Slot> sCriteriaQuery = criteriaBuilder.createQuery(Slot.class);

        /* Задаем корневой объект критериев для которого выполняем запрос */
        Root<Slot> sRoot = sCriteriaQuery.from(Slot.class);

        /* Формируем выборку select и условия выборки */
        sCriteriaQuery.select(sRoot);
        Predicate condition = criteriaBuilder.equal(sRoot.get("slotNumber"), slotNumber);
        sCriteriaQuery.where(condition);

        /* Возвращаем результат критериального запроса */
        return CriteriaQueryResultExpander.getOptionalResult(this.getEntityManager().createQuery(sCriteriaQuery));
    }
}
