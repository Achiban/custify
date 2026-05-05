package com.custify.exception;

public class ProspectNonTrouveException extends RuntimeException {

    public ProspectNonTrouveException(Long id) {
        super("Le prospect avec l'identifiant '" + id + "' n'existe pas.");
    }
}
