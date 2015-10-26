package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.sidrat.event.tracking.CapturedFieldValue;
import com.sidrat.event.tracking.TrackedObject;

/**
 * Stored whenever we update a local variable.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
@Entity
@Table(indexes = { @Index(columnList = "partition,time,id") }) // tediously copied on all entities
public class FieldUpdate extends SidratEvent {
    @ManyToOne(fetch = FetchType.EAGER)
    private EncounteredField field;
    private String value;
    @ManyToOne(fetch = FetchType.EAGER)
    private EncounteredObject ref;

    public CapturedFieldValue asCapturedFieldValue() {
        CapturedFieldValue capturedValue = new CapturedFieldValue(getId(), field.getObject().getName(), asTrackedObject());
        return capturedValue;
    }

    public TrackedObject asTrackedObject() {
        return new TrackedObject(ref.getClazz().getName(), value, ref.getName());
    }

    public EncounteredField getField() {
        return field;
    }

    public EncounteredObject getRef() {
        return ref;
    }

    public String getValue() {
        return value;
    }

    public void setField(EncounteredField field) {
        this.field = field;
    }

    public void setRef(EncounteredObject ref) {
        this.ref = ref;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
