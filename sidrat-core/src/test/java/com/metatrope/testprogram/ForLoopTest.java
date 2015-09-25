package com.metatrope.testprogram;

public class ForLoopTest {
    public static void main(String[] args) {
        System.out.println("hi");
        for (int i = 0; i < 10; i++) {
            System.out.println(i);
        }
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            sum += i + 1;
        }
        System.out.println("sum: " + sum);
    }
}
