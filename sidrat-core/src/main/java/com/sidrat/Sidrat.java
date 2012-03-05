package com.sidrat;

import java.util.Arrays;

public class Sidrat {
    public static final void main(String[] args) {
        String action = args[0];
        String target = args[1];

        if (action.equalsIgnoreCase("-debug")) {
            String[] rest = Arrays.copyOfRange(args, 2, args.length);
            SidratDebugger debugger = new SidratDebugger();
            debugger.debug(target, rest);
        } else if (action.equalsIgnoreCase("-replay")) {
            String source = null;
            if (args.length >= 3)
                source = args[2];
            SidratReplay replayer = new SidratReplay(target);
            if (source != null)
                replayer.withSource(source);
            replayer.replay();
        }
    }
}
