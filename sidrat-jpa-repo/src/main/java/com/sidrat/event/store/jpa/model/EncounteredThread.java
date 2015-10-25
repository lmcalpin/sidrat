package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import com.sidrat.event.store.jpa.Named;

@Entity
@Table(indexes = { @Index(columnList = "partition,id") }) // tediously copied on all entities
public class EncounteredThread extends BaseSidratEntity implements Named {
    private String name;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
