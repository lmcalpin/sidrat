package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Stored whenever we exit a method.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
@Entity
@Table(indexes = { @Index(columnList = "partition,time,id") }) // tediously copied on all entities
public class MethodExit extends SidratEvent {
    @ManyToOne(fetch = FetchType.EAGER)
    private MethodEntry methodEntry;
    @ManyToOne(fetch = FetchType.EAGER)
    private EncounteredObject object;
    @Lob
    private String value;

    public MethodEntry getMethodEntry() {
        return methodEntry;
    }

    public EncounteredObject getObject() {
        return object;
    }

    public String getValue() {
        return value;
    }

    public void setMethodEntry(MethodEntry methodEntry) {
        this.methodEntry = methodEntry;
    }

    public void setObject(EncounteredObject object) {
        this.object = object;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
