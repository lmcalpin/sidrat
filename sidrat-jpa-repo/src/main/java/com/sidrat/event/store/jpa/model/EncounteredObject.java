package com.sidrat.event.store.jpa.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(indexes = { @Index(columnList = "partition,id") }) // tediously copied on all entities
public class EncounteredObject extends BaseSidratEntity {
    @ManyToOne(cascade = { CascadeType.PERSIST }, fetch = FetchType.EAGER)
    private EncounteredClass clazz;

    public EncounteredClass getClazz() {
        return clazz;
    }

    public void setClazz(EncounteredClass clazz) {
        this.clazz = clazz;
    }
}
