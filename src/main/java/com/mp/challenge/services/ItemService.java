package com.mp.challenge.services;

import com.mp.challenge.components.dtos.CreateItemDto;
import com.mp.challenge.components.dtos.ItemDto;
import com.mp.challenge.components.dtos.UpdateItemDto;
import com.mp.challenge.components.exceptions.BusinessLogicException;
import com.mp.challenge.components.exceptions.ItemNotFoundException;
import com.mp.challenge.components.exceptions.ValidationException;
import com.mp.challenge.components.mappers.ItemMapper;
import com.mp.challenge.models.Item;
import com.mp.challenge.repositories.ItemRepository;
import com.mp.challenge.services.contracts.IItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ItemService
 * <p>
 * Service implementation for Item operations. This service provides business logic
 * for all Item-related operations, acting as an intermediary between the controller
 * layer and the repository layer.
 * <p>
 * Features:
 * <ul>
 *   <li>Complete CRUD operations with business logic validation</li>
 *   <li>DTO-based operations for clean API integration</li>
 *   <li>Custom exception handling with meaningful error messages</li>
 *   <li>Transaction management for data consistency</li>
 *   <li>Comprehensive logging for debugging and monitoring</li>
 *   <li>Search and filtering capabilities</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 15/10/2025
 */
@Slf4j
@Service
public class ItemService implements IItemService {

    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /**
     * Creates a new item from the provided DTO.
     *
     * @param createItemDto the DTO containing item data to create
     * @return the created item as DTO
     * @throws ValidationException if createItemDto is null
     * @throws BusinessLogicException if business rules are violated
     */
    @Override
    public ItemDto createItem(CreateItemDto createItemDto) {
        validateNotNull(createItemDto, "createItemDto");
        log.debug("Creating new item with name: {}", createItemDto.getName());

        Item item = ItemMapper.toEntity(createItemDto);
        validateBusinessRules(item);
        Item savedItem = itemRepository.save(item);

        log.info("Successfully created item with ID: {} and name: {}", savedItem.getId(), savedItem.getName());

        return ItemMapper.toDto(savedItem);
    }

    /**
     * Retrieves an item by its ID.
     *
     * @param id the unique identifier of the item
     * @return an Optional containing the item DTO if found, empty otherwise
     * @throws ValidationException if id is null
     */
    @Override
    public Optional<ItemDto> getItemById(UUID id) {
        validateNotNull(id, "id");
        log.debug("Retrieving item with ID: {}", id);

        return itemRepository
                .findById(id)
                .map(ItemMapper::toDto);
    }

    /**
     * Retrieves all items in the system.
     *
     * @return a list of all item DTOs
     */
    @Override
    public List<ItemDto> getAllItems() {
        log.debug("Retrieving all items");

        List<Item> items = itemRepository.findAll();

        log.debug("Retrieved {} items", items.size());

        return ItemMapper.toDtoList(items);
    }

    /**
     * Updates an existing item with the provided data.
     *
     * @param itemId the unique identifier of the item to update
     * @param updateItemDto the DTO containing updated item data
     * @return the updated item as DTO
     * @throws ValidationException if itemId or updateItemDto is null
     * @throws ItemNotFoundException if the item with the given ID is not found
     * @throws BusinessLogicException if business rules are violated
     */
    @Override
    public ItemDto updateItem(UUID itemId, UpdateItemDto updateItemDto) {
        validateNotNull(itemId, "itemId");
        validateNotNull(updateItemDto, "updateItemDto");
        log.debug("Updating item with ID: {}", updateItemDto.getId());

        Item updatedItem = itemRepository
                .findById(itemId)
                .map(existingItem -> ItemMapper.updateEntity(existingItem, updateItemDto))
                .orElseThrow(() -> new ItemNotFoundException(itemId, "update operation"));

        validateBusinessRules(updatedItem);
        Item savedItem = itemRepository.save(updatedItem);

        log.debug("Successfully updated item with ID: {}", itemId);

        return ItemMapper.toDto(savedItem);
    }

    /**
     * Deletes an item by its ID.
     *
     * @param id the unique identifier of the item to delete
     * @return true if the item was successfully deleted, false if the item was not found
     * @throws ValidationException if id is null
     */
    @Override
    public boolean deleteItem(UUID id) {
        validateNotNull(id, "id");
        log.debug("Deleting item with ID: {}", id);

        Optional<Item> deletedItem = itemRepository.deleteById(id);
        
        if (deletedItem.isPresent()) {
            log.info("Successfully deleted item with ID: {}", id);
            return true;
        } else {
            log.warn("Attempted to delete non-existent item with ID: {}", id);
            return false;
        }
    }

