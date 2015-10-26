package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Stored whenever we execute some code.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
@Entity
@Table(indexes = { @Index(columnList = "partition,time,id") }) // tediously copied on all entities
public class Execution extends SidratEvent {
    @ManyToOne(fetch = FetchType.EAGER)
    private MethodEntry methodEntry;
    private Integer lineNumber;

    public Integer getLineNumber() {
        return lineNumber;
    }

    public MethodEntry getMethodEntry() {
        return methodEntry;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setMethodEntry(MethodEntry methodEntry) {
        this.methodEntry = methodEntry;
    }
}
