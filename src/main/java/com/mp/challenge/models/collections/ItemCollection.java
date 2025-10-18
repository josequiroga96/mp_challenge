package com.mp.challenge.models.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mp.challenge.components.serializers.ItemCollectionDeserializer;
import com.mp.challenge.components.serializers.ItemCollectionSerializer;
import com.mp.challenge.models.Item;
import lombok.Data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ItemCollection
 * <p>
 * Internal data structure to hold the collection of items.
 * This class provides thread-safe operations on the item collection using ConcurrentHashMap
 * for optimal performance with O(1) operations for most common use cases.
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 14/10/2025
 */
@Data
@JsonSerialize(using = ItemCollectionSerializer.class)
@JsonDeserialize(using = ItemCollectionDeserializer.class)
public class ItemCollection {
    private final Map<UUID, Item> items;

    public ItemCollection() {
        this.items = new ConcurrentHashMap<>();
    }

    public ItemCollection(ItemCollection other) {
        this.items = new ConcurrentHashMap<>();
        other.getItems().forEach(item -> this.items.put(item.getId(), item));
    }

    @JsonCreator
    public ItemCollection(@JsonProperty("items") Map<UUID, Item> items) {
        this.items = new ConcurrentHashMap<>();
        if (items != null) {
            this.items.putAll(items);
        }
    }

    /**
     * Adds an item to this collection.
     *
     * @param item the item to add
     */
    public void addItem(Item item) {
        items.put(item.getId(), item);
    }

    /**
     * Updates an existing item in this collection.
     *
     * @param item the item to update
     */
    public void updateItem(Item item) {
        items.put(item.getId(), item);
    }

    /**
     * Finds an item by its ID.
     *
     * @param id the ID to search for
     * @return an Optional containing the item if found
     */
    public Optional<Item> findItemById(UUID id) {
        return Optional.ofNullable(items.get(id));
    }

    /**
     * Removes an item by its ID.
     *
     * @param id the ID of the item to remove
     * @return an Optional containing the removed item if it existed
     */
    public Optional<Item> removeItemById(UUID id) {
        return Optional.ofNullable(items.remove(id));
    }

    /**
     * Gets all items as a list.
     *
     * @return a list of all items
     */
    public List<Item> getItems() {
        return new ArrayList<>(items.values());
    }

    /**
     * Gets the items map directly.
     * This method is used by the custom serializer.
     *
     * @return the items map
     */
    public Map<UUID, Item> getItemsMap() {
        return items;
    }
}
