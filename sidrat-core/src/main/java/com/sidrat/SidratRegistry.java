package com.sidrat;

/**
 * Locates Sidrat stuff.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratRegistry {
    private static final SidratRegistry INSTANCE = new SidratRegistry();

    private SidratRecorder recorder;
    private SidratPermissions permissions;

    public static SidratRegistry instance() {
        return INSTANCE;
    }

    private SidratRegistry() {
        this.recorder = new SidratRecorder();
        this.permissions = new SidratPermissions();
    }

    /**
     * Create a new SidratRecorder for a new test execution
     * 
     * @return
     */
    public SidratRecorder newRecorder() {
        recorder = new SidratRecorder();
        return recorder;
    }

    /**
     * Find the existing SidratRecorder for the current test execution
     * 
     * @return
     */
    public SidratRecorder getRecorder() {
        return recorder;
    }

    /**
     * Find out whether we have permissions to instrument a class
     */
    public SidratPermissions getPermissions() {
        return permissions;
    }
}
