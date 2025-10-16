package com.mp.challenge.services.contracts;

import com.mp.challenge.components.dtos.CreateItemDto;
import com.mp.challenge.components.dtos.ItemDto;
import com.mp.challenge.components.dtos.UpdateItemDto;
import com.mp.challenge.components.exceptions.BusinessLogicException;
import com.mp.challenge.components.exceptions.ItemNotFoundException;
import com.mp.challenge.components.exceptions.ValidationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * IItemService
 * <p>
 * Service interface for Item operations. This interface defines the contract for
 * all Item-related business operations, providing a clean abstraction layer
 * between the controller and repository layers.
 * <p>
 * Features:
 * <ul>
 *   <li>Complete CRUD operations for Item entities</li>
 *   <li>DTO-based operations for API layer integration</li>
 *   <li>Business logic abstraction</li>
 *   <li>Exception handling through custom exceptions</li>
 *   <li>Optional return types for safe operations</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 15/10/2025
 */
public interface IItemService {

    /**
     * Creates a new item from the provided CreateItemDto.
     *
     * @param createItemDto the DTO containing item creation data
     * @return ItemDto representing the created item
     * @throws ValidationException    if the provided DTO is invalid
     * @throws BusinessLogicException if business rules are violated
     */
    ItemDto createItem(CreateItemDto createItemDto);

    /**
     * Retrieves an item by its ID.
     *
     * @param id the UUID of the item to retrieve
     * @return Optional containing the ItemDto if found, empty otherwise
     * @throws ValidationException if the provided ID is null
     */
    Optional<ItemDto> getItemById(UUID id);

    /**
     * Retrieves all items in the system.
     *
     * @return List of ItemDtos representing all items
     */
    List<ItemDto> getAllItems();

    /**
     * Updates an existing item with the provided data.
     *
     * @param updateItemDto the DTO containing update data
     * @return ItemDto representing the updated item
     * @throws ValidationException    if the provided DTO is invalid
     * @throws ItemNotFoundException  if the item to update doesn't exist
     * @throws BusinessLogicException if business rules are violated
     */
    ItemDto updateItem(UUID itemId, UpdateItemDto updateItemDto);

    /**
     * Deletes an item by its ID.
     *
     * @param id the UUID of the item to delete
     * @return true if the item was deleted, false if it didn't exist
     * @throws ValidationException if the provided ID is null
     */
    boolean deleteItem(UUID id);

    /**
     * Checks if an item exists by its ID.
     *
     * @param id the UUID of the item to check
     * @return true if the item exists, false otherwise
     * @throws ValidationException if the provided ID is null
     */
    boolean itemExists(UUID id);

    /**
     * Retrieves the total count of items in the system.
     *
     * @return the number of items
     */
    long getItemCount();

    /**
     * Retrieves items by name (partial match, case-insensitive).
     *
     * @param name the name or partial name to search for
     * @return List of ItemDtos matching the search criteria
     * @throws ValidationException if the provided name is null or empty
     */
    List<ItemDto> findItemsByName(String name);

    /**
     * Retrieves items within a specific price range.
     *
     * @param minPrice the minimum price (inclusive)
     * @param maxPrice the maximum price (inclusive)
     * @return List of ItemDtos within the price range
     * @throws ValidationException if price parameters are invalid
     */
    List<ItemDto> findItemsByPriceRange(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);

    /**
     * Retrieves items with a rating above the specified threshold.
     *
     * @param minRating the minimum rating threshold (inclusive)
     * @return List of ItemDtos with rating >= minRating
     * @throws ValidationException if the rating parameter is invalid
     */
    List<ItemDto> findItemsByMinimumRating(java.math.BigDecimal minRating);
}
