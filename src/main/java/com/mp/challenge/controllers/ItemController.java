package com.mp.challenge.controllers;

import com.mp.challenge.components.dtos.CreateItemDto;
import com.mp.challenge.components.dtos.ItemDto;
import com.mp.challenge.components.dtos.UpdateItemDto;
import com.mp.challenge.controllers.contracts.IItemController;
import com.mp.challenge.services.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * ItemController
 * <p>
 * REST controller implementation for Item operations. This controller provides
 * HTTP endpoints for all Item-related operations, supporting both synchronous
 * and asynchronous processing using Java 21 Virtual Threads.
 * <p>
 * Features:
 * <ul>
 *   <li>Complete CRUD operations for Items</li>
 *   <li>Search and filtering capabilities</li>
 *   <li>Asynchronous processing with Virtual Threads</li>
 *   <li>Clean separation of concerns with interface contracts</li>
 *   <li>Exception handling delegated to GlobalExceptionHandler</li>
 *   <li>Input validation with Jakarta Bean Validation</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 16/10/2025
 */
@Slf4j
@RestController
public class ItemController implements IItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public ResponseEntity<ItemDto> createItem(CreateItemDto createItemDto) {
        log.info("Creating new item with name: {}", createItemDto.getName());
        ItemDto createdItem = itemService.createItem(createItemDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @Override
    public ResponseEntity<ItemDto> getItemById(UUID id) {
        log.info("Retrieving item with ID: {}", id);
        return itemService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<ItemDto>> getAllItems() {
        log.info("Retrieving all items");
        List<ItemDto> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    @Override
    public ResponseEntity<ItemDto> updateItem(UUID id, UpdateItemDto updateItemDto) {
        log.info("Updating item with ID: {}", id);
        ItemDto updatedItem = itemService.updateItem(id, updateItemDto);
        return ResponseEntity.ok(updatedItem);
    }

    @Override
    public ResponseEntity<Void> deleteItem(UUID id) {
        log.info("Deleting item with ID: {}", id);
        boolean deleted = itemService.deleteItem(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<List<ItemDto>> searchItemsByName(String name) {
        log.info("Searching items by name: {}", name);
        List<ItemDto> items = itemService.findItemsByName(name);
        return ResponseEntity.ok(items);
    }

    @Override
    public ResponseEntity<List<ItemDto>> searchItemsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Searching items by price range: {} - {}", minPrice, maxPrice);
        List<ItemDto> items = itemService.findItemsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(items);
    }

    @Override
    public ResponseEntity<List<ItemDto>> searchItemsByMinimumRating(BigDecimal minRating) {
        log.info("Searching items with minimum rating: {}", minRating);
        List<ItemDto> items = itemService.findItemsByMinimumRating(minRating);
        return ResponseEntity.ok(items);
    }

    @Override
    public ResponseEntity<Long> getItemCount() {
        log.info("Retrieving item count");
        long count = itemService.getItemCount();
        return ResponseEntity.ok(count);
    }

    @Override
    public ResponseEntity<Boolean> itemExists(UUID id) {
        log.info("Checking if item exists with ID: {}", id);
        boolean exists = itemService.itemExists(id);
        return ResponseEntity.ok(exists);
    }
}
