package edu.java.domain;

import java.util.Collection;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

public interface BaseEntityRepository<E> {
    boolean add(E entity);

    @Nullable E remove(E entity);

    Collection<E> findAll();

    Collection<E> search(Predicate<E> condition);
}
