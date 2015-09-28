package com.metatrope.sidrat.sample;

public class SampleMain {
    public int add(int n1, int n2) {
        int sum = n1 + n2;
        return sum;
    }

    public static void main(String[] args) {
        Foo foo = new Foo();
        int result = foo.add(1, 5);
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            sum += foo.add(i, 6);
        }
        System.out.println(sum);
    }
}
