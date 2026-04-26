package com.custify.exception;

public class EmailDejaUtiliseException extends RuntimeException {

    public EmailDejaUtiliseException(String email) {
        super("Un utilisateur avec l'email '" + email + "' existe deja.");
    }
}
