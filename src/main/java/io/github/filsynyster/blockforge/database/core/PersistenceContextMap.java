package io.github.filsynyster.blockforge.database.core;

import java.util.HashMap;
import java.util.Map;


public class PersistenceContextMap {

    private final Map<String, PersistenceContext> persistenceContexts = new HashMap<>();

    public void register(String name, PersistenceContext persistenceContext) {
        persistenceContexts.put(name, persistenceContext);
    }

    public <T extends PersistenceContext> T get(String name, Class<T> type) {
        return (T) persistenceContexts.get(name);
    }

    public boolean contains(String name) {
        return persistenceContexts.containsKey(name);
    }
}
