package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Stored whenever we update a local variable.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
@Entity
public class LocalVariableUpdate extends BaseSidratEntity {
    @ManyToOne
    private LocalVariable localVariable;
    private String value;
    private Long objectId;

    public LocalVariable getLocalVariable() {
        return localVariable;
    }

    public Long getObjectId() {
        return objectId;
    }

    public String getValue() {
        return value;
    }

    public void setLocalVariable(LocalVariable localVariable) {
        this.localVariable = localVariable;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
