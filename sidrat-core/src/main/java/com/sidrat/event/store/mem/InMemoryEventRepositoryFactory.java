package com.sidrat.event.store.mem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sidrat.event.store.EventReader;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.store.EventStore;

public class InMemoryEventRepositoryFactory implements EventRepositoryFactory {
    private static Map<String, InMemoryEventRepository> STORES = new ConcurrentHashMap<>();
    
    public InMemoryEventRepositoryFactory() {
    }

    @Override
    public EventStore store(String name) {
        InMemoryEventRepository store = new InMemoryEventRepository();
        STORES.put(name, store);
        return store;
    }

    @Override
    public EventReader reader(String name) {
        InMemoryEventRepository store = STORES.get(name);
        if (store == null)
            return null;
        return store;
    }


}
