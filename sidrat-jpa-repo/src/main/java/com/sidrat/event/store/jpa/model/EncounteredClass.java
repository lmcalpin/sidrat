package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;

import com.sidrat.event.store.jpa.Named;

@Entity
public class EncounteredClass extends BaseSidratEntity implements Named {
    private String name;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
