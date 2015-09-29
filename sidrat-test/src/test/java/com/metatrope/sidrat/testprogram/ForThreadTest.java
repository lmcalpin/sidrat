package com.metatrope.sidrat.testprogram;

import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import org.junit.Ignore;

public class ForThreadTest {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("hi");
        int i[] = new int[] { 0 };
        CountDownLatch cdl = new CountDownLatch(2);
        Runnable r = () -> {
            int sum = IntStream.range(1, 10).parallel().sum();
            System.out.println(Thread.currentThread().getName() + ": " + sum);
            i[0] += sum;
            cdl.countDown();
        };
        new Thread(r).start();
        new Thread(r).start();
        cdl.await();
        int sum = 5;
        i[0] += sum;
        System.out.println("counted: " + i[0]);
    }
}
