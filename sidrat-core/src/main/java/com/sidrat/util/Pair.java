package com.sidrat.util;

import java.io.Serializable;

public class Pair<A, B> implements Serializable {
    private static final long serialVersionUID = -5395486044517921632L;

    A value1;
    B value2;

    public Pair(A a, B b) {
        this.value1 = a;
        this.value2 = b;
    }

    public Pair() {

    }

    public A getValue1() {
        return value1;
    }

    public void setValue1(A value1) {
        this.value1 = value1;
    }

    public B getValue2() {
        return value2;
    }

    public void setValue2(B value2) {
        this.value2 = value2;
    }

}
