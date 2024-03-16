package edu.java.domain;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;

public interface BaseEntityRepository<Entity> {
    boolean add(Entity entity);
    @Nullable Entity remove(Entity entity);
    Collection<Entity> findAll();
}
