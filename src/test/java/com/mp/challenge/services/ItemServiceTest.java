package com.mp.challenge.services;

import com.mp.challenge.components.dtos.CreateItemDto;
import com.mp.challenge.components.dtos.ItemDto;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ItemServiceTest
 * <p>
 * Comprehensive test suite for ItemService class. This test class verifies all
 * business logic, validation, and integration aspects of the ItemService.
 * <p>
 * Test coverage includes:
 * <ul>
 *   <li>CRUD operations with proper validation</li>
 *   <li>Business rule enforcement</li>
 *   <li>Exception handling scenarios</li>
 *   <li>Search and filtering functionality</li>
 *   <li>Logging verification</li>
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

    @Nested
    @DisplayName("createItem Tests")
    class CreateItemTests {

        @Test
        @DisplayName("Should create item successfully when valid DTO provided")
        void createItem_ShouldCreateItem_WhenValidDtoProvided() {
            // Given
            when(itemRepository.findAll()).thenReturn(List.of());
            when(itemRepository.save(any(Item.class))).thenReturn(testItem);

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toEntity(testCreateItemDto)).thenReturn(testItem);
                mockedMapper.when(() -> ItemMapper.toDto(testItem)).thenReturn(testItemDto);

                // When
                ItemDto result = itemService.createItem(testCreateItemDto);

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
            assertThatThrownBy(() -> itemService.createItem(null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("createItemDto cannot be null");
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
                assertThatThrownBy(() -> itemService.createItem(testCreateItemDto))
                        .isInstanceOf(BusinessLogicException.class)
                        .hasMessage("An item with the name 'Test Item' already exists");
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
                assertThatThrownBy(() -> itemService.createItem(expensiveDto))
                        .isInstanceOf(BusinessLogicException.class)
                        .hasMessage("Item price cannot exceed $10,000");
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
                assertThatThrownBy(() -> itemService.createItem(negativeRatingDto))
                        .isInstanceOf(BusinessLogicException.class)
                        .hasMessage("Item rating cannot be negative");
            }
        }
    }

    @Nested
    @DisplayName("getItemById Tests")
    class GetItemByIdTests {

        @Test
        @DisplayName("Should return item DTO when item exists")
        void getItemById_ShouldReturnItemDto_WhenItemExists() {
            // Given
            when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toDto(testItem)).thenReturn(testItemDto);

                // When
                Optional<ItemDto> result = itemService.getItemById(testItemId);

                // Then
                assertThat(result).isPresent();
                assertThat(result.get()).isEqualTo(testItemDto);
                verify(itemRepository).findById(testItemId);
            }
        }

        @Test
        @DisplayName("Should return empty Optional when item does not exist")
        void getItemById_ShouldReturnEmptyOptional_WhenItemDoesNotExist() {
            // Given
            when(itemRepository.findById(testItemId)).thenReturn(Optional.empty());

            // When
            Optional<ItemDto> result = itemService.getItemById(testItemId);

            // Then
            assertThat(result).isEmpty();
            verify(itemRepository).findById(testItemId);
        }

        @Test
        @DisplayName("Should throw ValidationException when id is null")
        void getItemById_ShouldThrowValidationException_WhenIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> itemService.getItemById(null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("id cannot be null");
        }
    }

    @Nested
    @DisplayName("getAllItems Tests")
    class GetAllItemsTests {

        @Test
        @DisplayName("Should return all items as DTOs")
        void getAllItems_ShouldReturnAllItemsAsDtos() {
            // Given
            List<Item> items = Arrays.asList(testItem, testItem);
            when(itemRepository.findAll()).thenReturn(items);

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toDtoList(items)).thenReturn(Arrays.asList(testItemDto, testItemDto));

                // When
                List<ItemDto> result = itemService.getAllItems();

                // Then
                assertThat(result).hasSize(2);
                assertThat(result).containsExactly(testItemDto, testItemDto);
                verify(itemRepository).findAll();
            }
        }

        @Test
        @DisplayName("Should return empty list when no items exist")
        void getAllItems_ShouldReturnEmptyList_WhenNoItemsExist() {
            // Given
            when(itemRepository.findAll()).thenReturn(List.of());

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toDtoList(List.of())).thenReturn(List.of());

                // When
                List<ItemDto> result = itemService.getAllItems();

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
        void updateItem_ShouldUpdateItem_WhenValidDataProvided() {
            // Given
            when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
            when(itemRepository.findAll()).thenReturn(List.of());
            when(itemRepository.save(any(Item.class))).thenReturn(testItem);

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.updateEntity(testItem, testUpdateItemDto)).thenReturn(testItem);
                mockedMapper.when(() -> ItemMapper.toDto(testItem)).thenReturn(testItemDto);

                // When
                ItemDto result = itemService.updateItem(testItemId, testUpdateItemDto);

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
            assertThatThrownBy(() -> itemService.updateItem(null, testUpdateItemDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("itemId cannot be null");
        }

        @Test
        @DisplayName("Should throw ValidationException when updateItemDto is null")
        void updateItem_ShouldThrowValidationException_WhenUpdateItemDtoIsNull() {
            // When & Then
            assertThatThrownBy(() -> itemService.updateItem(testItemId, null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("updateItemDto cannot be null");
        }

        @Test
        @DisplayName("Should throw ItemNotFoundException when item does not exist")
        void updateItem_ShouldThrowItemNotFoundException_WhenItemDoesNotExist() {
            // Given
            when(itemRepository.findById(testItemId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> itemService.updateItem(testItemId, testUpdateItemDto))
                    .isInstanceOf(ItemNotFoundException.class)
                    .hasMessage("Item with ID '" + testItemId + "' was not found during update operation");
        }
    }

    @Nested
    @DisplayName("deleteItem Tests")
    class DeleteItemTests {

        @Test
        @DisplayName("Should delete item successfully when item exists")
        void deleteItem_ShouldDeleteItem_WhenItemExists() {
            // Given
            when(itemRepository.deleteById(testItemId)).thenReturn(Optional.of(testItem));

            // When
            boolean result = itemService.deleteItem(testItemId);

            // Then
            assertThat(result).isTrue();
            verify(itemRepository).deleteById(testItemId);
        }

        @Test
        @DisplayName("Should return false when item does not exist")
        void deleteItem_ShouldReturnFalse_WhenItemDoesNotExist() {
            // Given
            when(itemRepository.deleteById(testItemId)).thenReturn(Optional.empty());

            // When
            boolean result = itemService.deleteItem(testItemId);

            // Then
            assertThat(result).isFalse();
            verify(itemRepository).deleteById(testItemId);
        }

        @Test
        @DisplayName("Should throw ValidationException when id is null")
        void deleteItem_ShouldThrowValidationException_WhenIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> itemService.deleteItem(null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("id cannot be null");
        }
    }

    @Nested
    @DisplayName("itemExists Tests")
    class ItemExistsTests {

        @Test
        @DisplayName("Should return true when item exists")
        void itemExists_ShouldReturnTrue_WhenItemExists() {
            // Given
            when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));

            // When
            boolean result = itemService.itemExists(testItemId);

            // Then
            assertThat(result).isTrue();
            verify(itemRepository).findById(testItemId);
        }

        @Test
        @DisplayName("Should return false when item does not exist")
        void itemExists_ShouldReturnFalse_WhenItemDoesNotExist() {
            // Given
            when(itemRepository.findById(testItemId)).thenReturn(Optional.empty());

            // When
            boolean result = itemService.itemExists(testItemId);

            // Then
            assertThat(result).isFalse();
            verify(itemRepository).findById(testItemId);
        }

        @Test
        @DisplayName("Should throw ValidationException when id is null")
        void itemExists_ShouldThrowValidationException_WhenIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> itemService.itemExists(null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("id cannot be null");
        }
    }

    @Nested
    @DisplayName("getItemCount Tests")
    class GetItemCountTests {

        @Test
        @DisplayName("Should return correct count when items exist")
        void getItemCount_ShouldReturnCorrectCount_WhenItemsExist() {
            // Given
            List<Item> items = Arrays.asList(testItem, testItem, testItem);
            when(itemRepository.findAll()).thenReturn(items);

            // When
            long result = itemService.getItemCount();

            // Then
            assertThat(result).isEqualTo(3);
            verify(itemRepository).findAll();
        }

        @Test
        @DisplayName("Should return zero when no items exist")
        void getItemCount_ShouldReturnZero_WhenNoItemsExist() {
            // Given
            when(itemRepository.findAll()).thenReturn(List.of());

            // When
            long result = itemService.getItemCount();

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
        void findItemsByName_ShouldFindItems_WhenNameMatches() {
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
                List<ItemDto> result = itemService.findItemsByName("test");

                // Then
                assertThat(result).hasSize(2);
                verify(itemRepository).findAll();
            }
        }

        @Test
        @DisplayName("Should return empty list when no items match")
        void findItemsByName_ShouldReturnEmptyList_WhenNoItemsMatch() {
            // Given
            List<Item> items = Arrays.asList(testItem);
            when(itemRepository.findAll()).thenReturn(items);

            try (MockedStatic<ItemMapper> mockedMapper = mockStatic(ItemMapper.class)) {
                mockedMapper.when(() -> ItemMapper.toDtoList(any())).thenReturn(List.of());

                // When
                List<ItemDto> result = itemService.findItemsByName("nonexistent");

                // Then
                assertThat(result).isEmpty();
                verify(itemRepository).findAll();
            }
        }

        @Test
        @DisplayName("Should throw ValidationException when name is null")
        void findItemsByName_ShouldThrowValidationException_WhenNameIsNull() {
            // When & Then
            assertThatThrownBy(() -> itemService.findItemsByName(null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("name cannot be null");
        }
    }

    @Nested
    @DisplayName("findItemsByPriceRange Tests")
    class FindItemsByPriceRangeTests {

        @Test
        @DisplayName("Should find items within price range")
        void findItemsByPriceRange_ShouldFindItems_WhenWithinRange() {
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
                List<ItemDto> result = itemService.findItemsByPriceRange(
                    new BigDecimal("75.00"), 
                    new BigDecimal("125.00")
                );

                // Then
                assertThat(result).hasSize(1);
                verify(itemRepository).findAll();
            }
        }

        @Test
        @DisplayName("Should throw ValidationException when minPrice is null")
        void findItemsByPriceRange_ShouldThrowValidationException_WhenMinPriceIsNull() {
            // When & Then
            assertThatThrownBy(() -> itemService.findItemsByPriceRange(null, new BigDecimal("100.00")))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("minPrice cannot be null");
        }

        @Test
        @DisplayName("Should throw ValidationException when maxPrice is null")
        void findItemsByPriceRange_ShouldThrowValidationException_WhenMaxPriceIsNull() {
            // When & Then
            assertThatThrownBy(() -> itemService.findItemsByPriceRange(new BigDecimal("50.00"), null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("maxPrice cannot be null");
        }
    }

    @Nested
    @DisplayName("findItemsByMinimumRating Tests")
    class FindItemsByMinimumRatingTests {

        @Test
        @DisplayName("Should find items with minimum rating")
        void findItemsByMinimumRating_ShouldFindItems_WhenRatingMeetsMinimum() {
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
                List<ItemDto> result = itemService.findItemsByMinimumRating(new BigDecimal("3.0"));

                // Then
                assertThat(result).hasSize(2);
                verify(itemRepository).findAll();
            }
        }

        @Test
        @DisplayName("Should throw ValidationException when minRating is null")
        void findItemsByMinimumRating_ShouldThrowValidationException_WhenMinRatingIsNull() {
            // When & Then
            assertThatThrownBy(() -> itemService.findItemsByMinimumRating(null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("minRating cannot be null");
        }
    }
}
