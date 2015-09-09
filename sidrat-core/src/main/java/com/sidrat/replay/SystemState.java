package com.sidrat.replay;

import java.util.HashMap;
import java.util.Map;

public class SystemState {
    private Map<String,String> threadNameCache = new HashMap<String,String>();
    private Map<String, ObservedClass> classes = new HashMap<String, ObservedClass>();
    
    private static final SystemState instance = new SystemState();
    
    public SystemState() {
        
    }
    
    public static SystemState current() {
        return instance;
    }
    
    public void addNewThread(String id, String name) {
        threadNameCache.put(id, name);
    }
    
    public String getThreadName(String id) {
        return threadNameCache.get(id);
    }
    
    public ObservedClass addNewClass(String className) {
        ObservedClass oc = new ObservedClass(className);
        classes.put(className, oc);
        return oc;
    }
    
    public void addMethod(ObservedClass oc, String methodName) {
        
    }
}
