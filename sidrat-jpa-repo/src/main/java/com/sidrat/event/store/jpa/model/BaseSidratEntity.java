package com.sidrat.event.store.jpa.model;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseSidratEntity {
    private String partition;

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }
}
