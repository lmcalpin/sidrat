package com.sidrat.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.sidrat.SidratProcessingException;

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
//        Field field;
//        try {
//            field = obj.getClass().getDeclaredField("__sidratObjId");
//            if (field != null) {
//                Long ownerID = (Long) field.get(obj);
//                return ownerID;
//            }
//        } catch (SecurityException e) {
//            throw new SidratProcessingException("Could not locate __sidratObjId");
//        } catch (NoSuchFieldException e) {
//            throw new SidratProcessingException("Could not locate __sidratObjId");
//        } catch (IllegalArgumentException e) {
//            throw new SidratProcessingException("Could not locate __sidratObjId");
//        } catch (IllegalAccessException e) {
//            throw new SidratProcessingException("Could not locate __sidratObjId");
//        }
        Long id = objectIdentityMap.get(obj);
        if (id == null) {
            objectIdentityMap.put(obj, id = objectIdSeq.incrementAndGet());
        }
        return id;
    }
}
