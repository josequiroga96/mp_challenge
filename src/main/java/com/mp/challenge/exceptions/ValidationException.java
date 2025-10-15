package com.mp.challenge.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * ValidationException
 * <p>
 * Exception thrown when input validation fails.
 * <p>
 * This exception is typically thrown when:
 * <ul>
 *   <li>Required fields are missing or null</li>
 *   <li>Field values are outside valid ranges</li>
 *   <li>Data format is invalid</li>
 *   <li>Business rules are violated</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 15/10/2025
 */
public class ValidationException extends BaseApplicationException {

    private static final String ERROR_CODE = "VALIDATION_FAILED";
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;

    /**
     * Constructs a new ValidationException with a single validation error.
     *
     * @param field the field that failed validation
     * @param message the validation error message
     */
    public ValidationException(String field, String message) {
        super(
            ERROR_CODE,
            HTTP_STATUS,
            String.format("Validation failed for field '%s': %s", field, message),
            "The provided data is invalid",
            Map.of(field, message)
        );
    }

    /**
     * Constructs a new ValidationException with multiple validation errors.
     *
     * @param errors map of field names to error messages
     */
    public ValidationException(Map<String, String> errors) {
        super(
            ERROR_CODE,
            HTTP_STATUS,
            String.format("Validation failed for %d field(s)", errors.size()),
            "The provided data contains validation errors",
            errors
        );
    }

    /**
     * Constructs a new ValidationException with a general message.
     *
     * @param message the validation error message
     */
    public ValidationException(String message) {
        super(
            ERROR_CODE,
            HTTP_STATUS,
            message,
            "The provided data is invalid",
            Map.of("message", message)
        );
    }
}
