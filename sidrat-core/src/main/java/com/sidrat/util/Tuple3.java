package com.sidrat.util;

import java.io.Serializable;

public class Tuple3<A, B, C> implements Serializable {
    private static final long serialVersionUID = 1603790525081759609L;

    A value1;
    B value2;
    C value3;

    public Tuple3(A a, B b, C c) {
        this.value1 = a;
        this.value2 = b;
        this.value3 = c;
    }

    public Tuple3() {

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

    public C getValue3() {
        return value3;
    }

    public void setValue3(C value3) {
        this.value3 = value3;
    }

}
