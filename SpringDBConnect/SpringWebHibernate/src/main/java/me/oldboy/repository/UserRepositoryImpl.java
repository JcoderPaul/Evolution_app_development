package me.oldboy.repository;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import me.oldboy.entity.User;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/* Применим несколько вариантов запросов к БД */
@NoArgsConstructor
@AllArgsConstructor
@Repository
public class UserRepositoryImpl implements CrudRepository<Long, User> {

    @Autowired
    private SessionFactory sessionFactory;

    private static final String FIND_USER_BY_ID_HQL = "SELECT u FROM User as u WHERE u.userId = :uId";
    private static final String FIND_USER_BY_NAME_HQL = "SELECT u FROM User as u WHERE u.userName = :uName";

    /* Применим язык запросов HQL - тут мы оперируем сущностями и их полями */
    @Override
    public Optional<User> findById(Long id) {
        User mayBeUser = null;
        try (Session session = getSession()) {
            session.beginTransaction();

            mayBeUser = session.createQuery(FIND_USER_BY_ID_HQL, User.class)
                          .setParameter("uId", id)
                          .getSingleResult();

            session.getTransaction().commit();
        } catch (Exception e){
             return Optional.empty();
        }
        return Optional.of(miniMapper(mayBeUser));
    }

    /* Применим Criteria API */
    @Override
    public List<User> findAll() {
        try (Session session = getSession()) {
            session.beginTransaction();

            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
            Root<User> root = criteriaQuery.from(User.class);
            criteriaQuery.select(root);
            TypedQuery<User> query = session.createQuery(criteriaQuery);
            List<User> userQuery = query.getResultList();

            session.getTransaction().commit();

            return userQuery.stream()
                            .map(this::miniMapper)
                            .toList();
        }
    }

    @Override
    public Optional<User> save(User newUser) {
        try (Session session = getSession()) {
            session.beginTransaction();

                session.persist(newUser);

            session.getTransaction().commit();

            return Optional.of(miniMapper(newUser));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean delete(Long id) {
        try (Session session = getSession()) {
            session.beginTransaction();

            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaDelete<User> criteriaDelete = criteriaBuilder.createCriteriaDelete(User.class);
            Root<User> root = criteriaDelete.from(User.class);
            criteriaDelete.where(criteriaBuilder.equal(root.get("userId"), id));

            int isUserDeleted = session.createQuery(criteriaDelete).executeUpdate();

            session.getTransaction().commit();

            if(isUserDeleted > 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void update(User entity) {

    }

    private Optional<User> getUserByName(String userName){
        User mayBeUser = null;
        try (Session session = getSession()) {
            session.beginTransaction();

            mayBeUser = session.createQuery(FIND_USER_BY_NAME_HQL, User.class)
                    .setParameter("uName", userName)
                    .getSingleResult();

            session.getTransaction().commit();
        }
        return Optional.of(miniMapper(mayBeUser));
    }

    private Session getSession(){
        return sessionFactory.openSession();
    }
    
    private User miniMapper(User userTo){
        return User.builder()
                .userId(userTo.getUserId())
                .userName(userTo.getUserName())
                .role(userTo.getRole())
                .build();
    }
}
