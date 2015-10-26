package com.sidrat.event.store.jpa.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.sidrat.event.store.jpa.Named;

@MappedSuperclass
public class SidratValueObject extends BaseSidratEntity implements Named {
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue
    private Long id;

    private String name;

    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
