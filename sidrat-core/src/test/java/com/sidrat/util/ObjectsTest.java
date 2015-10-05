package com.sidrat.util;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ObjectsTest {
    private class Foo {
    }

    Set<Long> ids;

    @Before
    public void setUp() {
        ids = new HashSet<>();
    }

    @Test
    public void testUniqueIdForObject() {
        Foo foo = new Foo();
        Long uniqueId = Objects.getUniqueIdentifier(foo);
        Long uniqueId2 = Objects.getUniqueIdentifier(foo);
        Assert.assertEquals(uniqueId, uniqueId2);
        for (int i = 0; i < 100; i++) {
            seenUnique(Objects.getUniqueIdentifier(new Foo()));
        }
    }

    @Test
    public void testUniqueIdForNumber() {
        Integer one = new Integer(1);
        Long uniqueId = Objects.getUniqueIdentifier(one);
        Long uniqueId2 = Objects.getUniqueIdentifier(one);
        seenUnique(uniqueId);
        Long uniqueId3 = Objects.getUniqueIdentifier(1); // boxed int is different
        seenUnique(uniqueId3);
        Assert.assertEquals(uniqueId, uniqueId2);
        for (int i = 0; i < 100; i++) {
            seenUnique(Objects.getUniqueIdentifier(new Integer(i)));
        }
    }

    private void seenUnique(Long id) {
        if (ids.contains(id)) {
            Assert.fail("Saw duplicate id: " + id);
        }
        ids.add(id);
    }
}
