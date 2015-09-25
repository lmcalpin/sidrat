package com.sidrat;

import java.util.function.IntSupplier;
import java.util.stream.IntStream;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.tracking.CapturedLocalVariableValue;

import org.junit.Assert;
import org.junit.Test;

public class SidratLambdaTest extends BaseRecorderTest {
    public static class ForLambdaTest {
        public int run(IntSupplier r) {
            return r.getAsInt();
        }

        public static void main(String[] args) {
            System.out.println("start");
            int x = IntStream.range(10, 15).map(i -> i + 1).sum();
            ForLambdaTest test = new ForLambdaTest();
            int y = test.run(() -> x * 10);
            System.out.println(x);
            System.out.println(y);
        }
    }
    
    public SidratLambdaTest(EventRepositoryFactory factory) {
        super(factory);
    }

    // verify we do not have a regression where the beginning of the for loop generates two events
    @Test
    public void testLambda() {
        recorder.record(ForLambdaTest.class.getName());
        SidratExecutionEvent execEvent = replay.gotoEvent(1);
        int countLine21s = 0;
        Long first21 = -1L;
        int lastLine = -1;
        while (execEvent != null) {
            int currentLineNumber = execEvent.getLineNumber();
            System.out.println(execEvent.getTime() + " @ LN: " + currentLineNumber + " : " + execEvent.getClassName() + "#" + execEvent.getMethodName());
            if (currentLineNumber == 21 && execEvent.getMethodName().equals("lambda$0")) {
                if (first21 == -1L)
                    first21 = execEvent.getTime();
                countLine21s++;
            }
            lastLine = execEvent.getLineNumber();
            execEvent = replay.readNext();
        }
        Assert.assertEquals(5, countLine21s);
        Assert.assertEquals(25, lastLine);
        Assert.assertTrue(replay.locals().keySet().contains("test"));
        Assert.assertTrue(replay.locals().keySet().contains("x"));
        Assert.assertTrue(replay.locals().keySet().contains("y"));
        Assert.assertEquals(3, replay.locals().size());
        CapturedLocalVariableValue x = replay.locals().get("x");
        Assert.assertEquals("65", x.getCurrentValue().getValueAsString());
        CapturedLocalVariableValue y = replay.locals().get("y");
        Assert.assertEquals("650", y.getCurrentValue().getValueAsString());
        // check history of x, go back in time to before first assignment
        replay.gotoEvent(first21);
        Assert.assertNull(replay.locals().get("x"));
        replay.gotoEvent(first21+4);
        Assert.assertEquals("65", replay.locals().get("x").getCurrentValue().getValueAsString());
    }
}
