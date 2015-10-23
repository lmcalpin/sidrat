package com.sidrat.event.store.jpa.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * Stored whenever we execute some code.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
@Entity
public class Execution extends BaseSidratEntity {
    @ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
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
