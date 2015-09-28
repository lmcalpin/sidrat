package com.sidrat.instrument;

import com.sidrat.SidratProcessingException;

/**
 * Thrown when we encounter errors trying to instrument a class.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class ClassInstrumentationException extends SidratProcessingException {
    private static final long serialVersionUID = 6312828793021164153L;

    public ClassInstrumentationException() {
        super();
    }

    public ClassInstrumentationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public ClassInstrumentationException(String arg0) {
        super(arg0);
    }

    public ClassInstrumentationException(Throwable arg0) {
        super(arg0);
    }

}
