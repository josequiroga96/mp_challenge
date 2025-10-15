package com.mp.challenge.exceptions;

/**
 * JsonWriteException
 * <p>
 * Exception thrown when there are issues writing JSON data to a file.
 * <p>
 * This exception is typically thrown when:
 * <ul>
 *     <li>The file system is full or read-only</li>
 *     <li>There are permission issues with the target file</li>
 *     <li>Serialization fails due to object structure issues</li>
 *     <li>Atomic file operations fail</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 14/10/2025
 */
public class JsonWriteException extends JsonStorageException {

    /**
     * Constructs a new JsonWriteException with the specified detail message.
     *
     * @param message the detail message explaining why the write operation failed
     */
    public JsonWriteException(String message) {
        super(message);
    }

    /**
     * Constructs a new JsonWriteException with the specified detail message and cause.
     *
     * @param message the detail message explaining why the write operation failed
     * @param cause   the underlying cause of this exception
     */
    public JsonWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}

