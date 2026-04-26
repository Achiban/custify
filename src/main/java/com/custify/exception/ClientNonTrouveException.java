package com.custify.exception;

public class ClientNonTrouveException extends RuntimeException {

    public ClientNonTrouveException(Long id) {
        super("Le client avec l'identifiant '" + id + "' n'existe pas.");
    }
}
