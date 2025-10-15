package com.mp.challenge.models.collections;

import com.mp.challenge.models.Item;
import lombok.Data;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ItemCollection
 * <p>
 * Internal data structure to hold the collection of items.
 * This class provides thread-safe operations on the item collection.
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 14/10/2025
 */
@Data
public class ItemCollection {
    private final List<Item> items;

    public ItemCollection() {
        this.items = new CopyOnWriteArrayList<>();
    }

    public ItemCollection(ItemCollection other) {
        this.items = new CopyOnWriteArrayList<>(other.getItems());
    }

    /**
     * Adds an item to this collection.
     *
     * @param item the item to add
     */
    public void addItem(Item item) {
        items.add(item);
    }

    /**
     * Updates an existing item in this collection.
     *
     * @param item the item to update
     */
    public void updateItem(Item item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                break;
            }
        }
    }

    /**
     * Finds an item by its ID.
     *
     * @param id the ID to search for
     * @return an Optional containing the item if found
     */
    public Optional<Item> findItemById(UUID id) {
        return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();
    }

    /**
     * Removes an item by its ID.
     *
     * @param id the ID of the item to remove
     * @return an Optional containing the removed item if it existed
     */
    public Optional<Item> removeItemById(UUID id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(id)) {
                return Optional.of(items.remove(i));
            }
        }
        return Optional.empty();
    }
}
