package com.sidrat.event.store;

/**
 * Used to create a new instance of an EventStore
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public interface EventRepositoryFactory {
    public EventStore store(String name);

    public EventReader reader(String name);
}
