package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.sidrat.event.store.jpa.Named;
import com.sidrat.event.tracking.TrackedVariable;
import com.sidrat.util.Pair;

/**
 * Stored whenever we encounter a local variable that we haven't seen before.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
@Entity
@Table(indexes = { @Index(columnList = "partition,name,id") }) // tediously copied on all value objects
public class EncounteredVariable extends SidratValueObject implements Named {
    private String variableName;
    @ManyToOne(fetch = FetchType.EAGER)
    private EncounteredMethod method;
    private Integer rangeStart;
    private Integer rangeEnd;
    private String clazz; // class of the variable

    public TrackedVariable asTrackedVariable() {
        TrackedVariable var = new TrackedVariable(clazz, method.getName(), variableName, new Pair<Integer, Integer>(rangeStart, rangeEnd));
        return var;
    }

    public String getClazz() {
        return clazz;
    }

    public EncounteredMethod getMethod() {
        return method;
    }

    public Integer getRangeEnd() {
        return rangeEnd;
    }

    public Integer getRangeStart() {
        return rangeStart;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public void setMethod(EncounteredMethod method) {
        this.method = method;
    }

    public void setRangeEnd(Integer rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public void setRangeStart(Integer rangeStart) {
        this.rangeStart = rangeStart;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
}
