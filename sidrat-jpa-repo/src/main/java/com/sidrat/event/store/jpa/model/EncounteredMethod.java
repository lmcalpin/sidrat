package com.sidrat.event.store.jpa.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import com.sidrat.event.store.jpa.Named;

@Entity
public class EncounteredMethod extends BaseSidratEntity implements Named {
    @ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    private EncounteredClass clazz;
    private String name;

    public EncounteredClass getClazz() {
        return clazz;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setClazz(EncounteredClass object) {
        this.clazz = object;
    }

    public void setName(String name) {
        this.name = name;
    }

}
