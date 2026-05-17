package com.custify.exception;

public class AccesNonAutoriseException extends RuntimeException {

    public AccesNonAutoriseException() {
        super("Vous n'etes pas autorise a effectuer cette action.");
    }

    public AccesNonAutoriseException(String message) {
        super(message);
    }
}
