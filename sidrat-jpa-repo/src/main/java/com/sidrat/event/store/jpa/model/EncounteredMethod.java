package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.sidrat.event.store.jpa.Named;

@Entity
@Table(indexes = { @Index(columnList = "partition,name,id") }) // tediously copied on all entities
public class EncounteredMethod extends SidratValueObject implements Named {
    @ManyToOne(fetch = FetchType.EAGER)
    private EncounteredClass clazz;

    public EncounteredClass getClazz() {
        return clazz;
    }

    public void setClazz(EncounteredClass object) {
        this.clazz = object;
    }
}
