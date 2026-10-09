package io.github.filsynyster.blockforge.database.providers.jpa;

import io.github.filsynyster.blockforge.database.core.PersistenceContext;
import io.github.filsynyster.blockforge.database.core.repository.DataRepository;

public class JpaPersistenceContext implements PersistenceContext {

    @Override
    public void initialize() {

    }

    @Override
    public void register(Class<?> entity, DataRepository<?, ?> repository) {

    }

    @Override
    public <R extends DataRepository<?, ?>> R getRepository(Class<?> entity) {
        return null;
    }

    @Override
    public void close() {
    }
}
