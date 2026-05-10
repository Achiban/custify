package com.custify.exception;

public class OpportuniteNonTrouveException extends RuntimeException {
    public OpportuniteNonTrouveException(Long id) {
        super("L'opportunite avec l'identifiant " + id + " n'a pas ete trouvee.");
    }
}