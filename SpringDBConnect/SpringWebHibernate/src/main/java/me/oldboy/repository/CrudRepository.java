package me.oldboy.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<K, T> {

    List<T> findAll();

    Optional<T> findById(K id);

    boolean delete(K id);

    void update(T entity);

    Optional<T> save(T entity);
}
