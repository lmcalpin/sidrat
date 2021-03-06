package com.sidrat.cli;

import java.util.Arrays;

import com.sidrat.SidratRecorder;
import com.sidrat.SidratRegistry;
import com.sidrat.event.store.hsqldb.HsqldbEventReader;
import com.sidrat.event.store.hsqldb.HsqldbEventStore;

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
            // target is the class name that has the main method that we will record
            recorder.store(new HsqldbEventStore("sidrat")).record(target, rest);
        } else if (action.equalsIgnoreCase("-replay")) {
            String source = null;
            if (args.length >= 3)
                source = args[2];
            // target is the name of the Sidrat recording
            SidratReplayCli replayer = new SidratReplayCli(new HsqldbEventReader(target));
            if (source != null)
                replayer.withSource(source);
            replayer.replay();
        }
    }
}
