package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(indexes = { @Index(columnList = "partition,name,id") }) // tediously copied on all value objects
public class EncounteredObject extends SidratValueObject {
    @ManyToOne(fetch = FetchType.EAGER)
    private EncounteredClass clazz;

    public EncounteredClass getClazz() {
        return clazz;
    }

    public void setClazz(EncounteredClass clazz) {
        this.clazz = clazz;
    }

    public void setName(Long identifier) {
        setName(String.valueOf(identifier));
    }
}
