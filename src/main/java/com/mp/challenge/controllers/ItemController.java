package com.mp.challenge.controllers;

import com.mp.challenge.components.dtos.AddSpecificationDto;
import com.mp.challenge.components.dtos.CreateItemDto;
import com.mp.challenge.components.dtos.ItemDto;
import com.mp.challenge.components.dtos.PatchItemDto;
import com.mp.challenge.components.dtos.UpdateItemDto;
import com.mp.challenge.controllers.contracts.IItemController;
import com.mp.challenge.services.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * ItemController
 * <p>
 * High-performance REST controller implementation for Item operations using Virtual Threads.
 * This controller provides HTTP endpoints for all Item-related operations with maximum
 * parallelization and optimal performance through asynchronous processing.
 * <p>
 * Features:
 * <ul>
 *   <li>Complete CRUD operations for Items with async support</li>
 *   <li>Advanced search and filtering capabilities with parallel processing</li>
 *   <li>Asynchronous processing with Virtual Threads for high concurrency</li>
 *   <li>Batch operations for optimal performance</li>
 *   <li>Clean separation of concerns with interface contracts</li>
 *   <li>Exception handling delegated to GlobalExceptionHandler with proper async propagation</li>
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
    public CompletableFuture<ResponseEntity<ItemDto>> createItem(@Valid CreateItemDto createItemDto) {
        log.info("Creating new item asynchronously with name: {}", createItemDto.getName());
        return itemService.createItem(createItemDto)
                .thenApply(item -> ResponseEntity.status(HttpStatus.CREATED).body(item));
    }

    @Override
    public CompletableFuture<ResponseEntity<ItemDto>> getItemById(UUID id) {
        log.info("Retrieving item asynchronously with ID: {}", id);
        return itemService.getItem(id)
                .thenApply(ResponseEntity::ok);
    }

    @Override
    public CompletableFuture<ResponseEntity<List<ItemDto>>> getAllItems() {
        log.info("Retrieving all items asynchronously");
        return itemService.getAllItems()
                .thenApply(ResponseEntity::ok);
    }

    @Override
    public CompletableFuture<ResponseEntity<ItemDto>> updateItem(UUID id, @Valid UpdateItemDto updateItemDto) {
        log.info("Updating item asynchronously with ID: {}", id);
        return itemService.updateItem(id, updateItemDto)
                .thenApply(ResponseEntity::ok);
    }

    @Override
    public CompletableFuture<ResponseEntity<Void>> deleteItem(UUID id) {
        log.info("Deleting item asynchronously with ID: {}", id);
        return itemService.deleteItem(id)
                .thenApply(deleted -> deleted ? 
                    ResponseEntity.noContent().build() : 
                    ResponseEntity.notFound().build());
    }

    @Override
    public CompletableFuture<ResponseEntity<List<ItemDto>>> searchItemsByName(String name) {
        log.info("Searching items asynchronously by name: {}", name);
        return itemService.findItemsByName(name)
                .thenApply(ResponseEntity::ok);
    }

    @Override
    public CompletableFuture<ResponseEntity<List<ItemDto>>> searchItemsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Searching items asynchronously by price range: {} - {}", minPrice, maxPrice);
        return itemService.findItemsByPriceRange(minPrice, maxPrice)
                .thenApply(ResponseEntity::ok);
    }

    @Override
    public CompletableFuture<ResponseEntity<List<ItemDto>>> searchItemsByMinimumRating(BigDecimal minRating) {
        log.info("Searching items asynchronously with minimum rating: {}", minRating);
        return itemService.findItemsByMinimumRating(minRating)
                .thenApply(ResponseEntity::ok);
    }

    @Override
    public CompletableFuture<ResponseEntity<Long>> getItemCount() {
        log.info("Retrieving item count asynchronously");
        return itemService.getItemCount()
                .thenApply(ResponseEntity::ok);
    }

    @Override
    public CompletableFuture<ResponseEntity<Boolean>> itemExists(UUID id) {
        log.info("Checking if item exists asynchronously with ID: {}", id);
        return itemService.itemExists(id)
                .thenApply(ResponseEntity::ok);
    }

    @Override
    public CompletableFuture<ResponseEntity<ItemDto>> patchItem(UUID id, @Valid PatchItemDto patchItemDto) {
        log.info("Patching item asynchronously with ID: {}", id);
        return itemService.patchItem(id, patchItemDto)
                .thenApply(ResponseEntity::ok);
    }

    @Override
    public CompletableFuture<ResponseEntity<ItemDto>> addSpecification(UUID id, @Valid AddSpecificationDto addSpecificationDto) {
        log.info("Adding specifications to item asynchronously with ID: {}", id);
        return itemService.addSpecification(id, addSpecificationDto)
                .thenApply(ResponseEntity::ok);
    }

    @Override
    public CompletableFuture<ResponseEntity<List<ItemDto>>> getItemsByIds(List<UUID> ids) {
        log.info("Retrieving {} items asynchronously by IDs", ids.size());
        return itemService.getItemsByIds(ids)
                .thenApply(ResponseEntity::ok);
    }
}
