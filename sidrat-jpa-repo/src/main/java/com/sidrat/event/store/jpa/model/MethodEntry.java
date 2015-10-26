package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Stored when we begin executing a method.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
@Entity
@Table(indexes = { @Index(columnList = "partition,time,id") }) // tediously copied on all entities
public class MethodEntry extends SidratEvent {
    @ManyToOne(fetch = FetchType.EAGER)
    private EncounteredObject object;
    @ManyToOne(fetch = FetchType.EAGER)
    private EncounteredThread thread;
    @ManyToOne(fetch = FetchType.EAGER)
    private EncounteredMethod method;

    public EncounteredMethod getMethod() {
        return method;
    }

    public EncounteredObject getObject() {
        return object;
    }

    public EncounteredThread getThread() {
        return thread;
    }

    public void setMethod(EncounteredMethod method) {
        this.method = method;
    }

    public void setObject(EncounteredObject object) {
        this.object = object;
    }

    public void setThread(EncounteredThread thread) {
        this.thread = thread;
    }
}
