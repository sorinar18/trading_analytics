package com.sparta.trading_analytics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors for @Valid single-object payloads.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        return buildErrorResponse(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles validation errors for List<@Valid ...> payloads.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                errors.put("error", error.getDefaultMessage());
            }
        });
        return buildErrorResponse(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles malformed JSON / deserialization errors.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Malformed JSON or deserialization issue: " + ex.getMostSpecificCause().getMessage());
        return buildErrorResponse(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles manually thrown IllegalArgumentException (like duplicate detection).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        return buildErrorResponse(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Fallback for unexpected exceptions (500 Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllOtherExceptions(Exception ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Unexpected error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
        return buildErrorResponse(errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Helper to build a standardized error response.
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(Map<String, String> errors, HttpStatus status) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", false);
        responseBody.put("errors", errors);
        return ResponseEntity.status(status).body(responseBody);
    }
}
