package com.sidrat.util;

import java.util.concurrent.atomic.AtomicLong;

public class Time {
    private static AtomicLong time = new AtomicLong(0);

    public static long next() {
        return time.incrementAndGet();
    }
}
