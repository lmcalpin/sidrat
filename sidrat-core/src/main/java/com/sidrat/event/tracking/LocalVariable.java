package com.sidrat.event.tracking;

/**
 * A local variable found while instrumenting a method.
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class LocalVariable {
    final String name;
    final int start;
    final int end;

    public LocalVariable(String name, int start, int end) {
        this.start = start;
        this.end = end;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public int getStart() {
        return start;
    }
    
    public int getEnd() {
        return end;
    }
    
    @Override
    public String toString() {
        return name + "@" + start + ":" + end;
    }
}
