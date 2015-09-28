package com.metatrope.sidrat.testprogram;

import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import org.junit.Ignore;

@Ignore("TODO")
public class ForThreadTest {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("hi");
        int i[] = new int[] { 0 };
        CountDownLatch cdl = new CountDownLatch(1);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int sum = IntStream.range(1, 10).parallel().sum();
                System.out.println(sum);
                i[0] += sum;
                cdl.countDown();
            }

        });
        t.start();
        cdl.await();
        System.out.println("counted: " + i[0]);
    }
}
