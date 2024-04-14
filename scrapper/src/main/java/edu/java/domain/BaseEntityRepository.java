package edu.java.domain;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public interface BaseEntityRepository<E> {
    boolean add(E entity);

    Optional<E> remove(E entity);

    Collection<E> findAll();

    Collection<E> search(Predicate<E> condition);
}
