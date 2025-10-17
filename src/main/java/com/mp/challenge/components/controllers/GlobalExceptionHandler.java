package com.mp.challenge.components.controllers;

import com.mp.challenge.components.exceptions.BaseApplicationException;
import com.mp.challenge.components.exceptions.BusinessLogicException;
import com.mp.challenge.components.exceptions.ItemNotFoundException;
import com.mp.challenge.components.exceptions.JsonStorageException;
import com.mp.challenge.components.exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
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
     *
     * @param ex the application exception
     * @return error response with appropriate HTTP status
     */
    @ExceptionHandler(BaseApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(BaseApplicationException ex) {
        log.error("Application exception occurred: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getHttpStatus().value())
                .error(ex.getHttpStatus().getReasonPhrase())
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .details(convertToStringObjectMap(ex.getDetails()))
                .path(getCurrentPath())
                .build();

        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    /**
     * Handles validation exceptions specifically.
     *
     * @param ex the validation exception
     * @return error response with 400 status
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        log.warn("Validation error occurred: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .details(convertToStringObjectMap(ex.getDetails()))
                .path(getCurrentPath())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles item not found exceptions.
     *
     * @param ex the item not found exception
     * @return error response with 404 status
     */
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleItemNotFoundException(ItemNotFoundException ex) {
        log.warn("Item not found: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .details(convertToStringObjectMap(ex.getDetails()))
                .path(getCurrentPath())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles business logic exceptions.
     *
     * @param ex the business logic exception
     * @return error response with 409 status
     */
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLogicException(BusinessLogicException ex) {
        log.warn("Business logic violation: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .details(convertToStringObjectMap(ex.getDetails()))
                .path(getCurrentPath())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles JSON storage exceptions.
     *
     * @param ex the JSON storage exception
     * @return error response with 500 status
     */
    @ExceptionHandler(JsonStorageException.class)
    public ResponseEntity<ErrorResponse> handleJsonStorageException(JsonStorageException ex) {
        log.error("JSON storage error occurred: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .details(convertToStringObjectMap(ex.getDetails()))
                .path(getCurrentPath())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
        
        Map<String, Object> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            validationErrors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .errorCode("VALIDATION_FAILED")
                .details(validationErrors)
                .path(getCurrentPath())
                .build();

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

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .errorCode("TYPE_MISMATCH")
                .path(getCurrentPath())
                .build();

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
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred")
                .errorCode("INTERNAL_ERROR")
                .path(getCurrentPath())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Gets the current request path for error reporting.
     *
     * @return the current path or "unknown" if not available
     */
    private String getCurrentPath() {
        // In a real implementation, you would get this from the request context
        // For now, we'll return a placeholder
        return "unknown";
    }

    /**
     * Converts a Map<String, String> to Map<String, Object>.
     *
     * @param stringMap the string map to convert
     * @return the converted object map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToStringObjectMap(Map<String, String> stringMap) {
        if (stringMap == null) {
            return null;
        }
        return (Map<String, Object>) (Map<?, ?>) stringMap;
    }

    /**
     * ErrorResponse
     * <p>
     * Standard error response format for all API errors.
     */
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String errorCode;
        private Map<String, Object> details;
        private String path;

        // Builder pattern
        public static ErrorResponseBuilder builder() {
            return new ErrorResponseBuilder();
        }

        public static class ErrorResponseBuilder {
            private LocalDateTime timestamp;
            private int status;
            private String error;
            private String message;
            private String errorCode;
            private Map<String, Object> details;
            private String path;

            public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public ErrorResponseBuilder status(int status) {
                this.status = status;
                return this;
            }

            public ErrorResponseBuilder error(String error) {
                this.error = error;
                return this;
            }

            public ErrorResponseBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ErrorResponseBuilder errorCode(String errorCode) {
                this.errorCode = errorCode;
                return this;
            }

            public ErrorResponseBuilder details(Map<String, Object> details) {
                this.details = details;
                return this;
            }

            public ErrorResponseBuilder path(String path) {
                this.path = path;
                return this;
            }

            public ErrorResponse build() {
                ErrorResponse response = new ErrorResponse();
                response.timestamp = this.timestamp;
                response.status = this.status;
                response.error = this.error;
                response.message = this.message;
                response.errorCode = this.errorCode;
                response.details = this.details;
                response.path = this.path;
                return response;
            }
        }

        // Getters
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getErrorCode() { return errorCode; }
        public Map<String, Object> getDetails() { return details; }
        public String getPath() { return path; }
    }
}
