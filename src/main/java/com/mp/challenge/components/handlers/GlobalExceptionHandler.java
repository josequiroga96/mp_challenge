package com.mp.challenge.components.handlers;

import com.mp.challenge.components.dtos.ErrorResponse;
import com.mp.challenge.components.exceptions.BaseApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler
 * <p>
 * Global exception handler for the application. This handler catches and processes
 * all exceptions thrown by controllers, providing consistent error responses
 * and proper HTTP status codes.
 * <p>
 * Features:
 * <ul>
 *   <li>Handles all custom application exceptions</li>
 *   <li>Provides consistent error response format</li>
 *   <li>Maps exceptions to appropriate HTTP status codes</li>
 *   <li>Logs all exceptions for debugging</li>
 *   <li>Handles validation errors from Jakarta Bean Validation</li>
 *   <li>Handles type mismatch errors</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 16/10/2025
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all custom application exceptions.
     * Uses the built-in toErrorResponse() method from BaseApplicationException.
     *
     * @param ex the application exception
     * @return error response with appropriate HTTP status
     */
    @ExceptionHandler(BaseApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(BaseApplicationException ex) {
        log.error("Application exception occurred: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);

        return ResponseEntity.status(ex.getHttpStatus()).body(ex.toErrorResponse());
    }

    /**
     * Handles Jakarta Bean Validation errors.
     *
     * @param ex the validation exception
     * @return error response with 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation errors occurred: {}", ex.getMessage());
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            validationErrors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_FAILED",
                "Validation failed",
                "One or more validation constraints were violated",
                validationErrors,
                System.currentTimeMillis()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles type mismatch errors (e.g., invalid UUID format).
     *
     * @param ex the type mismatch exception
     * @return error response with 400 status
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch error: {} - {}", ex.getName(), ex.getMessage());
        
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());

        ErrorResponse errorResponse = new ErrorResponse(
                "TYPE_MISMATCH",
                "Invalid parameter type",
                message,
                null,
                System.currentTimeMillis()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles JSON parsing errors.
     *
     * @param ex the JSON parsing exception
     * @return error response with 400 status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseError(HttpMessageNotReadableException ex) {
        log.warn("JSON parsing error: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_JSON",
                "Invalid JSON format",
                "The request body contains invalid JSON",
                null,
                System.currentTimeMillis()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles all other unexpected exceptions.
     *
     * @param ex the unexpected exception
     * @return error response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                ex.getMessage(),
                null,
                System.currentTimeMillis()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
