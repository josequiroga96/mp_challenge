package com.mp.challenge.components.exceptions;

/**
 * JsonReadException
 * <p>
 * Exception thrown when there are issues reading JSON data from a file.
 * <p>
 * This exception is typically thrown when:
 * <ul>
 *     <li>The JSON file format is invalid</li>
 *     <li>The file cannot be accessed or read</li>
 *     <li>Deserialization fails due to type mismatch</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 14/10/2025
 */
public class JsonReadException extends JsonStorageException {

    /**
     * Constructs a new JsonReadException with the specified detail message.
     *
     * @param message the detail message explaining why the read operation failed
     */
    public JsonReadException(String message) {
        super(message);
    }

    /**
     * Constructs a new JsonReadException with the specified detail message and cause.
     *
     * @param message the detail message explaining why the read operation failed
     * @param cause   the underlying cause of this exception
     */
    public JsonReadException(String message, Throwable cause) {
        super(message, cause);
    }
}

