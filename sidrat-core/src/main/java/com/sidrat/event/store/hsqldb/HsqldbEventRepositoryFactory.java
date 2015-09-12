package com.sidrat.event.store.hsqldb;

import com.sidrat.event.store.EventReader;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.store.EventStore;

public class HsqldbEventRepositoryFactory implements EventRepositoryFactory {

    @Override
    public EventStore store(String name) {
        return new HsqldbEventStore(name);
    }

    @Override
    public EventReader reader(String name) {
        return new HsqldbEventReader(name);
    }

}
