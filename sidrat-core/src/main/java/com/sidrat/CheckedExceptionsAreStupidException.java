package com.sidrat;

/**
 * Checked exceptions are stupid.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class CheckedExceptionsAreStupidException extends RuntimeException {
    public interface ThingThatThrowsExceptions<T> {
        void apply() throws Exception;
    }

    public interface ThingThatThrowsExceptionsAndReturnsStuff<T> {
        T apply() throws Exception;
    }

    private static final long serialVersionUID = 1L;

    public CheckedExceptionsAreStupidException() {
        super();
    }

    public CheckedExceptionsAreStupidException(String message) {
        super(message);
    }

    public CheckedExceptionsAreStupidException(String message, Throwable cause) {
        super(message, cause);
    }

    public CheckedExceptionsAreStupidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CheckedExceptionsAreStupidException(Throwable cause) {
        super(cause);
    }

    public static <T> void unchecked(ThingThatThrowsExceptions<T> supplier) {
        try {
            supplier.apply();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CheckedExceptionsAreStupidException(e);
        }
    }

    public static <T> T unchecked(ThingThatThrowsExceptionsAndReturnsStuff<T> supplier) {
        try {
            return supplier.apply();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CheckedExceptionsAreStupidException(e);
        }
    }
}
