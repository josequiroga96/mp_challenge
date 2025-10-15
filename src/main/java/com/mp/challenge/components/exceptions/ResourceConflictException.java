package com.mp.challenge.components.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * ResourceConflictException
 * <p>
 * Exception thrown when there are conflicts with resource operations.
 * <p>
 * This exception is typically thrown when:
 * <ul>
 *   <li>Attempting to create a resource that already exists</li>
 *   <li>Concurrent access conflicts</li>
 *   <li>Resource state conflicts</li>
 *   <li>Optimistic locking failures</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 15/10/2025
 */
public class ResourceConflictException extends BaseApplicationException {

    private static final String ERROR_CODE = "RESOURCE_CONFLICT";
    private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;

    /**
     * Constructs a new ResourceConflictException with the specified message.
     *
     * @param message the conflict message
     */
    public ResourceConflictException(String message) {
        super(
            ERROR_CODE,
            HTTP_STATUS,
            message,
            "The operation conflicts with the current resource state",
            Map.of()
        );
    }

    /**
     * Constructs a new ResourceConflictException with message and details.
     *
     * @param message the conflict message
     * @param details additional details about the conflict
     */
    public ResourceConflictException(String message, Map<String, String> details) {
        super(
            ERROR_CODE,
            HTTP_STATUS,
            message,
            "The operation conflicts with the current resource state",
            details
        );
    }
}
