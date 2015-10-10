package com.sidrat.event.tracking;

/**
 * A local variable found while instrumenting a method.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class LocalVariable {
    final String name;
    final int start;
    final int end;
    final int slot;

    public LocalVariable(int slot, String name, int start, int end) {
        this.slot = slot;
        this.start = start;
        this.end = end;
        this.name = name;
    }

    public int getEnd() {
        return end;
    }

    public String getName() {
        return name;
    }

    public int getSlot() {
        return slot;
    }

    public int getStart() {
        return start;
    }

    @Override
    public String toString() {
        return name + "@" + start + ":" + end + "#" + slot;
    }
}
