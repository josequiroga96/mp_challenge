package com.mp.challenge.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mp.challenge.exceptions.JsonReadException;
import com.mp.challenge.exceptions.JsonWriteException;
import com.mp.challenge.exceptions.ValidationException;
import com.mp.challenge.exceptions.ItemNotFoundException;
import com.mp.challenge.exceptions.BusinessLogicException;
import com.mp.challenge.infrastructure.JsonFileStorageCAS;
import com.mp.challenge.models.Item;
import com.mp.challenge.models.collections.ItemCollection;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * ItemRepositoryCAS
 * <p>
 * Compare-and-Swap implementation of ItemRepository using JSON file storage.
 * This repository provides thread-safe CRUD operations for Item entities using
 * atomic file operations with optimistic locking. It maintains an in-memory
 * collection of items that is periodically persisted to a JSON file.
 * <p>
 * Features:
 * <ul>
 *   <li>Thread-safe operations using atomic updates</li>
 *   <li>Automatic ID generation for new items</li>
 *   <li>Optimistic locking to prevent data corruption</li>
 *   <li>Automatic file persistence with configurable debounce</li>
 *   <li>Resource management with AutoCloseable</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 14/10/2025
 */
@Slf4j
public final class ItemRepositoryCAS implements ItemRepository, AutoCloseable {

    private final JsonFileStorageCAS<ItemCollection> storage;

