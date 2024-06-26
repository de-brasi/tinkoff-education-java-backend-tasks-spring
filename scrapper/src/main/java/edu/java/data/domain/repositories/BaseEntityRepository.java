package edu.java.data.domain.repositories;

import java.util.Collection;
import java.util.function.Predicate;

public interface BaseEntityRepository<E> {
    int add(E entity);

    int remove(E entity);

    Collection<E> findAll();

    Collection<E> search(Predicate<E> condition);

    Long getEntityId(E entity);
}
