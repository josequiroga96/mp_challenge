package com.mp.challenge.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

/**
 * ItemNotFoundException
 * <p>
 * Exception thrown when an Item entity cannot be found by its ID.
 * <p>
 * This exception is typically thrown when:
 * <ul>
 *   <li>An item with the specified ID does not exist</li>
 *   <li>An item was previously deleted</li>
 *   <li>An invalid ID is provided</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 15/10/2025
 */
public class ItemNotFoundException extends BaseApplicationException {

    private static final String ERROR_CODE = "ITEM_NOT_FOUND";
    private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND;

    /**
     * Constructs a new ItemNotFoundException for the specified item ID.
     *
     * @param itemId the ID of the item that was not found
     */
    public ItemNotFoundException(UUID itemId) {
        super(
            ERROR_CODE,
            HTTP_STATUS,
            String.format("Item with ID '%s' was not found", itemId),
            "The requested item could not be found",
            Map.of("itemId", itemId.toString())
        );
    }

    /**
     * Constructs a new ItemNotFoundException with additional context.
     *
     * @param itemId the ID of the item that was not found
     * @param context additional context about the operation
     */
    public ItemNotFoundException(UUID itemId, String context) {
        super(
            ERROR_CODE,
            HTTP_STATUS,
            String.format("Item with ID '%s' was not found during %s", itemId, context),
            "The requested item could not be found",
            Map.of("itemId", itemId.toString(), "context", context)
        );
    }
}
