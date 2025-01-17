package dev.mirodil.testing_system.responses;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GenericErrorResponse {
    /**
     * Creates and returns a {@link ResponseEntity} containing a JSON-formatted error message.
     *
     * @param message    value for the json object
     * @param statusCode http status code
     * @param path       request path of which error occurred
     * @return ResponseEntity - same structure as default error response in {@link BasicErrorController}
     */
    public static ResponseEntity<Map<String, Object>> returnResponse(String message, HttpStatus statusCode, URI path) {
        if (statusCode == HttpStatus.NO_CONTENT) {
            return new ResponseEntity<>(statusCode);
        }

        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("timestamp", new Date());
        jsonResponse.put("status", statusCode.value());
        jsonResponse.put("error", statusCode.getReasonPhrase());
        jsonResponse.put("message", message);
        jsonResponse.put("path", path);
        return new ResponseEntity<>(jsonResponse, statusCode);
    }

    /**
     * Creates and returns a {@link ResponseEntity} containing a JSON-formatted error message.
     *
     * @param message    value for the json object
     * @param statusCode http status code
     * @param request    HttpServletRequest to set "error" key in response
     * @return ResponseEntity - same structure as default error response in {@link BasicErrorController}
     */
    public static ResponseEntity<Map<String, Object>> returnResponse(String message, HttpStatus statusCode, HttpServletRequest request) {
        URI path = URI.create(request.getRequestURL().toString());
        return returnResponse(message, statusCode, path);
    }
}
