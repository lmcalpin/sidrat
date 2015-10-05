package com.sidrat.event.store.hibernate;


import com.sidrat.event.store.EventReader;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.store.EventStore;

public class JPAEventRepositoryFactory implements EventRepositoryFactory {

    @Override
    public EventStore store(String name) {
        return new JPAEventStore(name);
    }

    @Override
    public EventReader reader(String name) {
        return new JPAEventReader(name);
    }

}
