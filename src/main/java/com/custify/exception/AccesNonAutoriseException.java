package com.custify.exception;

public class AccesNonAutoriseException extends RuntimeException {

    public AccesNonAutoriseException() {
        super("Vous n'etes pas autorise a effectuer cette action sur cette fiche client.");
    }
}
