package com.mp.challenge.exceptions;

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
public class JsonStorageException extends Exception {

    /**
     * Constructs a new JsonStorageException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public JsonStorageException(String message) {
        super(message);
    }

    /**
     * Constructs a new JsonStorageException with the specified detail message and cause.
     *
     * @param message the detail message explaining the cause of the exception
     * @param cause   the underlying cause of this exception
     */
    public JsonStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

