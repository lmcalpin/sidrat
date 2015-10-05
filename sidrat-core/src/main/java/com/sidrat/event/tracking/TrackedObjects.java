package com.sidrat.event.tracking;

import com.sidrat.util.Objects;

/**
 * Assign a unique identifier to objects that we see.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class TrackedObjects {
    private Long getUniqueIDFor(Object obj) {
        Long id = Objects.getUniqueIdentifier(obj);
        return id;
    }

    public TrackedObject found(Object obj) {
        if (obj == null)
            return null;
        Long id = getUniqueIDFor(obj);
        return new TrackedObject(obj, id);
    }
}
