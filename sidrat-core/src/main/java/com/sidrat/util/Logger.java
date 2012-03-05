package com.sidrat.util;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class Logger {
    private java.util.logging.Logger logger;

    public Logger() {
        logger = java.util.logging.Logger.getAnonymousLogger();
    }

    public void severe(String str) {
        log(Level.SEVERE, str);
    }

    public void severe(Throwable t) {
        log(Level.SEVERE, "Unhandled exception", t);
    }

    public void severe(String str, Throwable t) {
        log(Level.SEVERE, str, t);
    }

    public void warning(String str) {
        log(Level.WARNING, str);
    }

    public void warning(String str, Throwable t) {
        log(Level.WARNING, str, t);
    }

    public void info(String str) {
        log(Level.INFO, str);
    }

    public void info(String str, Throwable t) {
        log(Level.INFO, str, t);
    }

    public void fine(String str) {
        log(Level.FINE, str);
    }

    public void finer(String str) {
        log(Level.FINER, str);
    }

    public void finest(String str) {
        log(Level.FINEST, str);
    }

    public void debug(String str) {
        log(Level.FINEST, str);
    }

    public boolean isDebugEnabled() {
        return isLoggable(Level.FINEST);
    }

    public boolean isWarnEnabled() {
        return isLoggable(Level.WARNING);
    }

    public boolean isLoggable(Level level) {
        return logger.isLoggable(level);
    }

    private void log(Level level, String str) {
        log(level, str, null);
    }

    private void log(Level level, String str, Throwable th) {
        if (logger.isLoggable(level)) {
            Throwable t = new Throwable();
            StackTraceElement[] elements = t.getStackTrace();
            String className = null;
            int idx = 0;
            for (int i = 2; i < elements.length; i++) {
                className = elements[i].getClassName();
                idx = i;
                if (!className.equals(Logger.class.getName()))
                    break;
            }
            String methodName = elements[idx].getMethodName() + ":" + elements[2].getLineNumber();
            LogRecord record = new LogRecord(level, str);
            if (th != null) {
                record.setThrown(th);
            }
            record.setSourceClassName(className);
            record.setSourceMethodName(methodName);
            logger.log(record);
        }
    }
}
