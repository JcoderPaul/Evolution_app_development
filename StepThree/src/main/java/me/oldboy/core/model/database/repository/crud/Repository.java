package me.oldboy.core.model.database.repository.crud;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface Repository<K extends Serializable, E> {
    E create(E entity);

    void delete(K id);

    void update(E entity);

    Optional<E> findById(K id);

    List<E> findAll();
}
