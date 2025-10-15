package com.mp.challenge.components.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * JsonStorageException
 * <p>
 * Base exception for JSON file storage operations.
 * <p>
 * This exception is thrown when there are issues with JSON file storage operations
 * such as reading, writing, or serializing data to/from JSON files.
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 14/10/2025
 */
public class JsonStorageException extends BaseApplicationException {

    private static final String ERROR_CODE = "JSON_STORAGE_ERROR";
    private static final HttpStatus HTTP_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    /**
     * Constructs a new JsonStorageException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public JsonStorageException(String message) {
        super(ERROR_CODE, HTTP_STATUS, message, "A storage operation failed", Map.of());
    }

    /**
     * Constructs a new JsonStorageException with the specified detail message and cause.
     *
     * @param message the detail message explaining the cause of the exception
     * @param cause   the underlying cause of this exception
     */
    public JsonStorageException(String message, Throwable cause) {
        super(ERROR_CODE, HTTP_STATUS, message, "A storage operation failed", Map.of(), cause);
    }
}

