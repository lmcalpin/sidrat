package com.sidrat.junit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation for JUnit tests that indicates where we should store our recordings.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SidratEventStore {
    Class<?> factory();
    String name();
}
