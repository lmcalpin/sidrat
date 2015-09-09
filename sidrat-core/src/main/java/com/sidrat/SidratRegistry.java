package com.sidrat;

/**
 * Locates Sidrat stuff.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratRegistry {
    private static final SidratRegistry INSTANCE = new SidratRegistry();
    
    private SidratRecorder recorder;

    public static SidratRegistry instance() {
        return INSTANCE;
    }
    
    private SidratRegistry() {
        this.recorder = new SidratRecorder();
    }

    /**
     * Create a new SidratRecorder for a new test execution
     * @return
     */
    public SidratRecorder newRecorder() {
        recorder = new SidratRecorder();
        return recorder;
    }

    /**
     * Find the existing SidratRecorder for the current test execution
     * @return
     */
    public SidratRecorder getRecorder() {
        return recorder;
    }
}