    /**
     * Constructs a new ItemRepositoryCAS with the specified storage file.
     *
     * @param storageFile  the path to the JSON file for persistence
     * @param objectMapper the Jackson ObjectMapper for JSON serialization
     * @throws JsonReadException if there's an error initializing the storage
     */
    public ItemRepositoryCAS(Path storageFile, ObjectMapper objectMapper) throws JsonReadException {
        ItemCollection initialData = new ItemCollection();
        this.storage = new JsonFileStorageCAS<>(storageFile, objectMapper, ItemCollection.class, initialData, 200);

        log.info("ItemRepositoryCAS initialized with storage file: {}", storageFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Item save(Item item) {
        validateItemNotNull(item);
        log.debug("Saving item: {}", item);

        UnaryOperator<ItemCollection> updateFunction = createSaveUpdateFunction(item);
        ItemCollection updatedData = storage.updateAtomically(updateFunction);

        return retrieveSavedItem(item, updatedData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Item> findById(UUID id) {
        validateIdNotNull(id);
        log.debug("Finding item by ID: {}", id);

        ItemCollection currentData = storage.getCurrentSnapshot();
        Optional<Item> result = currentData.findItemById(id);

        log.debug("Item {} found: {}", id, result.isPresent());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Item> findAll() {
        log.debug("Retrieving all items");

        ItemCollection currentData = storage.getCurrentSnapshot();
        List<Item> result = List.copyOf(currentData.getItems());

        log.debug("Retrieved {} items", result.size());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Item> deleteById(UUID id) {
        validateIdNotNull(id);
        log.debug("Deleting item by ID: {}", id);

        Optional<Item> itemToDelete = getItemToDelete(id);
        UnaryOperator<ItemCollection> updateFunction = createDeleteUpdateFunction(id);
        storage.updateAtomically(updateFunction);

        return itemToDelete;
    }

    /**
     * Forces a flush of pending data to disk.
     * <p>
     * This method can be useful when you need to ensure data is persisted
     * immediately rather than waiting for the automatic debounce timer.
     *
     * @throws JsonWriteException if there's an error writing to disk
     */
    public void flushToDisk() throws JsonWriteException {
        log.debug("Manually flushing data to disk");
        storage.flushToDisk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        log.info("Closing ItemRepositoryCAS");
        storage.close();
    }

    /**
     * Validates that the item is not null.
     *
     * @param item the item to validate
     * @throws ValidationException if the item is null
     */
    private void validateItemNotNull(Item item) {
        if (item == null) {
            throw new ValidationException("item", "Item cannot be null");
        }
    }

    /**
     * Validates that the ID is not null.
     *
     * @param id the ID to validate
     * @throws ValidationException if the ID is null
     */
    private void validateIdNotNull(UUID id) {
        if (id == null) {
            throw new ValidationException("id", "ID cannot be null");
        }
    }

    /**
     * Creates the update function for saving an item.
     *
     * @param item the item to save
     * @return the update function for atomic operation
     */
    private UnaryOperator<ItemCollection> createSaveUpdateFunction(Item item) {
        return currentData -> {
            ItemCollection newData = new ItemCollection(currentData);

            if (item.getId() == null) {
                handleNewItemSave(item, newData);
            } else {
                handleExistingItemSave(item, newData);
            }

            return newData;
        };
    }

    /**
     * Handles saving a new item with generated UUID.
     *
     * @param item the item to save
     * @param newData the collection to add the item to
     */
    private void handleNewItemSave(Item item, ItemCollection newData) {
        UUID newId = UUID.randomUUID();
        Item newItem = createItemWithTimestamps(item, newId);
        newData.addItem(newItem);
        log.debug("Created new item with ID: {}", newId);
    }

    /**
     * Handles saving an existing item (update or add with existing ID).
     *
     * @param item the item to save
     * @param newData the collection to update
     */
    private void handleExistingItemSave(Item item, ItemCollection newData) {
        Optional<Item> existingItem = newData.findItemById(item.getId());
        
        if (existingItem.isPresent()) {
            updateExistingItem(item, newData);
        } else {
            addItemWithExistingId(item, newData);
        }
    }

    /**
     * Updates an existing item with new timestamp.
     *
     * @param item the item to update
     * @param newData the collection to update
     */
    private void updateExistingItem(Item item, ItemCollection newData) {
        Optional<Item> existingItem = newData.findItemById(item.getId());
        Item updatedItem = createUpdatedItem(item, existingItem.orElse(null));
        newData.updateItem(updatedItem);
        log.debug("Updated existing item with ID: {}", item.getId());
    }

    /**
     * Adds an item with an existing ID.
     *
     * @param item the item to add
     * @param newData the collection to add to
     */
    private void addItemWithExistingId(Item item, ItemCollection newData) {
        Item newItem = createItemWithTimestamps(item, item.getId());
        newData.addItem(newItem);
        log.debug("Added item with existing ID: {}", item.getId());
    }

    /**
     * Creates an item with current timestamps.
     *
     * @param item the base item
     * @param id the ID to use
     * @return a new item with current timestamps
     */
    private Item createItemWithTimestamps(Item item, UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return new Item(id, item.getName(), item.getImageUrl(), item.getDescription(),
                item.getPrice(), item.getRating(), item.getSpecifications(), now, now);
    }

    /**
     * Creates an updated item with new timestamp.
     *
     * @param item the item to update
     * @param existingItem the existing item to preserve timestamps from
     * @return the updated item with new timestamp
     */
    private Item createUpdatedItem(Item item, Item existingItem) {
        LocalDateTime createdAt = (existingItem != null && existingItem.getCreatedAt() != null) 
                ? existingItem.getCreatedAt() 
                : LocalDateTime.now();
        
        return new Item(item.getId(), item.getName(), item.getImageUrl(),
                item.getDescription(), item.getPrice(), item.getRating(),
                item.getSpecifications(), createdAt, LocalDateTime.now());
    }

    /**
     * Retrieves the saved item from the updated collection.
     *
     * @param originalItem the original item that was saved
     * @param updatedData the updated collection
     * @return the saved item
     * @throws BusinessLogicException if the item cannot be retrieved
     */
    private Item retrieveSavedItem(Item originalItem, ItemCollection updatedData) {
        if (originalItem.getId() == null) {
            return findNewlyCreatedItem(updatedData);
        } else {
            return findItemByIdOrThrow(updatedData, originalItem.getId());
        }
    }

    /**
     * Finds the newly created item by timestamp.
     *
     * @param updatedData the updated collection
     * @return the newly created item
     * @throws BusinessLogicException if the item cannot be found
     */
    private Item findNewlyCreatedItem(ItemCollection updatedData) {
        return updatedData.getItems().stream()
                .filter(i -> i.getCreatedAt() != null && i.getUpdatedAt() != null)
                .max(java.util.Comparator.comparing(Item::getCreatedAt))
                .orElseThrow(() -> new BusinessLogicException("Failed to retrieve saved item after creation"));
    }

    /**
     * Finds an item by ID or throws an exception if not found.
     *
     * @param updatedData the collection to search
     * @param id the ID to search for
     * @return the found item
     * @throws ItemNotFoundException if the item is not found
     */
    private Item findItemByIdOrThrow(ItemCollection updatedData, UUID id) {
        return updatedData.findItemById(id)
                .orElseThrow(() -> new ItemNotFoundException(id, "retrieval after save"));
    }

    /**
     * Gets the item to delete before performing the deletion.
     *
     * @param id the ID of the item to delete
     * @return the item to delete if it exists
     */
    private Optional<Item> getItemToDelete(UUID id) {
        ItemCollection currentData = storage.getCurrentSnapshot();
        return currentData.findItemById(id);
    }

    /**
     * Creates the update function for deleting an item.
     *
     * @param id the ID of the item to delete
     * @return the update function for atomic operation
     */
    private UnaryOperator<ItemCollection> createDeleteUpdateFunction(UUID id) {
        return data -> {
            ItemCollection newData = new ItemCollection(data);
            Optional<Item> deletedItem = newData.removeItemById(id);

            logDeletionResult(id, deletedItem.isPresent());
            return newData;
        };
    }

    /**
     * Logs the result of a deletion operation.
     *
     * @param id the ID of the item that was deleted
     * @param wasDeleted whether the item was actually deleted
     */
    private void logDeletionResult(UUID id, boolean wasDeleted) {
        if (wasDeleted) {
            log.debug("Successfully deleted item with ID: {}", id);
        } else {
            log.debug("Item with ID {} not found for deletion", id);
        }
    }
}
