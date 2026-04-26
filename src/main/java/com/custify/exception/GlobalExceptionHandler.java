package com.custify.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> erreurs.put(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest().body(buildBody(HttpStatus.BAD_REQUEST, "Donnees invalides", erreurs));
    }

    @ExceptionHandler(EmailDejaUtiliseException.class)
    public ResponseEntity<Map<String, Object>> handleEmailDejaUtilise(EmailDejaUtiliseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildBody(HttpStatus.CONFLICT, ex.getMessage(), null));
    }

    private Map<String, Object> buildBody(HttpStatus status, String message, Object details) {
        Map<String, Object> body = new HashMap<>();
        body.put("horodatage", Instant.now().toString());
        body.put("statut", status.value());
        body.put("message", message);
        if (details != null) {
            body.put("details", details);
        }
        return body;
    }
}
