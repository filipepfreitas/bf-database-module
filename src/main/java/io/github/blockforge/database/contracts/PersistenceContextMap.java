package io.github.blockforge.database.contracts;

import java.util.Map;

public interface PersistenceContextMap {

    public void register(String name, PersistenceContext persistenceContext);

    public <T extends PersistenceContext> T getContext(String name, Class<T> type);

    public boolean contains(String name);

    public void putAll(Map<String, PersistenceContext> map);

    public Map<String, PersistenceContext> getMap();

}
