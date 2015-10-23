package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;

/**
 * Stored whenever we encounter a local variable that we haven't seen before.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
@Entity
public class LocalVariable extends BaseSidratEntity {
    private String uniqueId;
    private String name;
    private Long methodId;
    private Integer rangeStart;
    private Integer rangeEnd;
    private String clazz; // class of the variable

    public String getClazz() {
        return clazz;
    }

    public Long getMethodId() {
        return methodId;
    }

    public String getName() {
        return name;
    }

    public Integer getRangeEnd() {
        return rangeEnd;
    }

    public Integer getRangeStart() {
        return rangeStart;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public void setMethodId(Long methodId) {
        this.methodId = methodId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRangeEnd(Integer rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public void setRangeStart(Integer rangeStart) {
        this.rangeStart = rangeStart;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
