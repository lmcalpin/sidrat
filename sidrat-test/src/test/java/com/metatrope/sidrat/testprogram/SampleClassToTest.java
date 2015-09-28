package com.metatrope.sidrat.testprogram;

public class SampleClassToTest {
    private int val;
    public SampleClassToTest(int val) {
        this.val = val;
    }
    
    public void add(int v) {
        this.val += v;
    }
    
    public int get() {
        return val;
    }
}
