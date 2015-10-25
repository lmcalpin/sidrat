package com.sidrat.event.store.jpa.model;

import javax.persistence.CascadeType;
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
@Table(indexes = { @Index(columnList = "partition,id") }) // tediously copied on all entities
public class EncounteredField extends BaseSidratEntity implements Named {
    @ManyToOne(cascade = { CascadeType.PERSIST }, fetch = FetchType.EAGER)
    private EncounteredObject owner;
    private String name;

    @Override
    public String getName() {
        return name;
    }

    public EncounteredObject getObject() {
        return owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setObject(EncounteredObject object) {
        this.owner = object;
    }

}
