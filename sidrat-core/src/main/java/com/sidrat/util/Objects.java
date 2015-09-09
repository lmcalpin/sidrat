package com.sidrat.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Objects {
    public static final <T> byte[] serialize(T obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        try {
            oos.writeObject(obj);
            byte[] returnMe = baos.toByteArray();
            return returnMe;
        } finally {
            baos.close();
        }
    }

    private static final Map<Object, Long> objectIdentityMap = new IdentityHashMap<Object, Long>();
    public static final AtomicLong objectIdSeq = new AtomicLong();

    public synchronized static final Long getUniqueIdentifier(Object obj) {
        Long id = objectIdentityMap.get(obj);
        if (id == null) {
            objectIdentityMap.put(obj, id = objectIdSeq.incrementAndGet());
        }
        return id;
    }
}
