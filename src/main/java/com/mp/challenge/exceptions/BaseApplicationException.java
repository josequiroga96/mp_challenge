package com.mp.challenge.exceptions;

import org.springframework.http.HttpStatus;

import com.mp.challenge.dtos.ErrorResponse;

import lombok.Getter;
import java.util.Map;

/**
 * BaseApplicationException
 * <p>
 * Base exception class for all application-specific exceptions.
 * Provides a standardized way to handle errors with error codes,
 * HTTP status mapping, and consistent error reporting.
 * <p>
 * This exception includes:
 * <ul>
 *   <li>Error code for programmatic error identification</li>
 *   <li>HTTP status mapping for REST API responses</li>
 *   <li>User-friendly error messages</li>
 *   <li>Support for error details and context</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 15/10/2025
 */
@Getter
public abstract class BaseApplicationException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;
    private final String userMessage;
    private final Map<String, String> details;

    /**
     * Constructs a new BaseApplicationException with the specified parameters.
     *
     * @param errorCode   the unique error code for this exception type
     * @param httpStatus  the HTTP status code to return
     * @param message     the technical error message
     * @param userMessage the user-friendly error message
     * @param details     additional error details (optional)
     */
    protected BaseApplicationException(String errorCode, HttpStatus httpStatus, String message, String userMessage, Map<String, String> details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
        this.details = details;
    }

    /**
     * Constructs a new BaseApplicationException with the specified parameters and cause.
     *
     * @param errorCode   the unique error code for this exception type
     * @param httpStatus  the HTTP status code to return
     * @param message     the technical error message
     * @param userMessage the user-friendly error message
     * @param details     additional error details (optional)
     * @param cause       the underlying cause of this exception
     */
    protected BaseApplicationException(String errorCode, HttpStatus httpStatus, String message, String userMessage, Map<String, String> details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
        this.details = details;
    }

    /**
     * Creates a simple error response object.
     *
     * @return error response object
     */
    public ErrorResponse toErrorResponse() {
        return new ErrorResponse(
                errorCode,
                userMessage,
                getMessage(),
                details,
                System.currentTimeMillis()
        );
    }
}
