package com.metatrope.sidrat.testprogram;

public class ForFieldTrackingTest {
    public static class ClassWithFields {
        public int a;
        public String b;
        public void update(int val) {
            this.a = val;
            this.b = "The value of a is: " + String.valueOf(a);
        }
    }
    public static void main(String[] args) {
        ClassWithFields theClass = new ForFieldTrackingTest.ClassWithFields();
        theClass.a = 5;
        theClass.b = "five";
        theClass.update(6);
        theClass.update(7);
    }
}
