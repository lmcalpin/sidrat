package com.metatrope.sidrat;

import java.util.function.IntSupplier;
import java.util.stream.IntStream;

import com.sidrat.BaseRecorderTest;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.tracking.CapturedLocalVariableValue;

import org.junit.Assert;
import org.junit.Test;

public class SidratLambdaTest extends BaseRecorderTest {
    public static class ForLambdaTest {
        public static void main(String[] args) {
            System.out.println("start");
            int x = IntStream.range(10, 15).map(i -> i + 1).sum();
            ForLambdaTest test = new ForLambdaTest();
            int y = test.run(() -> x * 10);
            System.out.println(x);
            System.out.println(y);
        }

        public int run(IntSupplier r) {
            return r.getAsInt();
        }
    }

    public SidratLambdaTest(EventRepositoryFactory factory) {
        super(factory);
    }

    // verify we do not have a regression where the beginning of the for loop generates two events
    @Test
    public void testLambda() {
        recorder.record(ForLambdaTest.class.getName());
        SidratExecutionEvent execEvent = replay.gotoEvent(3);
        String lambdaMethodName = execEvent.getMethodName();
        Assert.assertTrue(lambdaMethodName.contains("lambda"));
        int lambdaLineNumber = 18; // if the source code changes, adjust this line number
        Long foundLambdaLine = -1L;
        int countLambdaIterations = 0;
        int lastLine = -1;
        execEvent = replay.gotoEvent(1);
        while (execEvent != null) {
            int currentLineNumber = execEvent.getLineNumber();
            System.out.println(execEvent.getTime() + " @ LN: " + currentLineNumber + " : " + execEvent.getClassName() + "#" + execEvent.getMethodName());
            if (currentLineNumber == lambdaLineNumber && execEvent.getMethodName().equals(lambdaMethodName)) {
                if (foundLambdaLine == -1L)
                    foundLambdaLine = execEvent.getTime();
                countLambdaIterations++;
            }
            lastLine = execEvent.getLineNumber();
            execEvent = replay.readNext();
        }
        Assert.assertEquals(5, countLambdaIterations);
        Assert.assertEquals(lambdaLineNumber + 4, lastLine);
        Assert.assertTrue(replay.locals().keySet().contains("test"));
        Assert.assertTrue(replay.locals().keySet().contains("x"));
        Assert.assertTrue(replay.locals().keySet().contains("y"));
        Assert.assertEquals(3, replay.locals().size());
        CapturedLocalVariableValue x = replay.locals().get("x");
        Assert.assertEquals("65", x.getCurrentValue().getValueAsString());
        CapturedLocalVariableValue y = replay.locals().get("y");
        Assert.assertEquals("650", y.getCurrentValue().getValueAsString());
        // check history of x, go back in time to before first assignment
        replay.gotoEvent(foundLambdaLine);
        Assert.assertNull(replay.locals().get("x"));
        replay.gotoEvent(foundLambdaLine + 5);
        Assert.assertEquals("65", replay.locals().get("x").getCurrentValue().getValueAsString());
    }
}
