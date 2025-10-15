package com.mp.challenge.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * BusinessLogicException
 * <p>
 * Exception thrown when business rules or constraints are violated.
 * <p>
 * This exception is typically thrown when:
 * <ul>
 *   <li>Business rules prevent an operation from completing</li>
 *   <li>Resource constraints are exceeded</li>
 *   <li>Concurrent modifications conflict</li>
 *   <li>State transitions are invalid</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 15/10/2025
 */
public class BusinessLogicException extends BaseApplicationException {

    private static final String ERROR_CODE = "BUSINESS_LOGIC_VIOLATION";
    private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;

    /**
     * Constructs a new BusinessLogicException with the specified message.
     *
     * @param message the business logic violation message
     */
    public BusinessLogicException(String message) {
        super(
            ERROR_CODE,
            HTTP_STATUS,
            message,
            "The operation cannot be completed due to business rules",
            Map.of()
        );
    }

    /**
     * Constructs a new BusinessLogicException with message and details.
     *
     * @param message the business logic violation message
     * @param details additional details about the violation
     */
    public BusinessLogicException(String message, Map<String, String> details) {
        super(
            ERROR_CODE,
            HTTP_STATUS,
            message,
            "The operation cannot be completed due to business rules",
            details
        );
    }

    /**
     * Constructs a new BusinessLogicException with message, details, and cause.
     *
     * @param message the business logic violation message
     * @param details additional details about the violation
     * @param cause the underlying cause
     */
    public BusinessLogicException(String message, Map<String, String> details, Throwable cause) {
        super(
            ERROR_CODE,
            HTTP_STATUS,
            message,
            "The operation cannot be completed due to business rules",
            details,
            cause
        );
    }

    /**
     * Constructs a new BusinessLogicException with message and cause.
     *
     * @param message the business logic violation message
     * @param cause the underlying cause
     */
    public BusinessLogicException(String message, Throwable cause) {
        super(
            ERROR_CODE,
            HTTP_STATUS,
            message,
            "The operation cannot be completed due to business rules",
            Map.of(),
            cause
        );
    }
}
