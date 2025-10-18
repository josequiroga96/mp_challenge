package com.mp.challenge.controllers.contracts;

import com.mp.challenge.components.dtos.AddSpecificationDto;
import com.mp.challenge.components.dtos.CreateItemDto;
import com.mp.challenge.components.dtos.ItemDto;
import com.mp.challenge.components.dtos.PatchItemDto;
import com.mp.challenge.components.dtos.UpdateItemDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * IItemController
 * <p>
 * Contract interface for Item Controller operations with full asynchronous support using Virtual Threads.
 * This interface defines all Item-related endpoints with comprehensive Swagger documentation
 * and maximum performance through parallelization.
 * <p>
 * Features:
 * <ul>
 *   <li>Complete CRUD operations for Items with async support</li>
 *   <li>Advanced search and filtering capabilities with parallel processing</li>
 *   <li>Asynchronous processing with Virtual Threads for high concurrency</li>
 *   <li>Batch operations for optimal performance</li>
 *   <li>Comprehensive API documentation with Swagger</li>
 *   <li>Custom exception handling with proper async propagation</li>
 *   <li>Input validation with Jakarta Bean Validation</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 16/10/2025
 */
@RequestMapping("/api/items")
@Tag(name = "Items", description = "Operations related to item management")
public interface IItemController {

