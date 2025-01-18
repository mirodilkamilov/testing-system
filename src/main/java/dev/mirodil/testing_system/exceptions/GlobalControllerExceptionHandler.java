package dev.mirodil.testing_system.exceptions;

import dev.mirodil.testing_system.responses.GenericErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.security.auth.login.AccountLockedException;
import java.util.Map;

@ControllerAdvice
public class GlobalControllerExceptionHandler {
    private final HttpServletRequest servletRequest;

    public GlobalControllerExceptionHandler(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResponseStatusException e) {
        return GenericErrorResponse.returnResponse(e.getMessage(), e.getStatusCode(), servletRequest);
    }

    @ExceptionHandler({InvalidTokenException.class, GenericValidationError.class})
    public ResponseEntity<Map<String, Object>> handleUserExceptions(ResponseStatusException e) {
        return GenericErrorResponse.returnResponse(e.getMessage(), e.getStatusCode(), servletRequest);
    }

    @ExceptionHandler({BadCredentialsException.class, AccountLockedException.class})
    public ResponseEntity<Map<String, Object>> handleUserExceptionsWithDefaultStatusCode(Exception e) {
        return GenericErrorResponse.returnResponse(e.getMessage(), HttpStatus.BAD_REQUEST, servletRequest);
    }
}
