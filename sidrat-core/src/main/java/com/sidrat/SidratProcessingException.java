package com.sidrat;

public class SidratProcessingException extends RuntimeException {

    private static final long serialVersionUID = 2093347856166178844L;

    public SidratProcessingException() {
        super();
    }

    public SidratProcessingException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public SidratProcessingException(String arg0) {
        super(arg0);
    }

    public SidratProcessingException(Throwable arg0) {
        super(arg0);
    }

}
