package com.sidrat.event.store.jpa.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseSidratEntity {
    @Id
    @Column(nullable = false, unique = true)
    private Long id;

    private String partition;

    public Long getId() {
        return id;
    }

    public String getPartition() {
        return partition;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

}
