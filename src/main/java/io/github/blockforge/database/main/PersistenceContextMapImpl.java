package io.github.blockforge.database.main;

import io.github.blockforge.database.contracts.PersistenceContext;
import io.github.blockforge.database.contracts.PersistenceContextMap;

import java.util.HashMap;
import java.util.Map;


public class PersistenceContextMapImpl implements PersistenceContextMap {

    private final Map<String, PersistenceContext> contexts = new HashMap<>();

    @Override
    public void register(String name, PersistenceContext persistenceContext) {
        contexts.put(name, persistenceContext);
    }

    @Override
    public <T extends PersistenceContext> T getContext(String name, Class<T> type) {
        return type.cast(contexts.get(name));
    }

    @Override
    public boolean contains(String name) {
        return contexts.containsKey(name);
    }

    @Override
    public void putAll(Map<String, PersistenceContext> map) {
        contexts.putAll(map);
    }

    @Override
    public Map<String, PersistenceContext> getMap() {
        return contexts;
    }

}
