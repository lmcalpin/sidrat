package com.sidrat.event.store.mem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sidrat.event.store.EventReader;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.store.EventStore;

public class InMemoryEventStoreFactory implements EventRepositoryFactory {
    private static Map<String, InMemoryEventStore> STORES = new ConcurrentHashMap<>();
    
    public InMemoryEventStoreFactory() {
    }

    @Override
    public EventStore store(String name) {
        InMemoryEventStore store = STORES.get(name);
        if (store == null) {
            store = new InMemoryEventStore();
            STORES.put(name, store);
        }
        return store;
    }

    @Override
    public EventReader reader(String name) {
        InMemoryEventStore store = STORES.get(name);
        if (store == null)
            return null;
        return new InMemoryEventReader(store);
    }


}
