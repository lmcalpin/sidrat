package com.metatrope.sidrat.testprogram;

public class ProgramToTest {
    public static void main(String[] args) {
        int i;
        int total = 0;
        SampleClassToTest lastIntWrapper = null;
        for (i = 0; i < 10; i++) {
            SampleClassToTest intWrapper = new SampleClassToTest(i);
            if (lastIntWrapper != null) {
                intWrapper.add(lastIntWrapper.get());
            }
            lastIntWrapper = intWrapper;
            total += intWrapper.get();
        }
        System.out.println(total);
    }
}
