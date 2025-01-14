package dev.mirodil.testing_system.exceptions;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {
//    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            handleException(response, HttpStatus.UNAUTHORIZED, "Invalid JWT Token");
        } catch (InvalidTokenException | UserAccountBlocked | ResourceNotFoundException e) {
            handleException(response, e.getStatusCode(), e.getMessage());
        }
    }

    private void handleException(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setContentType("application/json");
        response.sendError(status.value(), message);
//        response.setStatus(status.value());
//        response.getWriter().write(
//                "{\"status\": " + status.value() + ", " +
//                        "\"error\": \"" + status.getReasonPhrase() + "\", " +
//                        "\"message\": \"" + message + "\", " +
//                        "\"path\": \"/api/auth\"}"
//        );
    }
}
