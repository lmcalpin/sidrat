package com.sidrat.instrument;

import com.sidrat.util.Pair;

/**
 * A local variable found while instrumenting a method.
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class LocalVariable {
    final String name;
    final int startLine;
    final int endLine;

    public LocalVariable(String name, int startLine, int endLine) {
        this.startLine = startLine;
        this.endLine = endLine;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public Pair<Integer, Integer> getLineNumberRange() {
        return new Pair<>(startLine, endLine);
    }
    
    @Override
    public String toString() {
        return name + "@" + startLine + ":" + endLine;
    }
}
