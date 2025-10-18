package com.mp.challenge.services;

import com.mp.challenge.components.dtos.*;
import com.mp.challenge.components.exceptions.BusinessLogicException;
import com.mp.challenge.components.exceptions.ItemNotFoundException;
import com.mp.challenge.components.exceptions.ValidationException;
import com.mp.challenge.components.mappers.ItemMapper;
import com.mp.challenge.models.Item;
import com.mp.challenge.repositories.ItemRepository;
import com.mp.challenge.services.contracts.IItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * ItemService
 * <p>
 * High-performance asynchronous service implementation for Item operations using Virtual Threads.
 * This service provides business logic for all Item-related operations with maximum
 * parallelization and optimal performance, acting as an intermediary between the controller
 * and repository layers.
 * <p>
 * Features:
 * <ul>
 *   <li>Complete CRUD operations with async support and business logic validation</li>
 *   <li>DTO-based operations for clean API integration</li>
 *   <li>Advanced parallelization using Virtual Threads</li>
 *   <li>Custom exception handling with meaningful error messages</li>
 *   <li>Batch operations for optimal performance</li>
 *   <li>Comprehensive logging for debugging and monitoring</li>
 *   <li>Advanced search and filtering with parallel processing</li>
 *   <li>Thread-safe operations compatible with CAS implementation</li>
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
    private final TaskExecutor executor;

    @Autowired
    public ItemService(ItemRepository itemRepository, TaskExecutor executor) {
        this.itemRepository = itemRepository;
        this.executor = executor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<ItemDto> createItem(CreateItemDto createItemDto) {
        validateNotNull(createItemDto, "createItemDto");
        
        log.debug("Creating new item asynchronously with name: {}", createItemDto.getName());
        Item item = ItemMapper.toEntity(createItemDto);
        
        return validateBusinessRulesAsync(item)
                .thenCompose(validatedItem -> supplyAsync(() -> itemRepository.save(validatedItem)))
                .thenApply(ItemMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<ItemDto> getItem(UUID id) {
        validateNotNull(id, "id");
        log.debug("Retrieving item asynchronously with ID: {}", id);
        
        return supplyAsync(() -> itemRepository
                .findById(id)
                .map(ItemMapper::toDto)
                .orElseThrow(() -> new ItemNotFoundException(id, "get operation")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<ItemDto>> getItemsByIds(List<UUID> ids) {
        validateNotNull(ids, "ids");
        log.debug("Retrieving {} items asynchronously in parallel", ids.size());
        
        if (ids.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        List<CompletableFuture<ItemDto>> futures = ids.stream()
                .map(this::getItem)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList()
                );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<ItemDto>> getAllItems() {
        log.debug("Retrieving all items asynchronously");

        return supplyAsync(itemRepository::findAll)
                .thenApply(ItemMapper::toDtoList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<ItemDto> updateItem(UUID itemId, UpdateItemDto updateItemDto) {
        validateNotNull(itemId, "itemId");
        validateNotNull(updateItemDto, "updateItemDto");
        log.debug("Updating item asynchronously with ID: {}", itemId);

        return supplyAsync(() -> itemRepository
                .findById(itemId)
                .map(existingItem -> ItemMapper.updateEntity(existingItem, updateItemDto))
                .orElseThrow(() -> new ItemNotFoundException(itemId, "update operation")))
                .thenCompose(this::validateBusinessRulesAsync)
                .thenCompose(item -> supplyAsync(() -> itemRepository.save(item)))
                .thenApply(ItemMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Boolean> deleteItem(UUID id) {
        validateNotNull(id, "id");
        log.debug("Deleting item asynchronously with ID: {}", id);

        return supplyAsync(() -> itemRepository.deleteById(id).isPresent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Boolean> itemExists(UUID id) {
        validateNotNull(id, "id");
        log.debug("Checking if item exists asynchronously with ID: {}", id);

        return supplyAsync(() -> itemRepository.findById(id).isPresent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Long> getItemCount() {
        log.debug("Retrieving item count asynchronously");

        return supplyAsync(() -> (long) itemRepository.findAll().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<ItemDto>> findItemsByName(String name) {
        validateNotNull(name, "name");
        log.debug("Searching items by name asynchronously: {}", name);

        return supplyAsync(() -> {
            List<Item> items = itemRepository.findAll().stream()
                    .filter(item -> item.getName().toLowerCase().contains(name.toLowerCase()))
                    .toList();

            List<ItemDto> result = ItemMapper.toDtoList(items);
            log.info("Found {} items matching name asynchronously: {}", result.size(), name);
            return result;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<ItemDto>> findItemsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        validateNotNull(minPrice, "minPrice");
        validateNotNull(maxPrice, "maxPrice");
        log.debug("Searching items by price range asynchronously: {} - {}", minPrice, maxPrice);

        return supplyAsync(() -> {
            List<Item> items = itemRepository.findAll().stream()
                    .filter(item -> item.getPrice().compareTo(minPrice) >= 0 &&
                            item.getPrice().compareTo(maxPrice) <= 0)
                    .toList();

            List<ItemDto> result = ItemMapper.toDtoList(items);
            log.info("Found {} items in price range asynchronously {} - {}", result.size(), minPrice, maxPrice);
            return result;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<ItemDto>> findItemsByMinimumRating(BigDecimal minRating) {
        validateNotNull(minRating, "minRating");
        log.debug("Searching items with minimum rating asynchronously: {}", minRating);

        return supplyAsync(() -> {
            List<Item> items = itemRepository.findAll().stream()
                    .filter(item -> item.getRating() != null &&
                            item.getRating().compareTo(minRating) >= 0)
                    .toList();

            List<ItemDto> result = ItemMapper.toDtoList(items);
            log.info("Found {} items with rating >= {} asynchronously", result.size(), minRating);
            return result;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<ItemDto> patchItem(UUID id, PatchItemDto patchItemDto) {
        validateNotNull(id, "id");
        validateNotNull(patchItemDto, "patchItemDto");
        log.debug("Patching item asynchronously with ID: {}", id);

        return supplyAsync(() -> itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id, "patch operation")))
                .thenCompose(existingItem -> supplyAsync(() -> Item.builder()
                        .id(existingItem.getId())
                        .name(patchItemDto.getName() != null ? patchItemDto.getName() : existingItem.getName())
                        .imageUrl(patchItemDto.getImageUrl() != null ? patchItemDto.getImageUrl() : existingItem.getImageUrl())
                        .description(patchItemDto.getDescription() != null ? patchItemDto.getDescription() : existingItem.getDescription())
                        .price(patchItemDto.getPrice() != null ? patchItemDto.getPrice() : existingItem.getPrice())
                        .rating(patchItemDto.getRating() != null ? patchItemDto.getRating() : existingItem.getRating())
                        .specifications(existingItem.getSpecifications()) // Keep existing specifications
                        .createdAt(existingItem.getCreatedAt())
                        .updatedAt(java.time.LocalDateTime.now())
                        .build()))
                .thenCompose(this::validateBusinessRulesAsync)
                .thenCompose(item -> supplyAsync(() -> itemRepository.save(item)))
                .thenApply(ItemMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<ItemDto> addSpecification(UUID id, AddSpecificationDto addSpecificationDto) {
        validateNotNull(id, "id");
        validateNotNull(addSpecificationDto, "addSpecificationDto");
        log.debug("Adding specifications to item asynchronously with ID: {}", id);

        return supplyAsync(() -> {
            Item existingItem = itemRepository.findById(id)
                    .orElseThrow(() -> new ItemNotFoundException(id, "add specification operation"));

            // Merge existing specifications with new ones
            List<String> existingSpecs = existingItem.getSpecifications() != null ?
                    existingItem.getSpecifications() : List.of();
            List<String> newSpecs = addSpecificationDto.getSpecifications();

            // Check if adding new specs would exceed the limit
            if (existingSpecs.size() + newSpecs.size() > 20) {
                throw new BusinessLogicException("Cannot add specifications: would exceed maximum limit of 20");
            }

            List<String> mergedSpecs = java.util.stream.Stream
                    .concat(existingSpecs.stream(), newSpecs.stream())
                    .distinct() // Remove duplicates
                    .toList();

            Item updatedItem = Item.builder()
                    .id(existingItem.getId())
                    .name(existingItem.getName())
                    .imageUrl(existingItem.getImageUrl())
                    .description(existingItem.getDescription())
                    .price(existingItem.getPrice())
                    .rating(existingItem.getRating())
                    .specifications(mergedSpecs)
                    .createdAt(existingItem.getCreatedAt())
                    .updatedAt(java.time.LocalDateTime.now())
                    .build();

            Item savedItem = itemRepository.save(updatedItem);
            log.info("Successfully added {} specifications to item asynchronously with ID: {}",
                    newSpecs.size(), id);
            return ItemMapper.toDto(savedItem);
        });
    }

    /**
     * Validates that an object is not null synchronously.
     * This is more efficient than async validation for simple null checks.
     *
     * @param object     the object to validate
     * @param fieldName  the name of the field for error reporting
     * @param <T>        the type of the object
     * @return the object if it is not null
     * @throws ValidationException if the object is null
     */
    private <T> T validateNotNull(T object, String fieldName) {
        if (object == null) {
            throw new ValidationException(fieldName, fieldName + " cannot be null");
        }
        return object;
    }

    /**
     * Validates business rules for an item asynchronously.
     *
     * @param item the item to validate
     * @return a CompletableFuture containing the item if the business rules are valid
     * @throws BusinessLogicException if any business rule is violated
     */
    private CompletableFuture<Item> validateBusinessRulesAsync(Item item) {
        return supplyAsync(() -> {
            validateBusinessRules(item);
            return item;
        });
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
     * Supplies an asynchronous operation using the configured virtual thread executor.
     *
     * @param supplier the supplier of the operation
     * @return a CompletableFuture containing the result of the operation
     */
    private <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }
}
