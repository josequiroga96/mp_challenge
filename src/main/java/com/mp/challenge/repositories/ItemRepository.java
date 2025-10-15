package com.mp.challenge.repositories;

import com.mp.challenge.components.exceptions.JsonReadException;
import com.mp.challenge.components.exceptions.JsonWriteException;
import com.mp.challenge.models.Item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ItemRepository
 * <p>
 * Repository interface for managing Item entities with CRUD operations.
 * This interface defines the contract for Item data persistence operations,
 * providing methods to save, find, retrieve all, and delete items.
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 14/10/2025
 */
public interface ItemRepository {

    /**
     * Saves an item to the repository.
     * <p>
     * If the item has a null ID, it will be treated as a new item and assigned a new ID.
     * If the item has an existing ID, it will update the existing item.
     *
     * @param item the item to save, must not be null
     * @return the saved item with its ID assigned
     * @throws IllegalArgumentException                       if the item is null
     * @throws JsonWriteException if there's an error writing to storage
     */
    Item save(Item item);

    /**
     * Finds an item by its unique identifier.
     *
     * @param id the unique identifier of the item to find
     * @return an Optional containing the item if found, empty otherwise
     * @throws IllegalArgumentException                      if the id is null
     * @throws JsonReadException if there's an error reading from storage
     */
    Optional<Item> findById(UUID id);

    /**
     * Retrieves all items from the repository.
     *
     * @return a list of all items, empty list if no items exist
     * @throws JsonReadException if there's an error reading from storage
     */
    List<Item> findAll();

    /**
     * Deletes an item by its unique identifier.
     *
     * @param id the unique identifier of the item to delete
     * @return an Optional containing the deleted item if it existed, empty otherwise
     * @throws IllegalArgumentException                       if the id is null
     * @throws JsonWriteException if there's an error writing to storage
     */
    Optional<Item> deleteById(UUID id);
}
