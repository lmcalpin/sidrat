package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class EncounteredObject extends BaseSidratEntity {
    @ManyToOne
    private EncounteredClass clazz;

    public EncounteredClass getClazz() {
        return clazz;
    }

    public void setClazz(EncounteredClass clazz) {
        this.clazz = clazz;
    }
}