    /**
     * Creates a new item.
     *
     * @param createItemDto the item data to create
     * @return the created item
     */
    @Operation(
            summary = "Create a new item",
            description = "Creates a new item with the provided data. Validates business rules and returns the created item with generated ID and timestamps."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Item data to create",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateItemDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Valid Item",
                                    summary = "Complete item data",
                                    value = """
                                            {
                                              "name": "Gaming Laptop",
                                              "description": "High-performance gaming laptop with RTX 4080",
                                              "imageUrl": "https://example.com/gaming-laptop.jpg",
                                              "price": 1999.99,
                                              "rating": 4.8
                                            }"""
                            ),
                            @ExampleObject(
                                    name = "Minimal Item",
                                    summary = "Minimal required data",
                                    value = """
                                            {
                                              "name": "Basic Item",
                                              "description": "A basic item",
                                              "price": 29.99,
                                              "rating": 3.5
                                            }"""
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Item created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ItemDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Success Response",
                                            summary = "Created item with all fields",
                                            value = """
                                                    {
                                                        "id": "123e4567-e89b-12d3-a456-426614174000",
                                                        "name": "Gaming Laptop",
                                                        "description": "High-performance gaming laptop",
                                                        "imageUrl": "https://example.com/gaming-laptop.jpg",
                                                        "price": 1999.99,
                                                        "rating": 4.8,
                                                        "createdAt": "2020-01-16T10:30:00.000Z",
                                                        "updatedAt": "2020-01-16T10:30:00.000Z"
                                                    }"""
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error - Invalid input data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ValidationException.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Missing Required Field",
                                            summary = "Name is required",
                                            value = """
                                                    {
                                                      "timestamp": "2025-10-16T10:30:00Z",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Name is required",
                                                      "errorCode": "VALIDATION_FAILED",
                                                      "path": "/api/items"
                                                    }"""
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Price",
                                            summary = "Price must be positive",
                                            value = """
                                                    {
                                                      "timestamp": "2025-10-16T10:30:00Z",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Price must be greater than 0",
                                                      "errorCode": "VALIDATION_FAILED",
                                                      "path": "/api/items"
                                                    }"""
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Business logic violation - Duplicate item name",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.BusinessLogicException.class),
                            examples = @ExampleObject(
                                    name = "Duplicate Name",
                                    summary = "Item name already exists",
                                    value = """
                                            {
                                              "timestamp": "2025-10-16T10:30:00Z",
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Item with name 'Gaming Laptop' already exists",
                                              "errorCode": "BUSINESS_LOGIC_VIOLATION",
                                              "path": "/api/items"
                                            }"""
                            )
                    )
            )
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<ItemDto>> createItem(@Valid @RequestBody CreateItemDto createItemDto);

    /**
     * Retrieves an item by ID.
     *
     * @param id the item ID
     * @return the item if found
     */
    @Operation(
            summary = "Get item by ID",
            description = "Retrieves a specific item by its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Item found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ItemDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Item not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ItemNotFoundException.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ID format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ValidationException.class)
                    )
            )
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<ItemDto>> getItemById(
            @Parameter(description = "Item unique identifier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id);

    /**
     * Retrieves all items.
     *
     * @return list of all items
     */
    @Operation(
            summary = "Get all items",
            description = "Retrieves all items in the system."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Items retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "array", implementation = ItemDto.class)
            )
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<List<ItemDto>>> getAllItems();

    /**
     * Updates an existing item.
     *
     * @param id            the item ID
     * @param updateItemDto the updated item data
     * @return the updated item
     */
    @Operation(
            summary = "Update an item",
            description = "Updates an existing item with the provided data. Validates business rules and returns the updated item."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated item data",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UpdateItemDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Update Price and Rating",
                                    summary = "Update item price and rating",
                                    value = """
                                            {
                                              "name": "Updated Gaming Laptop",
                                              "description": "Updated description with better specs",
                                              "imageUrl": "https://example.com/updated-laptop.jpg",
                                              "price": 1799.99,
                                              "rating": 4.9
                                            }"""
                            ),
                            @ExampleObject(
                                    name = "Partial Update",
                                    summary = "Update only specific fields",
                                    value = """
                                            {
                                              "price": 1599.99,
                                              "rating": 4.7
                                            }"""
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Item updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ItemDto.class),
                            examples = @ExampleObject(
                                    name = "Updated Item",
                                    summary = "Successfully updated item",
                                    value = """
                                            {
                                              "id": "123e4567-e89b-12d3-a456-426614174000",
                                              "name": "Updated Gaming Laptop",
                                              "description": "Updated description with better specs",
                                              "imageUrl": "https://example.com/updated-laptop.jpg",
                                              "price": 1799.99,
                                              "rating": 4.9,
                                              "createdAt": "2025-10-16T10:30:00Z",
                                              "updatedAt": "2025-10-16T12:45:00Z"
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Item not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ItemNotFoundException.class),
                            examples = @ExampleObject(
                                    name = "Item Not Found",
                                    summary = "Item with specified ID does not exist",
                                    value = """
                                            {
                                              "timestamp": "2025-10-16T10:30:00Z",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Item with ID '123e4567-e89b-12d3-a456-426614174000' was not found during update operation",
                                              "errorCode": "ITEM_NOT_FOUND",
                                              "path": "/api/items/123e4567-e89b-12d3-a456-426614174000"
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ValidationException.class),
                            examples = @ExampleObject(
                                    name = "Invalid Rating",
                                    summary = "Rating out of valid range",
                                    value = """
                                            {
                                              "timestamp": "2025-10-16T10:30:00Z",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Rating must be between 0.0 and 5.0",
                                              "errorCode": "VALIDATION_FAILED",
                                              "path": "/api/items/123e4567-e89b-12d3-a456-426614174000"
                                            }"""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Business logic violation",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.BusinessLogicException.class),
                            examples = @ExampleObject(
                                    name = "Duplicate Name",
                                    summary = "Updated name already exists",
                                    value = """
                                            {
                                              "timestamp": "2025-10-16T10:30:00Z",
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Item with name 'Updated Gaming Laptop' already exists",
                                              "errorCode": "BUSINESS_LOGIC_VIOLATION",
                                              "path": "/api/items/123e4567-e89b-12d3-a456-426614174000"
                                            }"""
                            )
                    )
            )
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<ItemDto>> updateItem(
            @Parameter(description = "Item unique identifier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateItemDto updateItemDto);

    /**
     * Deletes an item by ID.
     *
     * @param id the item ID
     * @return no content if successful
     */
    @Operation(
            summary = "Delete an item",
            description = "Deletes an item by its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Item deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Item not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ItemNotFoundException.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ID format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ValidationException.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    CompletableFuture<ResponseEntity<Void>> deleteItem(
            @Parameter(description = "Item unique identifier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id);

    /**
     * Searches items by name.
     *
     * @param name the search term
     * @return list of matching items
     */
    @Operation(
            summary = "Search items by name",
            description = "Searches for items by name using case-insensitive partial matching."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "array", implementation = ItemDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Found Items",
                                            summary = "Multiple items found",
                                            value = """
                                                    [
                                                      {
                                                        "id": "123e4567-e89b-12d3-a456-426614174000",
                                                        "name": "Gaming Laptop",
                                                        "description": "High-performance gaming laptop",
                                                        "imageUrl": "https://example.com/laptop.jpg",
                                                        "price": 1999.99,
                                                        "rating": 4.8,
                                                        "createdAt": "2025-10-16T10:30:00Z",
                                                        "updatedAt": "2025-10-16T10:30:00Z"
                                                      },
                                                      {
                                                        "id": "456e7890-e89b-12d3-a456-426614174001",
                                                        "name": "Gaming Mouse",
                                                        "description": "High-precision gaming mouse",
                                                        "imageUrl": "https://example.com/mouse.jpg",
                                                        "price": 79.99,
                                                        "rating": 4.5,
                                                        "createdAt": "2025-10-16T11:00:00Z",
                                                        "updatedAt": "2025-10-16T11:00:00Z"
                                                      }
                                                    ]"""
                                    ),
                                    @ExampleObject(
                                            name = "No Results",
                                            summary = "No items found",
                                            value = "[]"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search parameter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ValidationException.class),
                            examples = @ExampleObject(
                                    name = "Empty Search Term",
                                    summary = "Search term cannot be empty",
                                    value = """
                                            {
                                              "timestamp": "2025-10-16T10:30:00Z",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Search term cannot be empty",
                                              "errorCode": "VALIDATION_FAILED",
                                              "path": "/api/items/search"
                                            }"""
                            )
                    )
            )
    })
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<List<ItemDto>>> searchItemsByName(
            @Parameter(description = "Search term for item name", required = true, example = "laptop")
            @RequestParam String name);

    /**
     * Searches items by price range.
     *
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @return list of items within price range
     */
    @Operation(
            summary = "Search items by price range",
            description = "Searches for items within a specified price range (inclusive)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "array", implementation = ItemDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Items in Range",
                                            summary = "Items found in price range",
                                            value = """
                                                    [
                                                      {
                                                        "id": "123e4567-e89b-12d3-a456-426614174000",
                                                        "name": "Budget Laptop",
                                                        "description": "Affordable laptop for everyday use",
                                                        "imageUrl": "https://example.com/budget-laptop.jpg",
                                                        "price": 599.99,
                                                        "rating": 4.2,
                                                        "createdAt": "2025-10-16T10:30:00Z",
                                                        "updatedAt": "2025-10-16T10:30:00Z"
                                                      },
                                                      {
                                                        "id": "456e7890-e89b-12d3-a456-426614174001",
                                                        "name": "Mid-range Laptop",
                                                        "description": "Balanced performance and price",
                                                        "imageUrl": "https://example.com/mid-laptop.jpg",
                                                        "price": 899.99,
                                                        "rating": 4.6,
                                                        "createdAt": "2025-10-16T11:00:00Z",
                                                        "updatedAt": "2025-10-16T11:00:00Z"
                                                      }
                                                    ]"""
                                    ),
                                    @ExampleObject(
                                            name = "No Items in Range",
                                            summary = "No items found in price range",
                                            value = "[]"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid price parameters",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ValidationException.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid Range",
                                            summary = "Min price greater than max price",
                                            value = """
                                                    {
                                                      "timestamp": "2025-10-16T10:30:00Z",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Minimum price cannot be greater than maximum price",
                                                      "errorCode": "VALIDATION_FAILED",
                                                      "path": "/api/items/search/price"
                                                    }"""
                                    ),
                                    @ExampleObject(
                                            name = "Negative Price",
                                            summary = "Price cannot be negative",
                                            value = """
                                                    {
                                                      "timestamp": "2025-10-16T10:30:00Z",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Price must be greater than or equal to 0",
                                                      "errorCode": "VALIDATION_FAILED",
                                                      "path": "/api/items/search/price"
                                                    }"""
                                    )
                            }
                    )
            )
    })
    @GetMapping(value = "/search/price", produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<List<ItemDto>>> searchItemsByPriceRange(
            @Parameter(description = "Minimum price", required = true, example = "10.00")
            @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price", required = true, example = "100.00")
            @RequestParam BigDecimal maxPrice);

    /**
     * Searches items by minimum rating.
     *
     * @param minRating minimum rating
     * @return list of items with rating >= minRating
     */
    @Operation(
            summary = "Search items by minimum rating",
            description = "Searches for items with a rating greater than or equal to the specified minimum rating."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "array", implementation = ItemDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid rating parameter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ValidationException.class)
                    )
            )
    })
    @GetMapping(value = "/search/rating", produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<List<ItemDto>>> searchItemsByMinimumRating(
            @Parameter(description = "Minimum rating", required = true, example = "4.0")
            @RequestParam BigDecimal minRating);

    /**
     * Gets the total count of items.
     *
     * @return the item count
     */
    @Operation(
            summary = "Get item count",
            description = "Returns the total number of items in the system."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Count retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "integer", example = "42")
            )
    )
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<Long>> getItemCount();

    /**
     * Checks if an item exists by ID.
     *
     * @param id the item ID
     * @return a Boolean, true if item exists, false otherwise
     */
    @Operation(
            summary = "Check if item exists",
            description = "Checks whether an item with the specified ID exists in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Check completed successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "boolean", example = "true")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ID format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ValidationException.class)
                    )
            )
    })
    @GetMapping(value = "/{id}/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<Boolean>> itemExists(
            @Parameter(description = "Item unique identifier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id);

    @Operation(
            summary = "Patch item",
            description = "Partially updates an existing item. Only provided fields will be updated, others remain unchanged.",
            operationId = "patchItem"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Item successfully patched",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ItemDto.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = "{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"name\":\"Updated Product\",\"price\":99.99,\"rating\":4.5}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ValidationException.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Item not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ItemNotFoundException.class)
                    )
            )
    })
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<ItemDto>> patchItem(
            @Parameter(description = "Item unique identifier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Item data for partial update",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PatchItemDto.class),
                            examples = @ExampleObject(
                                    name = "Patch Example",
                                    value = "{\"name\":\"Updated Product Name\",\"price\":99.99}"
                            )
                    )
            )
            @Valid @RequestBody PatchItemDto patchItemDto);

    @Operation(
            summary = "Add specifications to item",
            description = "Adds new specifications to an existing item without replacing existing ones.",
            operationId = "addSpecification"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Specifications successfully added",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ItemDto.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = "{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"name\":\"Product\",\"specifications\":[\"Wireless\",\"Bluetooth 5.0\",\"Water Resistant\"]}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ValidationException.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Item not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ItemNotFoundException.class)
                    )
            )
    })
    @PostMapping(value = "/{id}/specifications", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<ItemDto>> addSpecification(
            @Parameter(description = "Item unique identifier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Specifications to add",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AddSpecificationDto.class),
                            examples = @ExampleObject(
                                    name = "Add Specifications Example",
                                    value = "{\"specifications\":[\"Wireless\",\"Bluetooth 5.0\",\"Water Resistant\"]}"
                            )
                    )
            )
            @Valid @RequestBody AddSpecificationDto addSpecificationDto);

    /**
     * Retrieves multiple items by their IDs.
     *
     * @param ids list of item IDs
     * @return list of items found
     */
    @Operation(
            summary = "Get items by IDs",
            description = "Retrieves multiple items by their unique identifiers. Returns only the items that exist."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Items retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "array", implementation = ItemDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Found Items",
                                            summary = "Multiple items found",
                                            value = """
                                                    [
                                                      {
                                                        "id": "123e4567-e89b-12d3-a456-426614174000",
                                                        "name": "Gaming Laptop",
                                                        "description": "High-performance gaming laptop",
                                                        "imageUrl": "https://example.com/laptop.jpg",
                                                        "price": 1999.99,
                                                        "rating": 4.8,
                                                        "createdAt": "2025-10-16T10:30:00Z",
                                                        "updatedAt": "2025-10-16T10:30:00Z"
                                                      },
                                                      {
                                                        "id": "456e7890-e89b-12d3-a456-426614174001",
                                                        "name": "Gaming Mouse",
                                                        "description": "High-precision gaming mouse",
                                                        "imageUrl": "https://example.com/mouse.jpg",
                                                        "price": 79.99,
                                                        "rating": 4.5,
                                                        "createdAt": "2025-10-16T11:00:00Z",
                                                        "updatedAt": "2025-10-16T11:00:00Z"
                                                      }
                                                    ]"""
                                    ),
                                    @ExampleObject(
                                            name = "No Results",
                                            summary = "No items found",
                                            value = "[]"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.mp.challenge.components.exceptions.ValidationException.class)
                    )
            )
    })
    @PostMapping(value = "/batch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CompletableFuture<ResponseEntity<List<ItemDto>>> getItemsByIds(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of item IDs to retrieve",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "array", implementation = UUID.class),
                            examples = @ExampleObject(
                                    name = "Item IDs Example",
                                    value = "[\"123e4567-e89b-12d3-a456-426614174000\", \"456e7890-e89b-12d3-a456-426614174001\"]"
                            )
                    )
            )
            @RequestBody List<UUID> ids);
}
