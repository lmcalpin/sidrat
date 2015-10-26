package com.sidrat.event.store.jpa.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class SidratEvent extends BaseSidratEntity {
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue
    private Long id;

    private Long time;

    public Long getId() {
        return id;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

}
