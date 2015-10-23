package com.sidrat.event.store.jpa.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * Stored when we begin executing a method.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
@Entity
public class MethodEntry extends BaseSidratEntity {
    @ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    private EncounteredObject object;
    @ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    private EncounteredThread thread;
    @ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
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
