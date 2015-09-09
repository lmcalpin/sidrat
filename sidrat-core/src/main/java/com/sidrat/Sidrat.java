package com.sidrat;

import java.util.Arrays;

/**
 * Starts Sidrat from the command line.
 *  
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class Sidrat {
    public static final void main(String[] args) {
        String action = args[0];
        String target = args[1];

        if (action.equalsIgnoreCase("-debug")) {
            String[] rest = Arrays.copyOfRange(args, 2, args.length);
            SidratRecorder recorder = SidratRegistry.instance().getRecorder();
            recorder.store("sidrat").record(target, rest);
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
