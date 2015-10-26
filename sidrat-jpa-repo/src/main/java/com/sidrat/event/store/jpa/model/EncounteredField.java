package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.sidrat.event.store.jpa.Named;

/**
 * Stored whenever we encounter a local variable that we haven't seen before.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
@Entity
@Table(indexes = { @Index(columnList = "partition,name,id") }) // tediously copied on all entities
public class EncounteredField extends SidratValueObject implements Named {
    @ManyToOne(fetch = FetchType.EAGER)
    private EncounteredObject owner;

    public EncounteredObject getObject() {
        return owner;
    }

    public void setObject(EncounteredObject object) {
        this.owner = object;
    }
}
