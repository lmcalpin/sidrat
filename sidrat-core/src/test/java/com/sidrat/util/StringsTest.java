package com.sidrat.util;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class StringsTest {
    @Test
    public void testCaptures() {
        Pattern p = Pattern.compile("e(\\d)(\\d)e");
        String[] captures = Strings.captures("e35e", p);
        Assert.assertEquals("3", captures[1]);
        Assert.assertEquals("5", captures[2]);
    }
}