    /**
     * Checks if an item exists by its ID.
     *
     * @param id the unique identifier of the item to check
     * @return true if the item exists, false otherwise
     * @throws ValidationException if id is null
     */
    @Override
    public boolean itemExists(UUID id) {
        validateNotNull(id, "id");
        log.debug("Checking if item exists with ID: {}", id);

        return itemRepository.findById(id).isPresent();
    }

    /**
     * Gets the total count of items in the system.
     *
     * @return the total number of items
     */
    @Override
    public long getItemCount() {
        log.debug("Retrieving item count");

        return itemRepository.findAll().size();
    }

    /**
     * Searches for items by name (case-insensitive partial match).
     *
     * @param name the name to search for
     * @return a list of item DTOs matching the search criteria
     * @throws ValidationException if name is null
     */
    @Override
    public List<ItemDto> findItemsByName(String name) {
        validateNotNull(name, "name");
        log.debug("Searching items by name: {}", name);

        List<Item> items = itemRepository.findAll().stream()
                .filter(item -> item.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();

        log.info("Found {} items matching name: {}", items.size(), name);

        return ItemMapper.toDtoList(items);
    }

    /**
     * Searches for items within a specified price range.
     *
     * @param minPrice the minimum price (inclusive)
     * @param maxPrice the maximum price (inclusive)
     * @return a list of item DTOs within the price range
     * @throws ValidationException if minPrice or maxPrice is null
     */
    @Override
    public List<ItemDto> findItemsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        validateNotNull(minPrice, "minPrice");
        validateNotNull(maxPrice, "maxPrice");
        log.debug("Searching items by price range: {} - {}", minPrice, maxPrice);

        List<Item> items = itemRepository.findAll().stream()
                .filter(item -> item.getPrice().compareTo(minPrice) >= 0 &&
                        item.getPrice().compareTo(maxPrice) <= 0)
                .toList();

        log.info("Found {} items in price range {} - {}", items.size(), minPrice, maxPrice);

        return ItemMapper.toDtoList(items);
    }

    /**
     * Searches for items with a minimum rating.
     *
     * @param minRating the minimum rating (inclusive)
     * @return a list of item DTOs with rating greater than or equal to minRating
     * @throws ValidationException if minRating is null
     */
    @Override
    public List<ItemDto> findItemsByMinimumRating(BigDecimal minRating) {
        validateNotNull(minRating, "minRating");
        log.debug("Searching items with minimum rating: {}", minRating);

        List<Item> items = itemRepository.findAll().stream()
                .filter(item -> item.getRating() != null &&
                        item.getRating().compareTo(minRating) >= 0)
                .toList();

        log.info("Found {} items with rating >= {}", items.size(), minRating);

        return ItemMapper.toDtoList(items);
    }


    /**
     * Validates business rules for an item.
     *
     * @param item the item to validate
     * @throws BusinessLogicException if any business rule is violated
     */
    private void validateBusinessRules(Item item) {
        // Business rule: Check for duplicate names (case-insensitive)
        List<Item> existingItems = itemRepository.findAll();

        boolean duplicateName = existingItems.stream()
                .anyMatch(existingItem ->
                        !existingItem.getId().equals(item.getId()) &&
                                existingItem.getName().equalsIgnoreCase(item.getName()));

        if (duplicateName) {
            throw new BusinessLogicException("An item with the name '" + item.getName() + "' already exists");
        }

        // Business rule: Validate price is reasonable (not too high)
        if (item.getPrice().compareTo(BigDecimal.valueOf(10000)) > 0) {
            throw new BusinessLogicException("Item price cannot exceed $10,000");
        }

        // Business rule: Validate rating if provided
        if (item.getRating() != null && item.getRating().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessLogicException("Item rating cannot be negative");
        }
    }

    /**
     * Validates that a parameter is not null.
     *
     * @param <T> the type of the parameter
     * @param parameter the parameter to validate
     * @param parameterName the name of the parameter for error messages
     * @throws ValidationException if the parameter is null
     */
    private <T> void validateNotNull(T parameter, String parameterName) {
        if (parameter == null) {
            throw new ValidationException(parameterName + " cannot be null");
        }
    }
}
