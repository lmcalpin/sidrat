package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.TrackedObject;

/**
 * Stored whenever we update a local variable.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
@Entity
@Table(indexes = { @Index(columnList = "partition,time,id") }) // tediously copied on all entities
public class LocalVariableUpdate extends SidratEvent {
    @ManyToOne(fetch = FetchType.EAGER)
    private EncounteredVariable localVariable;
    private String value;
    @ManyToOne(fetch = FetchType.EAGER)
    private EncounteredObject object;

    public CapturedLocalVariableValue asCapturedLocalVariableValue() {
        CapturedLocalVariableValue capturedValue = new CapturedLocalVariableValue(getId(), localVariable.asTrackedVariable(), asTrackedObject());
        return capturedValue;
    }

    public TrackedObject asTrackedObject() {
        return new TrackedObject(object.getClazz().getName(), value, object.getName());
    }

    public EncounteredVariable getLocalVariable() {
        return localVariable;
    }

    public EncounteredObject getObject() {
        return object;
    }

    public String getValue() {
        return value;
    }

    public void setLocalVariable(EncounteredVariable localVariable) {
        this.localVariable = localVariable;
    }

    public void setObject(EncounteredObject object) {
        this.object = object;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
