package com.mp.challenge.services.contracts;

import com.mp.challenge.components.dtos.AddSpecificationDto;
import com.mp.challenge.components.dtos.CreateItemDto;
import com.mp.challenge.components.dtos.ItemDto;
import com.mp.challenge.components.dtos.PatchItemDto;
import com.mp.challenge.components.dtos.UpdateItemDto;
import com.mp.challenge.components.exceptions.BusinessLogicException;
import com.mp.challenge.components.exceptions.ItemNotFoundException;
import com.mp.challenge.components.exceptions.ValidationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * IItemService
 * <p>
 * Service interface for Item operations with full asynchronous support using Virtual Threads.
 * This interface defines the contract for all Item-related business operations, providing
 * a clean abstraction layer between the controller and repository layers with maximum
 * parallelization and performance optimization.
 * <p>
 * Features:
 * <ul>
 *   <li>Complete CRUD operations for Item entities with async support</li>
 *   <li>DTO-based operations for API layer integration</li>
 *   <li>Business logic abstraction with parallelization</li>
 *   <li>Exception handling through custom exceptions</li>
 *   <li>Virtual Threads integration for high concurrency</li>
 *   <li>Advanced search and filtering with parallel processing</li>
 *   <li>Batch operations for optimal performance</li>
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
     * Creates a new item from the provided CreateItemDto asynchronously.
     *
     * @param createItemDto the DTO containing item creation data
     * @return CompletableFuture containing ItemDto representing the created item
     * @throws ValidationException    if the provided DTO is invalid
     * @throws BusinessLogicException if business rules are violated
     */
    CompletableFuture<ItemDto> createItem(CreateItemDto createItemDto);

    /**
     * Retrieves an item by its ID asynchronously.
     *
     * @param id the unique identifier of the item
     * @return CompletableFuture containing the item DTO
     * @throws ItemNotFoundException if the item with the given ID is not found
     * @throws ValidationException if id is null
     */
    CompletableFuture<ItemDto> getItem(UUID id);

    /**
     * Retrieves multiple items by their IDs asynchronously in parallel.
     *
     * @param ids the list of unique identifiers to retrieve
     * @return CompletableFuture containing a list of found items (may be empty for missing IDs)
     * @throws ValidationException if ids is null or contains null elements
     */
    CompletableFuture<List<ItemDto>> getItemsByIds(List<UUID> ids);

    /**
     * Retrieves all items in the system asynchronously.
     *
     * @return CompletableFuture containing List of ItemDtos representing all items
     */
    CompletableFuture<List<ItemDto>> getAllItems();

    /**
     * Updates an existing item with the provided data asynchronously.
     *
     * @param itemId the unique identifier of the item to update
     * @param updateItemDto the DTO containing update data
     * @return CompletableFuture containing ItemDto representing the updated item
     * @throws ValidationException    if the provided DTO is invalid
     * @throws ItemNotFoundException  if the item to update doesn't exist
     * @throws BusinessLogicException if business rules are violated
     */
    CompletableFuture<ItemDto> updateItem(UUID itemId, UpdateItemDto updateItemDto);

    /**
     * Deletes an item by its ID asynchronously.
     *
     * @param id the UUID of the item to delete
     * @return CompletableFuture containing true if the item was deleted, false if it didn't exist
     * @throws ValidationException if the provided ID is null
     */
    CompletableFuture<Boolean> deleteItem(UUID id);

    /**
     * Checks if an item exists by its ID asynchronously.
     *
     * @param id the UUID of the item to check
     * @return CompletableFuture containing true if the item exists, false otherwise
     * @throws ValidationException if the provided ID is null
     */
    CompletableFuture<Boolean> itemExists(UUID id);

    /**
     * Retrieves the total count of items in the system asynchronously.
     *
     * @return CompletableFuture containing the number of items
     */
    CompletableFuture<Long> getItemCount();

    /**
     * Retrieves items by name (partial match, case-insensitive) asynchronously.
     *
     * @param name the name or partial name to search for
     * @return CompletableFuture containing List of ItemDtos matching the search criteria
     * @throws ValidationException if the provided name is null or empty
     */
    CompletableFuture<List<ItemDto>> findItemsByName(String name);

    /**
     * Retrieves items within a specific price range asynchronously.
     *
     * @param minPrice the minimum price (inclusive)
     * @param maxPrice the maximum price (inclusive)
     * @return CompletableFuture containing List of ItemDtos within the price range
     * @throws ValidationException if price parameters are invalid
     */
    CompletableFuture<List<ItemDto>> findItemsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Retrieves items with a rating above the specified threshold asynchronously.
     *
     * @param minRating the minimum rating threshold (inclusive)
     * @return CompletableFuture containing List of ItemDtos with rating >= minRating
     * @throws ValidationException if the rating parameter is invalid
     */
    CompletableFuture<List<ItemDto>> findItemsByMinimumRating(BigDecimal minRating);

    /**
     * Performs partial update of an item asynchronously using PATCH semantics.
     * Only provided fields will be updated, others will remain unchanged.
     *
     * @param id the UUID of the item to update
     * @param patchItemDto DTO containing only the fields to update
     * @return CompletableFuture containing the updated ItemDto
     * @throws ValidationException if ID is null or DTO is invalid
     * @throws ItemNotFoundException if item with given ID does not exist
     * @throws BusinessLogicException if business rules are violated
     */
    CompletableFuture<ItemDto> patchItem(UUID id, PatchItemDto patchItemDto);

    /**
     * Adds specifications to an existing item asynchronously.
     * New specifications are added to existing ones without replacing them.
     *
     * @param id the UUID of the item to add specifications to
     * @param addSpecificationDto DTO containing specifications to add
     * @return CompletableFuture containing the updated ItemDto
     * @throws ValidationException if ID is null or DTO is invalid
     * @throws ItemNotFoundException if item with given ID does not exist
     * @throws BusinessLogicException if business rules are violated
     */
    CompletableFuture<ItemDto> addSpecification(UUID id, AddSpecificationDto addSpecificationDto);
}
