package org.spice.rest;

public class RESTException extends RuntimeException{
    public RESTException(String message) {
        super(message);
    }

    public RESTException(String message, Throwable err) {
        super(message, err);
    }
}
