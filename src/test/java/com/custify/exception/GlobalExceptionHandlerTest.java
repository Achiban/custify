package com.custify.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.custify.dto.CreerUtilisateurRequest;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationShouldReturnBadRequestWithFieldErrors() throws Exception {
        MethodArgumentNotValidException exception = newValidationException();

        var response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertEquals(400, body.get("statut"));
        assertEquals("Donnees invalides", body.get("message"));
        assertTrue(body.containsKey("horodatage"));
        assertTrue(((Map<?, ?>) body.get("details")).containsKey("email"));
    }

    @Test
    void handleEmailDejaUtiliseShouldReturnConflict() {
        EmailDejaUtiliseException exception = new EmailDejaUtiliseException("taken@mail.com");

        var response = handler.handleEmailDejaUtilise(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertEquals(409, body.get("statut"));
        assertEquals(exception.getMessage(), body.get("message"));
        assertTrue(body.containsKey("horodatage"));
    }

    private MethodArgumentNotValidException newValidationException() throws Exception {
        Method method = DummyEndpoint.class.getDeclaredMethod("create", CreerUtilisateurRequest.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new CreerUtilisateurRequest(), "requete");
        bindingResult.rejectValue("email", "NotBlank", "L'email est obligatoire");
        return new MethodArgumentNotValidException(parameter, bindingResult);
    }

    private static final class DummyEndpoint {
        @SuppressWarnings("unused")
        void create(CreerUtilisateurRequest request) {
        }
    }
}
