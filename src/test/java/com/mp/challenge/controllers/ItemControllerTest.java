package com.mp.challenge.controllers;

import com.mp.challenge.components.dtos.*;
import com.mp.challenge.components.exceptions.BusinessLogicException;
import com.mp.challenge.components.exceptions.ItemNotFoundException;
import com.mp.challenge.components.exceptions.ValidationException;
import com.mp.challenge.services.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ItemControllerTest
 * <p>
 * Comprehensive test suite for ItemController covering all endpoints and scenarios.
 * Tests both successful operations and error handling with proper async testing.
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 17/10/2025
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ItemController Tests")
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private ItemDto sampleItemDto;
    private CreateItemDto sampleCreateItemDto;
    private UpdateItemDto sampleUpdateItemDto;
    private PatchItemDto samplePatchItemDto;
    private AddSpecificationDto sampleAddSpecificationDto;
    private UUID sampleItemId;

    @BeforeEach
    void setUp() {
        sampleItemId = UUID.randomUUID();
        
        sampleItemDto = ItemDto.builder()
                .id(sampleItemId)
                .name("Test Item")
                .description("Test Description")
                .imageUrl("https://example.com/image.jpg")
                .price(new BigDecimal("99.99"))
                .rating(new BigDecimal("4.5"))
                .specifications(List.of("Wireless", "Bluetooth"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleCreateItemDto = CreateItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .imageUrl("https://example.com/image.jpg")
                .price(new BigDecimal("99.99"))
                .rating(new BigDecimal("4.5"))
                .specifications(List.of("Wireless", "Bluetooth"))
                .build();

        sampleUpdateItemDto = UpdateItemDto.builder()
                .id(sampleItemId)
                .name("Updated Item")
                .description("Updated Description")
                .imageUrl("https://example.com/updated-image.jpg")
                .price(new BigDecimal("149.99"))
                .rating(new BigDecimal("4.8"))
                .specifications(List.of("Wireless", "Bluetooth", "Water Resistant"))
                .build();

        samplePatchItemDto = PatchItemDto.builder()
                .name("Patched Item")
                .price(new BigDecimal("79.99"))
                .build();

        sampleAddSpecificationDto = AddSpecificationDto.builder()
                .specifications(List.of("New Feature", "Enhanced Quality"))
                .build();
    }

    // ==================== CREATE ITEM TESTS ====================

    @Test
    @DisplayName("createItem - Should create item successfully")
    void createItem_ShouldCreateItemSuccessfully() {
        // Given
        when(itemService.createItem(any(CreateItemDto.class)))
                .thenReturn(CompletableFuture.completedFuture(sampleItemDto));

        // When
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.createItem(sampleCreateItemDto);

        // Then
        ResponseEntity<ItemDto> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(sampleItemDto);
        verify(itemService).createItem(sampleCreateItemDto);
    }

    @Test
    @DisplayName("createItem - Should handle ValidationException")
    void createItem_ShouldHandleValidationException() {
        // Given
        ValidationException exception = new ValidationException("name", "Name is required");
        when(itemService.createItem(any(CreateItemDto.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.createItem(sampleCreateItemDto);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).createItem(sampleCreateItemDto);
    }

    @Test
    @DisplayName("createItem - Should handle BusinessLogicException")
    void createItem_ShouldHandleBusinessLogicException() {
        // Given
        BusinessLogicException exception = new BusinessLogicException("Item name already exists");
        when(itemService.createItem(any(CreateItemDto.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.createItem(sampleCreateItemDto);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).createItem(sampleCreateItemDto);
    }

    // ==================== GET ITEM BY ID TESTS ====================

    @Test
    @DisplayName("getItemById - Should retrieve item successfully")
    void getItemById_ShouldRetrieveItemSuccessfully() {
        // Given
        when(itemService.getItem(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(sampleItemDto));

        // When
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.getItemById(sampleItemId);

        // Then
        ResponseEntity<ItemDto> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleItemDto);
        verify(itemService).getItem(sampleItemId);
    }

    @Test
    @DisplayName("getItemById - Should handle ItemNotFoundException")
    void getItemById_ShouldHandleItemNotFoundException() {
        // Given
        ItemNotFoundException exception = new ItemNotFoundException(sampleItemId, "get operation");
        when(itemService.getItem(any(UUID.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.getItemById(sampleItemId);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).getItem(sampleItemId);
    }

    // ==================== GET ALL ITEMS TESTS ====================

    @Test
    @DisplayName("getAllItems - Should retrieve all items successfully")
    void getAllItems_ShouldRetrieveAllItemsSuccessfully() {
        // Given
        List<ItemDto> items = List.of(sampleItemDto);
        when(itemService.getAllItems())
                .thenReturn(CompletableFuture.completedFuture(items));

        // When
        CompletableFuture<ResponseEntity<List<ItemDto>>> result = itemController.getAllItems();

        // Then
        ResponseEntity<List<ItemDto>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(items);
        verify(itemService).getAllItems();
    }

    @Test
    @DisplayName("getAllItems - Should return empty list when no items exist")
    void getAllItems_ShouldReturnEmptyListWhenNoItemsExist() {
        // Given
        when(itemService.getAllItems())
                .thenReturn(CompletableFuture.completedFuture(List.of()));

        // When
        CompletableFuture<ResponseEntity<List<ItemDto>>> result = itemController.getAllItems();

        // Then
        ResponseEntity<List<ItemDto>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(itemService).getAllItems();
    }

    // ==================== UPDATE ITEM TESTS ====================

    @Test
    @DisplayName("updateItem - Should update item successfully")
    void updateItem_ShouldUpdateItemSuccessfully() {
        // Given
        when(itemService.updateItem(any(UUID.class), any(UpdateItemDto.class)))
                .thenReturn(CompletableFuture.completedFuture(sampleItemDto));

        // When
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.updateItem(sampleItemId, sampleUpdateItemDto);

        // Then
        ResponseEntity<ItemDto> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleItemDto);
        verify(itemService).updateItem(sampleItemId, sampleUpdateItemDto);
    }

    @Test
    @DisplayName("updateItem - Should handle ItemNotFoundException")
    void updateItem_ShouldHandleItemNotFoundException() {
        // Given
        ItemNotFoundException exception = new ItemNotFoundException(sampleItemId, "update operation");
        when(itemService.updateItem(any(UUID.class), any(UpdateItemDto.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.updateItem(sampleItemId, sampleUpdateItemDto);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).updateItem(sampleItemId, sampleUpdateItemDto);
    }

    @Test
    @DisplayName("updateItem - Should handle BusinessLogicException")
    void updateItem_ShouldHandleBusinessLogicException() {
        // Given
        BusinessLogicException exception = new BusinessLogicException("Item name already exists");
        when(itemService.updateItem(any(UUID.class), any(UpdateItemDto.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.updateItem(sampleItemId, sampleUpdateItemDto);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).updateItem(sampleItemId, sampleUpdateItemDto);
    }

    // ==================== DELETE ITEM TESTS ====================

    @Test
    @DisplayName("deleteItem - Should delete item successfully")
    void deleteItem_ShouldDeleteItemSuccessfully() {
        // Given
        when(itemService.deleteItem(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(true));

        // When
        CompletableFuture<ResponseEntity<Void>> result = itemController.deleteItem(sampleItemId);

        // Then
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(itemService).deleteItem(sampleItemId);
    }

    @Test
    @DisplayName("deleteItem - Should return not found when item does not exist")
    void deleteItem_ShouldReturnNotFoundWhenItemDoesNotExist() {
        // Given
        when(itemService.deleteItem(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(false));

        // When
        CompletableFuture<ResponseEntity<Void>> result = itemController.deleteItem(sampleItemId);

        // Then
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(itemService).deleteItem(sampleItemId);
    }

    // ==================== SEARCH ITEMS BY NAME TESTS ====================

    @Test
    @DisplayName("searchItemsByName - Should find items by name successfully")
    void searchItemsByName_ShouldFindItemsByNameSuccessfully() {
        // Given
        List<ItemDto> items = List.of(sampleItemDto);
        when(itemService.findItemsByName(anyString()))
                .thenReturn(CompletableFuture.completedFuture(items));

        // When
        CompletableFuture<ResponseEntity<List<ItemDto>>> result = itemController.searchItemsByName("test");

        // Then
        ResponseEntity<List<ItemDto>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(items);
        verify(itemService).findItemsByName("test");
    }

    @Test
    @DisplayName("searchItemsByName - Should return empty list when no items found")
    void searchItemsByName_ShouldReturnEmptyListWhenNoItemsFound() {
        // Given
        when(itemService.findItemsByName(anyString()))
                .thenReturn(CompletableFuture.completedFuture(List.of()));

        // When
        CompletableFuture<ResponseEntity<List<ItemDto>>> result = itemController.searchItemsByName("nonexistent");

        // Then
        ResponseEntity<List<ItemDto>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(itemService).findItemsByName("nonexistent");
    }

    @Test
    @DisplayName("searchItemsByName - Should handle ValidationException")
    void searchItemsByName_ShouldHandleValidationException() {
        // Given
        ValidationException exception = new ValidationException("name", "Search term cannot be empty");
        when(itemService.findItemsByName(anyString()))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<List<ItemDto>>> result = itemController.searchItemsByName("");
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).findItemsByName("");
    }

    // ==================== SEARCH ITEMS BY PRICE RANGE TESTS ====================

    @Test
    @DisplayName("searchItemsByPriceRange - Should find items by price range successfully")
    void searchItemsByPriceRange_ShouldFindItemsByPriceRangeSuccessfully() {
        // Given
        List<ItemDto> items = List.of(sampleItemDto);
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("150.00");
        when(itemService.findItemsByPriceRange(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(CompletableFuture.completedFuture(items));

        // When
        CompletableFuture<ResponseEntity<List<ItemDto>>> result = itemController.searchItemsByPriceRange(minPrice, maxPrice);

        // Then
        ResponseEntity<List<ItemDto>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(items);
        verify(itemService).findItemsByPriceRange(minPrice, maxPrice);
    }

    @Test
    @DisplayName("searchItemsByPriceRange - Should handle ValidationException")
    void searchItemsByPriceRange_ShouldHandleValidationException() {
        // Given
        ValidationException exception = new ValidationException("minPrice", "Minimum price cannot be greater than maximum price");
        BigDecimal minPrice = new BigDecimal("150.00");
        BigDecimal maxPrice = new BigDecimal("50.00");
        when(itemService.findItemsByPriceRange(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<List<ItemDto>>> result = itemController.searchItemsByPriceRange(minPrice, maxPrice);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).findItemsByPriceRange(minPrice, maxPrice);
    }

    // ==================== SEARCH ITEMS BY MINIMUM RATING TESTS ====================

    @Test
    @DisplayName("searchItemsByMinimumRating - Should find items by minimum rating successfully")
    void searchItemsByMinimumRating_ShouldFindItemsByMinimumRatingSuccessfully() {
        // Given
        List<ItemDto> items = List.of(sampleItemDto);
        BigDecimal minRating = new BigDecimal("4.0");
        when(itemService.findItemsByMinimumRating(any(BigDecimal.class)))
                .thenReturn(CompletableFuture.completedFuture(items));

        // When
        CompletableFuture<ResponseEntity<List<ItemDto>>> result = itemController.searchItemsByMinimumRating(minRating);

        // Then
        ResponseEntity<List<ItemDto>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(items);
        verify(itemService).findItemsByMinimumRating(minRating);
    }

    @Test
    @DisplayName("searchItemsByMinimumRating - Should handle ValidationException")
    void searchItemsByMinimumRating_ShouldHandleValidationException() {
        // Given
        ValidationException exception = new ValidationException("minRating", "Rating must be between 0.0 and 5.0");
        BigDecimal minRating = new BigDecimal("6.0");
        when(itemService.findItemsByMinimumRating(any(BigDecimal.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<List<ItemDto>>> result = itemController.searchItemsByMinimumRating(minRating);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).findItemsByMinimumRating(minRating);
    }

    // ==================== GET ITEM COUNT TESTS ====================

    @Test
    @DisplayName("getItemCount - Should return item count successfully")
    void getItemCount_ShouldReturnItemCountSuccessfully() {
        // Given
        Long count = 5L;
        when(itemService.getItemCount())
                .thenReturn(CompletableFuture.completedFuture(count));

        // When
        CompletableFuture<ResponseEntity<Long>> result = itemController.getItemCount();

        // Then
        ResponseEntity<Long> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(count);
        verify(itemService).getItemCount();
    }

    @Test
    @DisplayName("getItemCount - Should return zero when no items exist")
    void getItemCount_ShouldReturnZeroWhenNoItemsExist() {
        // Given
        when(itemService.getItemCount())
                .thenReturn(CompletableFuture.completedFuture(0L));

        // When
        CompletableFuture<ResponseEntity<Long>> result = itemController.getItemCount();

        // Then
        ResponseEntity<Long> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(0L);
        verify(itemService).getItemCount();
    }

    // ==================== ITEM EXISTS TESTS ====================

    @Test
    @DisplayName("itemExists - Should return true when item exists")
    void itemExists_ShouldReturnTrueWhenItemExists() {
        // Given
        when(itemService.itemExists(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(true));

        // When
        CompletableFuture<ResponseEntity<Boolean>> result = itemController.itemExists(sampleItemId);

        // Then
        ResponseEntity<Boolean> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
        verify(itemService).itemExists(sampleItemId);
    }

    @Test
    @DisplayName("itemExists - Should return false when item does not exist")
    void itemExists_ShouldReturnFalseWhenItemDoesNotExist() {
        // Given
        when(itemService.itemExists(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(false));

        // When
        CompletableFuture<ResponseEntity<Boolean>> result = itemController.itemExists(sampleItemId);

        // Then
        ResponseEntity<Boolean> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isFalse();
        verify(itemService).itemExists(sampleItemId);
    }

    @Test
    @DisplayName("itemExists - Should handle ValidationException")
    void itemExists_ShouldHandleValidationException() {
        // Given
        ValidationException exception = new ValidationException("id", "ID cannot be null");
        when(itemService.itemExists(any(UUID.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<Boolean>> result = itemController.itemExists(sampleItemId);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).itemExists(sampleItemId);
    }

    // ==================== PATCH ITEM TESTS ====================

    @Test
    @DisplayName("patchItem - Should patch item successfully")
    void patchItem_ShouldPatchItemSuccessfully() {
        // Given
        when(itemService.patchItem(any(UUID.class), any(PatchItemDto.class)))
                .thenReturn(CompletableFuture.completedFuture(sampleItemDto));

        // When
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.patchItem(sampleItemId, samplePatchItemDto);

        // Then
        ResponseEntity<ItemDto> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleItemDto);
        verify(itemService).patchItem(sampleItemId, samplePatchItemDto);
    }

    @Test
    @DisplayName("patchItem - Should handle ItemNotFoundException")
    void patchItem_ShouldHandleItemNotFoundException() {
        // Given
        ItemNotFoundException exception = new ItemNotFoundException(sampleItemId, "patch operation");
        when(itemService.patchItem(any(UUID.class), any(PatchItemDto.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.patchItem(sampleItemId, samplePatchItemDto);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).patchItem(sampleItemId, samplePatchItemDto);
    }

    @Test
    @DisplayName("patchItem - Should handle ValidationException")
    void patchItem_ShouldHandleValidationException() {
        // Given
        ValidationException exception = new ValidationException("price", "Price must be greater than 0");
        when(itemService.patchItem(any(UUID.class), any(PatchItemDto.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.patchItem(sampleItemId, samplePatchItemDto);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).patchItem(sampleItemId, samplePatchItemDto);
    }

    @Test
    @DisplayName("patchItem - Should handle BusinessLogicException")
    void patchItem_ShouldHandleBusinessLogicException() {
        // Given
        BusinessLogicException exception = new BusinessLogicException("Item name already exists");
        when(itemService.patchItem(any(UUID.class), any(PatchItemDto.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.patchItem(sampleItemId, samplePatchItemDto);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).patchItem(sampleItemId, samplePatchItemDto);
    }

    // ==================== ADD SPECIFICATION TESTS ====================

    @Test
    @DisplayName("addSpecification - Should add specifications successfully")
    void addSpecification_ShouldAddSpecificationsSuccessfully() {
        // Given
        when(itemService.addSpecification(any(UUID.class), any(AddSpecificationDto.class)))
                .thenReturn(CompletableFuture.completedFuture(sampleItemDto));

        // When
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.addSpecification(sampleItemId, sampleAddSpecificationDto);

        // Then
        ResponseEntity<ItemDto> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleItemDto);
        verify(itemService).addSpecification(sampleItemId, sampleAddSpecificationDto);
    }

    @Test
    @DisplayName("addSpecification - Should handle ItemNotFoundException")
    void addSpecification_ShouldHandleItemNotFoundException() {
        // Given
        ItemNotFoundException exception = new ItemNotFoundException(sampleItemId, "add specification operation");
        when(itemService.addSpecification(any(UUID.class), any(AddSpecificationDto.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.addSpecification(sampleItemId, sampleAddSpecificationDto);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).addSpecification(sampleItemId, sampleAddSpecificationDto);
    }

    @Test
    @DisplayName("addSpecification - Should handle ValidationException")
    void addSpecification_ShouldHandleValidationException() {
        // Given
        ValidationException exception = new ValidationException("specifications", "Must provide between 1 and 10 specifications");
        when(itemService.addSpecification(any(UUID.class), any(AddSpecificationDto.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.addSpecification(sampleItemId, sampleAddSpecificationDto);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).addSpecification(sampleItemId, sampleAddSpecificationDto);
    }

    @Test
    @DisplayName("addSpecification - Should handle BusinessLogicException")
    void addSpecification_ShouldHandleBusinessLogicException() {
        // Given
        BusinessLogicException exception = new BusinessLogicException("Cannot add specifications: would exceed maximum limit of 20");
        when(itemService.addSpecification(any(UUID.class), any(AddSpecificationDto.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<ItemDto>> result = itemController.addSpecification(sampleItemId, sampleAddSpecificationDto);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).addSpecification(sampleItemId, sampleAddSpecificationDto);
    }

    // ==================== GET ITEMS BY IDS TESTS ====================

    @Test
    @DisplayName("getItemsByIds - Should retrieve items by IDs successfully")
    void getItemsByIds_ShouldRetrieveItemsByIdsSuccessfully() {
        // Given
        List<UUID> ids = List.of(sampleItemId, UUID.randomUUID());
        List<ItemDto> items = List.of(sampleItemDto);
        when(itemService.getItemsByIds(anyList()))
                .thenReturn(CompletableFuture.completedFuture(items));

        // When
        CompletableFuture<ResponseEntity<List<ItemDto>>> result = itemController.getItemsByIds(ids);

        // Then
        ResponseEntity<List<ItemDto>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(items);
        verify(itemService).getItemsByIds(ids);
    }

    @Test
    @DisplayName("getItemsByIds - Should return empty list when no items found")
    void getItemsByIds_ShouldReturnEmptyListWhenNoItemsFound() {
        // Given
        List<UUID> ids = List.of(UUID.randomUUID());
        when(itemService.getItemsByIds(anyList()))
                .thenReturn(CompletableFuture.completedFuture(List.of()));

        // When
        CompletableFuture<ResponseEntity<List<ItemDto>>> result = itemController.getItemsByIds(ids);

        // Then
        ResponseEntity<List<ItemDto>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(itemService).getItemsByIds(ids);
    }

    @Test
    @DisplayName("getItemsByIds - Should handle ValidationException")
    void getItemsByIds_ShouldHandleValidationException() {
        // Given
        List<UUID> ids = List.of();
        ValidationException exception = new ValidationException("ids", "IDs list cannot be empty");
        when(itemService.getItemsByIds(anyList()))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When & Then
        CompletableFuture<ResponseEntity<List<ItemDto>>> result = itemController.getItemsByIds(ids);
        assertThatThrownBy(() -> result.join())
                .hasCause(exception);
        verify(itemService).getItemsByIds(ids);
    }

    // ==================== EDGE CASES AND INTEGRATION TESTS ====================

    @Test
    @DisplayName("All methods should handle service exceptions gracefully")
    void allMethods_ShouldHandleServiceExceptionsGracefully() {
        // Test createItem with validation error
        when(itemService.createItem(any(CreateItemDto.class)))
                .thenReturn(CompletableFuture.failedFuture(new ValidationException("name", "Name is required")));
        
        CompletableFuture<ResponseEntity<ItemDto>> createResult = itemController.createItem(sampleCreateItemDto);
        assertThatThrownBy(() -> createResult.join())
                .hasCauseInstanceOf(ValidationException.class);

        // Test getItemById with validation error
        when(itemService.getItem(any(UUID.class)))
                .thenReturn(CompletableFuture.failedFuture(new ValidationException("id", "ID cannot be null")));
        
        CompletableFuture<ResponseEntity<ItemDto>> getResult = itemController.getItemById(sampleItemId);
        assertThatThrownBy(() -> getResult.join())
                .hasCauseInstanceOf(ValidationException.class);

        // Test searchItemsByName with validation error
        when(itemService.findItemsByName(anyString()))
                .thenReturn(CompletableFuture.failedFuture(new ValidationException("name", "Search term cannot be empty")));
        
        CompletableFuture<ResponseEntity<List<ItemDto>>> searchResult = itemController.searchItemsByName("test");
        assertThatThrownBy(() -> searchResult.join())
                .hasCauseInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Controller should properly delegate all calls to service layer")
    void controller_ShouldProperlyDelegateAllCallsToServiceLayer() {
        // Given
        when(itemService.createItem(any())).thenReturn(CompletableFuture.completedFuture(sampleItemDto));
        when(itemService.getItem(any())).thenReturn(CompletableFuture.completedFuture(sampleItemDto));
        when(itemService.getAllItems()).thenReturn(CompletableFuture.completedFuture(List.of(sampleItemDto)));
        when(itemService.updateItem(any(), any())).thenReturn(CompletableFuture.completedFuture(sampleItemDto));
        when(itemService.deleteItem(any())).thenReturn(CompletableFuture.completedFuture(true));
        when(itemService.findItemsByName(any())).thenReturn(CompletableFuture.completedFuture(List.of(sampleItemDto)));
        when(itemService.findItemsByPriceRange(any(), any())).thenReturn(CompletableFuture.completedFuture(List.of(sampleItemDto)));
        when(itemService.findItemsByMinimumRating(any())).thenReturn(CompletableFuture.completedFuture(List.of(sampleItemDto)));
        when(itemService.getItemCount()).thenReturn(CompletableFuture.completedFuture(1L));
        when(itemService.itemExists(any())).thenReturn(CompletableFuture.completedFuture(true));
        when(itemService.patchItem(any(), any())).thenReturn(CompletableFuture.completedFuture(sampleItemDto));
        when(itemService.addSpecification(any(), any())).thenReturn(CompletableFuture.completedFuture(sampleItemDto));
        when(itemService.getItemsByIds(any())).thenReturn(CompletableFuture.completedFuture(List.of(sampleItemDto)));

        // When & Then - Verify all methods delegate to service
        itemController.createItem(sampleCreateItemDto).join();
        itemController.getItemById(sampleItemId).join();
        itemController.getAllItems().join();
        itemController.updateItem(sampleItemId, sampleUpdateItemDto).join();
        itemController.deleteItem(sampleItemId).join();
        itemController.searchItemsByName("test").join();
        itemController.searchItemsByPriceRange(new BigDecimal("10"), new BigDecimal("100")).join();
        itemController.searchItemsByMinimumRating(new BigDecimal("4.0")).join();
        itemController.getItemCount().join();
        itemController.itemExists(sampleItemId).join();
        itemController.patchItem(sampleItemId, samplePatchItemDto).join();
        itemController.addSpecification(sampleItemId, sampleAddSpecificationDto).join();
        itemController.getItemsByIds(List.of(sampleItemId)).join();

        // Verify all service methods were called
        verify(itemService).createItem(sampleCreateItemDto);
        verify(itemService).getItem(sampleItemId);
        verify(itemService).getAllItems();
        verify(itemService).updateItem(sampleItemId, sampleUpdateItemDto);
        verify(itemService).deleteItem(sampleItemId);
        verify(itemService).findItemsByName("test");
        verify(itemService).findItemsByPriceRange(new BigDecimal("10"), new BigDecimal("100"));
        verify(itemService).findItemsByMinimumRating(new BigDecimal("4.0"));
        verify(itemService).getItemCount();
        verify(itemService).itemExists(sampleItemId);
        verify(itemService).patchItem(sampleItemId, samplePatchItemDto);
        verify(itemService).addSpecification(sampleItemId, sampleAddSpecificationDto);
        verify(itemService).getItemsByIds(List.of(sampleItemId));
    }
}
