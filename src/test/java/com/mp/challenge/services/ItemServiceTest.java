package com.mp.challenge.services;

import com.mp.challenge.components.dtos.AddSpecificationDto;
import com.mp.challenge.components.dtos.CreateItemDto;
import com.mp.challenge.components.dtos.ItemDto;
import com.mp.challenge.components.dtos.PatchItemDto;
import com.mp.challenge.components.dtos.UpdateItemDto;
import com.mp.challenge.components.exceptions.BusinessLogicException;
import com.mp.challenge.components.exceptions.ItemNotFoundException;
import com.mp.challenge.components.exceptions.ValidationException;
import com.mp.challenge.components.mappers.ItemMapper;
import com.mp.challenge.models.Item;
import com.mp.challenge.repositories.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ItemServiceTest
 * <p>
 * Comprehensive test suite for ItemService class with full asynchronous support.
 * This test class verifies all business logic, validation, and integration aspects
 * of the ItemService including Virtual Threads and parallelization features.
 * <p>
 * Test coverage includes:
 * <ul>
 *   <li>CRUD operations with proper validation and async support</li>
 *   <li>Business rule enforcement in async context</li>
 *   <li>Exception handling scenarios with CompletableFuture</li>
 *   <li>Search and filtering functionality with parallel processing</li>
 *   <li>Batch operations and advanced parallelization</li>
 *   <li>Virtual Threads integration and performance</li>
 *   <li>Logging verification in async context</li>
 * </ul>
 * <p>
 * This test suite was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 16/10/2025
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ItemService Tests")
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;
    
    @Mock
    private TaskExecutor taskExecutor;

    @InjectMocks
    private ItemService itemService;

    private Item testItem;
    private ItemDto testItemDto;
    private CreateItemDto testCreateItemDto;
    private UpdateItemDto testUpdateItemDto;
    private UUID testItemId;

    @BeforeEach
    void setUp() {
        testItemId = UUID.randomUUID();
        
        // Configure TaskExecutor mock to execute tasks immediately (lenient to avoid unnecessary stubbing errors)
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));
        
        testItem = Item.builder()
                .id(testItemId)
                .name("Test Item")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .rating(new BigDecimal("4.5"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testItemDto = ItemDto.builder()
                .id(testItemId)
                .name("Test Item")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .rating(new BigDecimal("4.5"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCreateItemDto = CreateItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .rating(new BigDecimal("4.5"))
                .build();

        testUpdateItemDto = UpdateItemDto.builder()
                .id(testItemId)
                .name("Updated Item")
                .description("Updated Description")
                .price(new BigDecimal("149.99"))
                .rating(new BigDecimal("4.8"))
                .build();
    }
    
    /**
     * Helper method to extract result from CompletableFuture and handle exceptions
     */
    private <T> T getResult(CompletableFuture<T> future) throws Exception {
        return future.get();
    }
    
    /**
     * Helper method to assert that a CompletableFuture throws a specific exception
     */
    private void assertAsyncException(CompletableFuture<?> future, Class<? extends Exception> expectedException) {
        assertThatThrownBy(() -> future.get())
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(expectedException);
    }

    @Nested
    @DisplayName("createItem Tests")
    class CreateItemTests {

        @Test
        @DisplayName("Should create item successfully when valid DTO provided")
        void createItem_ShouldCreateItem_WhenValidDtoProvided() throws Exception {
            // Given
            when(itemRepository.findAll()).thenReturn(List.of());
            when(itemRepository.save(any(Item.class))).thenReturn(testItem);

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toEntity(testCreateItemDto)).thenReturn(testItem);
                mockedMapper.when(() -> ItemMapper.toDto(testItem)).thenReturn(testItemDto);

                // When
                CompletableFuture<ItemDto> future = itemService.createItem(testCreateItemDto);
                ItemDto result = getResult(future);

                // Then
                assertThat(result).isEqualTo(testItemDto);
                verify(itemRepository).save(testItem);
                verify(itemRepository).findAll();
            }
        }

        @Test
        @DisplayName("Should throw ValidationException when createItemDto is null")
        void createItem_ShouldThrowValidationException_WhenCreateItemDtoIsNull() {
            // When & Then
            assertThrows(ValidationException.class, () -> itemService.createItem(null));
        }

        @Test
        @DisplayName("Should throw BusinessLogicException when duplicate name exists")
        void createItem_ShouldThrowBusinessLogicException_WhenDuplicateNameExists() {
            // Given
            Item existingItem = Item.builder()
                    .id(UUID.randomUUID())
                    .name("Test Item")
                    .description("Existing Description")
                    .price(new BigDecimal("50.00"))
                    .build();

            when(itemRepository.findAll()).thenReturn(List.of(existingItem));

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toEntity(testCreateItemDto)).thenReturn(testItem);

                // When & Then
                CompletableFuture<ItemDto> future = itemService.createItem(testCreateItemDto);
                assertAsyncException(future, BusinessLogicException.class);
            }
        }

        @Test
        @DisplayName("Should throw BusinessLogicException when price exceeds maximum")
        void createItem_ShouldThrowBusinessLogicException_WhenPriceExceedsMaximum() {
            // Given
            CreateItemDto expensiveDto = CreateItemDto.builder()
                    .name("Expensive Item")
                    .description("Very expensive item")
                    .price(new BigDecimal("15000.00"))
                    .build();

            Item expensiveItem = Item.builder()
                    .id(UUID.randomUUID())
                    .name("Expensive Item")
                    .description("Very expensive item")
                    .price(new BigDecimal("15000.00"))
                    .build();

            when(itemRepository.findAll()).thenReturn(List.of());

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toEntity(expensiveDto)).thenReturn(expensiveItem);

                // When & Then
                CompletableFuture<ItemDto> future = itemService.createItem(expensiveDto);
                assertAsyncException(future, BusinessLogicException.class);
            }
        }

        @Test
        @DisplayName("Should throw BusinessLogicException when rating is negative")
        void createItem_ShouldThrowBusinessLogicException_WhenRatingIsNegative() {
            // Given
            CreateItemDto negativeRatingDto = CreateItemDto.builder()
                    .name("Negative Rating Item")
                    .description("Item with negative rating")
                    .price(new BigDecimal("50.00"))
                    .rating(new BigDecimal("-1.0"))
                    .build();

            Item negativeRatingItem = Item.builder()
                    .id(UUID.randomUUID())
                    .name("Negative Rating Item")
                    .description("Item with negative rating")
                    .price(new BigDecimal("50.00"))
                    .rating(new BigDecimal("-1.0"))
                    .build();

            when(itemRepository.findAll()).thenReturn(List.of());

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toEntity(negativeRatingDto)).thenReturn(negativeRatingItem);

                // When & Then
                CompletableFuture<ItemDto> future = itemService.createItem(negativeRatingDto);
                assertAsyncException(future, BusinessLogicException.class);
            }
        }
    }

    @Nested
    @DisplayName("getItemById Tests")
    class GetItemByIdTests {

        @Test
        @DisplayName("Should return item DTO when item exists")
        void getItemById_ShouldReturnItemDto_WhenItemExists() throws Exception {
            // Given
            when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toDto(testItem)).thenReturn(testItemDto);

                // When
                CompletableFuture<ItemDto> future = itemService.getItem(testItemId);
                ItemDto result = getResult(future);

                // Then
                assertThat(result).isEqualTo(testItemDto);
                verify(itemRepository).findById(testItemId);
            }
        }

        @Test
        @DisplayName("Should return empty Optional when item does not exist")
        void getItemById_ShouldReturnEmptyOptional_WhenItemDoesNotExist() {
            // Given
            when(itemRepository.findById(testItemId)).thenReturn(Optional.empty());

            // When
            CompletableFuture<ItemDto> future = itemService.getItem(testItemId);
            
            // Then
            assertAsyncException(future, ItemNotFoundException.class);
            verify(itemRepository).findById(testItemId);
        }

        @Test
        @DisplayName("Should throw ValidationException when id is null")
        void getItemById_ShouldThrowValidationException_WhenIdIsNull() {
            // When & Then
            assertThrows(ValidationException.class, () -> itemService.getItem(null));
        }
    }

    @Nested
    @DisplayName("getAllItems Tests")
    class GetAllItemsTests {

        @Test
        @DisplayName("Should return all items as DTOs")
        void getAllItems_ShouldReturnAllItemsAsDtos() throws Exception {
            // Given
            List<Item> items = Arrays.asList(testItem, testItem);
            when(itemRepository.findAll()).thenReturn(items);

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toDtoList(items)).thenReturn(Arrays.asList(testItemDto, testItemDto));

                // When
                CompletableFuture<List<ItemDto>> future = itemService.getAllItems();
                List<ItemDto> result = getResult(future);

                // Then
                assertThat(result).hasSize(2);
                assertThat(result).containsExactly(testItemDto, testItemDto);
                verify(itemRepository).findAll();
            }
        }

        @Test
        @DisplayName("Should return empty list when no items exist")
        void getAllItems_ShouldReturnEmptyList_WhenNoItemsExist() throws Exception {
            // Given
            when(itemRepository.findAll()).thenReturn(List.of());

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toDtoList(List.of())).thenReturn(List.of());

                // When
                CompletableFuture<List<ItemDto>> future = itemService.getAllItems();
                List<ItemDto> result = getResult(future);

                // Then
                assertThat(result).isEmpty();
                verify(itemRepository).findAll();
            }
        }
    }

    @Nested
    @DisplayName("updateItem Tests")
    class UpdateItemTests {

        @Test
        @DisplayName("Should update item successfully when valid data provided")
        void updateItem_ShouldUpdateItem_WhenValidDataProvided() throws Exception {
            // Given
            when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
            when(itemRepository.findAll()).thenReturn(List.of());
            when(itemRepository.save(any(Item.class))).thenReturn(testItem);

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.updateEntity(testItem, testUpdateItemDto)).thenReturn(testItem);
                mockedMapper.when(() -> ItemMapper.toDto(testItem)).thenReturn(testItemDto);

                // When
                CompletableFuture<ItemDto> future = itemService.updateItem(testItemId, testUpdateItemDto);
                ItemDto result = getResult(future);

                // Then
                assertThat(result).isEqualTo(testItemDto);
                verify(itemRepository).findById(testItemId);
                verify(itemRepository).save(testItem);
            }
        }

        @Test
        @DisplayName("Should throw ValidationException when itemId is null")
        void updateItem_ShouldThrowValidationException_WhenItemIdIsNull() {
            // When & Then
            assertThrows(ValidationException.class, () -> itemService.updateItem(null, testUpdateItemDto));
        }

        @Test
        @DisplayName("Should throw ValidationException when updateItemDto is null")
        void updateItem_ShouldThrowValidationException_WhenUpdateItemDtoIsNull() {
            // When & Then
            assertThrows(ValidationException.class, () -> itemService.updateItem(testItemId, null));
        }

        @Test
        @DisplayName("Should throw ItemNotFoundException when item does not exist")
        void updateItem_ShouldThrowItemNotFoundException_WhenItemDoesNotExist() {
            // Given
            when(itemRepository.findById(testItemId)).thenReturn(Optional.empty());

            // When & Then
            CompletableFuture<ItemDto> future = itemService.updateItem(testItemId, testUpdateItemDto);
            assertAsyncException(future, ItemNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteItem Tests")
    class DeleteItemTests {

        @Test
        @DisplayName("Should delete item successfully when item exists")
        void deleteItem_ShouldDeleteItem_WhenItemExists() throws Exception {
            // Given
            when(itemRepository.deleteById(testItemId)).thenReturn(Optional.of(testItem));

            // When
            CompletableFuture<Boolean> future = itemService.deleteItem(testItemId);
            Boolean result = getResult(future);

            // Then
            assertThat(result).isTrue();
            verify(itemRepository).deleteById(testItemId);
        }

        @Test
        @DisplayName("Should return false when item does not exist")
        void deleteItem_ShouldReturnFalse_WhenItemDoesNotExist() throws Exception {
            // Given
            when(itemRepository.deleteById(testItemId)).thenReturn(Optional.empty());

            // When
            CompletableFuture<Boolean> future = itemService.deleteItem(testItemId);
            Boolean result = getResult(future);

            // Then
            assertThat(result).isFalse();
            verify(itemRepository).deleteById(testItemId);
        }

        @Test
        @DisplayName("Should throw ValidationException when id is null")
        void deleteItem_ShouldThrowValidationException_WhenIdIsNull() {
            // When & Then
            assertThrows(ValidationException.class, () -> itemService.deleteItem(null));
        }
    }

    @Nested
    @DisplayName("itemExists Tests")
    class ItemExistsTests {

        @Test
        @DisplayName("Should return true when item exists")
        void itemExists_ShouldReturnTrue_WhenItemExists() throws Exception {
            // Given
            when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));

            // When
            CompletableFuture<Boolean> future = itemService.itemExists(testItemId);
            Boolean result = getResult(future);

            // Then
            assertThat(result).isTrue();
            verify(itemRepository).findById(testItemId);
        }

        @Test
        @DisplayName("Should return false when item does not exist")
        void itemExists_ShouldReturnFalse_WhenItemDoesNotExist() throws Exception {
            // Given
            when(itemRepository.findById(testItemId)).thenReturn(Optional.empty());

            // When
            CompletableFuture<Boolean> future = itemService.itemExists(testItemId);
            Boolean result = getResult(future);

            // Then
            assertThat(result).isFalse();
            verify(itemRepository).findById(testItemId);
        }

        @Test
        @DisplayName("Should throw ValidationException when id is null")
        void itemExists_ShouldThrowValidationException_WhenIdIsNull() {
            // When & Then
            assertThrows(ValidationException.class, () -> itemService.itemExists(null));
        }
    }

    @Nested
    @DisplayName("getItemCount Tests")
    class GetItemCountTests {

        @Test
        @DisplayName("Should return correct count when items exist")
        void getItemCount_ShouldReturnCorrectCount_WhenItemsExist() throws Exception {
            // Given
            List<Item> items = Arrays.asList(testItem, testItem, testItem);
            when(itemRepository.findAll()).thenReturn(items);

            // When
            CompletableFuture<Long> future = itemService.getItemCount();
            Long result = getResult(future);

            // Then
            assertThat(result).isEqualTo(3);
            verify(itemRepository).findAll();
        }

        @Test
        @DisplayName("Should return zero when no items exist")
        void getItemCount_ShouldReturnZero_WhenNoItemsExist() throws Exception {
            // Given
            when(itemRepository.findAll()).thenReturn(List.of());

            // When
            CompletableFuture<Long> future = itemService.getItemCount();
            Long result = getResult(future);

            // Then
            assertThat(result).isZero();
            verify(itemRepository).findAll();
        }
    }

    @Nested
    @DisplayName("findItemsByName Tests")
    class FindItemsByNameTests {

        @Test
        @DisplayName("Should find items by name (case-insensitive)")
        void findItemsByName_ShouldFindItems_WhenNameMatches() throws Exception {
            // Given
            Item item1 = Item.builder().id(UUID.randomUUID()).name("Test Item").price(new BigDecimal("10.00")).build();
            Item item2 = Item.builder().id(UUID.randomUUID()).name("Another Test Item").price(new BigDecimal("20.00")).build();
            Item item3 = Item.builder().id(UUID.randomUUID()).name("Different Item").price(new BigDecimal("30.00")).build();
            
            List<Item> allItems = Arrays.asList(item1, item2, item3);
            when(itemRepository.findAll()).thenReturn(allItems);

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                List<ItemDto> expectedDtos = Arrays.asList(
                    ItemDto.builder().id(item1.getId()).name("Test Item").price(new BigDecimal("10.00")).build(),
                    ItemDto.builder().id(item2.getId()).name("Another Test Item").price(new BigDecimal("20.00")).build()
                );
                mockedMapper.when(() -> ItemMapper.toDtoList(any())).thenReturn(expectedDtos);

                // When
                CompletableFuture<List<ItemDto>> future = itemService.findItemsByName("test");
                List<ItemDto> result = getResult(future);

                // Then
                assertThat(result).hasSize(2);
                verify(itemRepository).findAll();
            }
        }

        @Test
        @DisplayName("Should return empty list when no items match")
        void findItemsByName_ShouldReturnEmptyList_WhenNoItemsMatch() throws Exception {
            // Given
            List<Item> items = Arrays.asList(testItem);
            when(itemRepository.findAll()).thenReturn(items);

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toDtoList(any())).thenReturn(List.of());

                // When
                CompletableFuture<List<ItemDto>> future = itemService.findItemsByName("nonexistent");
                List<ItemDto> result = getResult(future);

                // Then
                assertThat(result).isEmpty();
                verify(itemRepository).findAll();
            }
        }

        @Test
        @DisplayName("Should throw ValidationException when name is null")
        void findItemsByName_ShouldThrowValidationException_WhenNameIsNull() {
            // When & Then
            assertThrows(ValidationException.class, () -> itemService.findItemsByName(null));
        }
    }

    @Nested
    @DisplayName("findItemsByPriceRange Tests")
    class FindItemsByPriceRangeTests {

        @Test
        @DisplayName("Should find items within price range")
        void findItemsByPriceRange_ShouldFindItems_WhenWithinRange() throws Exception {
            // Given
            Item item1 = Item.builder().id(UUID.randomUUID()).name("Item 1").price(new BigDecimal("50.00")).build();
            Item item2 = Item.builder().id(UUID.randomUUID()).name("Item 2").price(new BigDecimal("100.00")).build();
            Item item3 = Item.builder().id(UUID.randomUUID()).name("Item 3").price(new BigDecimal("150.00")).build();
            
            List<Item> allItems = Arrays.asList(item1, item2, item3);
            when(itemRepository.findAll()).thenReturn(allItems);

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                List<ItemDto> expectedDtos = Arrays.asList(
                    ItemDto.builder().id(item2.getId()).name("Item 2").price(new BigDecimal("100.00")).build()
                );
                mockedMapper.when(() -> ItemMapper.toDtoList(any())).thenReturn(expectedDtos);

                // When
                CompletableFuture<List<ItemDto>> future = itemService.findItemsByPriceRange(
                    new BigDecimal("75.00"), 
                    new BigDecimal("125.00")
                );
                List<ItemDto> result = getResult(future);

                // Then
                assertThat(result).hasSize(1);
                verify(itemRepository).findAll();
            }
        }

        @Test
        @DisplayName("Should throw ValidationException when minPrice is null")
        void findItemsByPriceRange_ShouldThrowValidationException_WhenMinPriceIsNull() {
            // When & Then
            assertThrows(ValidationException.class, () -> itemService.findItemsByPriceRange(null, new BigDecimal("100.00")));
        }

        @Test
        @DisplayName("Should throw ValidationException when maxPrice is null")
        void findItemsByPriceRange_ShouldThrowValidationException_WhenMaxPriceIsNull() {
            // When & Then
            assertThrows(ValidationException.class, () -> itemService.findItemsByPriceRange(new BigDecimal("50.00"), null));
        }
    }

    @Nested
    @DisplayName("findItemsByMinimumRating Tests")
    class FindItemsByMinimumRatingTests {

        @Test
        @DisplayName("Should find items with minimum rating")
        void findItemsByMinimumRating_ShouldFindItems_WhenRatingMeetsMinimum() throws Exception {
            // Given
            Item item1 = Item.builder().id(UUID.randomUUID()).name("Item 1").price(new BigDecimal("10.00")).rating(new BigDecimal("3.5")).build();
            Item item2 = Item.builder().id(UUID.randomUUID()).name("Item 2").price(new BigDecimal("20.00")).rating(new BigDecimal("4.5")).build();
            Item item3 = Item.builder().id(UUID.randomUUID()).name("Item 3").price(new BigDecimal("30.00")).rating(null).build();
            Item item4 = Item.builder().id(UUID.randomUUID()).name("Item 4").price(new BigDecimal("40.00")).rating(new BigDecimal("2.5")).build();
            
            List<Item> allItems = Arrays.asList(item1, item2, item3, item4);
            when(itemRepository.findAll()).thenReturn(allItems);

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                List<ItemDto> expectedDtos = Arrays.asList(
                    ItemDto.builder().id(item1.getId()).name("Item 1").price(new BigDecimal("10.00")).rating(new BigDecimal("3.5")).build(),
                    ItemDto.builder().id(item2.getId()).name("Item 2").price(new BigDecimal("20.00")).rating(new BigDecimal("4.5")).build()
                );
                mockedMapper.when(() -> ItemMapper.toDtoList(any())).thenReturn(expectedDtos);

                // When
                CompletableFuture<List<ItemDto>> future = itemService.findItemsByMinimumRating(new BigDecimal("3.0"));
                List<ItemDto> result = getResult(future);

                // Then
                assertThat(result).hasSize(2);
                verify(itemRepository).findAll();
            }
        }

        @Test
        @DisplayName("Should throw ValidationException when minRating is null")
        void findItemsByMinimumRating_ShouldThrowValidationException_WhenMinRatingIsNull() {
            // When & Then
            assertThrows(ValidationException.class, () -> itemService.findItemsByMinimumRating(null));
        }
    }

    @Nested
    @DisplayName("Patch Item Tests")
    class PatchItemTests {

        @Test
        @DisplayName("Should patch item successfully when valid data provided")
        void patchItem_ShouldPatchItem_WhenValidDataProvided() throws Exception {
            // Given
            UUID itemId = UUID.randomUUID();
            PatchItemDto patchDto = PatchItemDto.builder()
                    .name("Updated Name")
                    .price(new BigDecimal("99.99"))
                    .build();
            Item existingItem = Item.builder()
                    .id(itemId)
                    .name("Original Name")
                    .imageUrl("https://example.com/image.jpg")
                    .description("Original Description")
                    .price(new BigDecimal("50.00"))
                    .rating(new BigDecimal("4.0"))
                    .specifications(List.of("Original Spec"))
                    .build();
            Item updatedItem = Item.builder()
                    .id(itemId)
                    .name("Updated Name")
                    .imageUrl("https://example.com/image.jpg")
                    .description("Original Description")
                    .price(new BigDecimal("99.99"))
                    .rating(new BigDecimal("4.0"))
                    .specifications(List.of("Original Spec"))
                    .build();
            ItemDto expectedDto = ItemDto.builder()
                    .id(itemId)
                    .name("Updated Name")
                    .imageUrl("https://example.com/image.jpg")
                    .description("Original Description")
                    .price(new BigDecimal("99.99"))
                    .rating(new BigDecimal("4.0"))
                    .specifications(List.of("Original Spec"))
                    .build();

            when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
            when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toDto(updatedItem)).thenReturn(expectedDto);

                // When
                CompletableFuture<ItemDto> future = itemService.patchItem(itemId, patchDto);
                ItemDto result = getResult(future);

                // Then
                assertThat(result).isEqualTo(expectedDto);
                verify(itemRepository).findById(itemId);
                verify(itemRepository).save(any(Item.class));
            }
        }

        @Test
        @DisplayName("Should throw ItemNotFoundException when item does not exist")
        void patchItem_ShouldThrowItemNotFoundException_WhenItemDoesNotExist() throws Exception {
            // Given
            UUID itemId = UUID.randomUUID();
            PatchItemDto patchDto = PatchItemDto.builder()
                    .name("Updated Name")
                    .build();

            when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

            // When & Then
            CompletableFuture<ItemDto> future = itemService.patchItem(itemId, patchDto);
            assertAsyncException(future, ItemNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw ValidationException when itemId is null")
        void patchItem_ShouldThrowValidationException_WhenItemIdIsNull() {
            // Given
            PatchItemDto patchDto = PatchItemDto.builder()
                    .name("Updated Name")
                    .build();

            // When & Then
            assertThrows(ValidationException.class, () -> itemService.patchItem(null, patchDto));
        }

        @Test
        @DisplayName("Should throw ValidationException when patchDto is null")
        void patchItem_ShouldThrowValidationException_WhenPatchDtoIsNull() {
            // Given
            UUID itemId = UUID.randomUUID();

            // When & Then
            assertThrows(ValidationException.class, () -> itemService.patchItem(itemId, null));
        }
    }

    @Nested
    @DisplayName("Add Specification Tests")
    class AddSpecificationTests {

        @Test
        @DisplayName("Should add specifications successfully when valid data provided")
        void addSpecification_ShouldAddSpecifications_WhenValidDataProvided() throws Exception {
            // Given
            UUID itemId = UUID.randomUUID();
            AddSpecificationDto addSpecDto = AddSpecificationDto.builder()
                    .specifications(List.of("New Spec 1", "New Spec 2"))
                    .build();
            Item existingItem = Item.builder()
                    .id(itemId)
                    .name("Test Item")
                    .imageUrl("https://example.com/image.jpg")
                    .description("Test Description")
                    .price(new BigDecimal("50.00"))
                    .rating(new BigDecimal("4.0"))
                    .specifications(List.of("Existing Spec"))
                    .build();
            Item updatedItem = Item.builder()
                    .id(itemId)
                    .name("Test Item")
                    .imageUrl("https://example.com/image.jpg")
                    .description("Test Description")
                    .price(new BigDecimal("50.00"))
                    .rating(new BigDecimal("4.0"))
                    .specifications(List.of("Existing Spec", "New Spec 1", "New Spec 2"))
                    .build();
            ItemDto expectedDto = ItemDto.builder()
                    .id(itemId)
                    .name("Test Item")
                    .imageUrl("https://example.com/image.jpg")
                    .description("Test Description")
                    .price(new BigDecimal("50.00"))
                    .rating(new BigDecimal("4.0"))
                    .specifications(List.of("Existing Spec", "New Spec 1", "New Spec 2"))
                    .build();

            when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
            when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toDto(updatedItem)).thenReturn(expectedDto);

                // When
                CompletableFuture<ItemDto> future = itemService.addSpecification(itemId, addSpecDto);
                ItemDto result = getResult(future);

                // Then
                assertThat(result).isEqualTo(expectedDto);
                verify(itemRepository).findById(itemId);
                verify(itemRepository).save(any(Item.class));
            }
        }

        @Test
        @DisplayName("Should throw ItemNotFoundException when item does not exist")
        void addSpecification_ShouldThrowItemNotFoundException_WhenItemDoesNotExist() throws Exception {
            // Given
            UUID itemId = UUID.randomUUID();
            AddSpecificationDto addSpecDto = AddSpecificationDto.builder()
                    .specifications(List.of("New Spec"))
                    .build();

            when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

            // When & Then
            CompletableFuture<ItemDto> future = itemService.addSpecification(itemId, addSpecDto);
            assertAsyncException(future, ItemNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw ValidationException when itemId is null")
        void addSpecification_ShouldThrowValidationException_WhenItemIdIsNull() {
            // Given
            AddSpecificationDto addSpecDto = AddSpecificationDto.builder()
                    .specifications(List.of("New Spec"))
                    .build();

            // When & Then
            assertThrows(ValidationException.class, () -> itemService.addSpecification(null, addSpecDto));
        }

        @Test
        @DisplayName("Should throw ValidationException when addSpecDto is null")
        void addSpecification_ShouldThrowValidationException_WhenAddSpecDtoIsNull() {
            // Given
            UUID itemId = UUID.randomUUID();

            // When & Then
            assertThrows(ValidationException.class, () -> itemService.addSpecification(itemId, null));
        }

        @Test
        @DisplayName("Should throw BusinessLogicException when specifications limit exceeded")
        void addSpecification_ShouldThrowBusinessLogicException_WhenSpecificationsLimitExceeded() throws Exception {
            // Given
            UUID itemId = UUID.randomUUID();
            AddSpecificationDto addSpecDto = AddSpecificationDto.builder()
                    .specifications(List.of("Spec1", "Spec2", "Spec3", "Spec4", "Spec5", "Spec6", "Spec7", "Spec8", "Spec9", "Spec10", "Spec11"))
                    .build();
            Item existingItem = Item.builder()
                    .id(itemId)
                    .name("Test Item")
                    .imageUrl("https://example.com/image.jpg")
                    .description("Test Description")
                    .price(new BigDecimal("50.00"))
                    .rating(new BigDecimal("4.0"))
                    .specifications(List.of("Spec1", "Spec2", "Spec3", "Spec4", "Spec5", "Spec6", "Spec7", "Spec8", "Spec9", "Spec10"))
                    .build();

            when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

            // When & Then
            CompletableFuture<ItemDto> future = itemService.addSpecification(itemId, addSpecDto);
            assertAsyncException(future, BusinessLogicException.class);
        }
    }
}
