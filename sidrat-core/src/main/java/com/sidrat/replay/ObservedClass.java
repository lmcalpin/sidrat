package com.sidrat.replay;

import java.util.ArrayList;
import java.util.List;

public class ObservedClass {
    private String name;
    private List<String> methods = new ArrayList<String>();
    
    public ObservedClass(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public void addMethod(String methodName) {
        methods.add(methodName);
    }
}
