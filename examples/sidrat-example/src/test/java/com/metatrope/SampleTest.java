package com.metatrope;

import com.metatrope.sidrat.sample.Foo;

import com.sidrat.junit.SidratEventStore;
import com.sidrat.junit.SidratTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SidratTestRunner.class)
@SidratEventStore(factory=com.sidrat.event.store.mem.InMemoryEventRepositoryFactory.class, name="sidrat-testrepo-hello")
public class SampleTest {
    @Test
    public void testFailure() {
        Foo foo = new Foo();
        int result = foo.add(1, 5);
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            sum += foo.add(i, 6);
        }
        Assert.assertEquals(6000, sum);
        Assert.assertEquals(6000, result);
    }
}
