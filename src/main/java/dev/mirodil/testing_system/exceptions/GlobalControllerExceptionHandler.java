package dev.mirodil.testing_system.exceptions;

import dev.mirodil.testing_system.responses.GenericErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResponseStatusException e, HttpServletRequest request) {
        return GenericErrorResponse.returnResponse(e.getMessage(), e.getStatusCode(), request);
    }

    @ExceptionHandler({InvalidTokenException.class, GenericValidationError.class})
    public ResponseEntity<Map<String, Object>> handleUserExceptions(ResponseStatusException e, HttpServletRequest request) {
        return GenericErrorResponse.returnResponse(e.getMessage(), e.getStatusCode(), request);
    }
}
