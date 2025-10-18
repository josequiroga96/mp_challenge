package com.mp.challenge.components.mappers;

import com.mp.challenge.components.dtos.CreateItemDto;
import com.mp.challenge.components.dtos.ItemDto;
import com.mp.challenge.components.dtos.UpdateItemDto;
import com.mp.challenge.components.exceptions.ValidationException;
import com.mp.challenge.models.Item;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ItemMapper.
 * <p>
 * Tests focus on verifying correct mapping between Item entities and various DTOs,
 * ensuring proper data transformation and null safety.
 */
class ItemMapperTest {
    @Test
    void toDto_ShouldMapCorrectly_WhenValidItem() {
        // Given
        Item item = createTestItem();

        // When
        ItemDto result = ItemMapper.toDto(item);

        // Then
        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getImageUrl(), result.getImageUrl());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getPrice(), result.getPrice());
        assertEquals(item.getRating(), result.getRating());
        assertEquals(item.getSpecifications(), result.getSpecifications());
        assertEquals(item.getCreatedAt(), result.getCreatedAt());
        assertEquals(item.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void toDto_ShouldThrowException_WhenItemIsNull() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ItemMapper.toDto(null)
        );
        assertEquals("Validation failed for field 'item': Item cannot be null", exception.getMessage());
        assertEquals("VALIDATION_FAILED", exception.getErrorCode());
    }

    @Test
    void toEntity_ShouldMapCorrectly_WhenValidItemDto() {
        // Given
        ItemDto itemDto = createTestItemDto();

        // When
        Item result = ItemMapper.toEntity(itemDto);

        // Then
        assertNotNull(result);
        assertEquals(itemDto.getId(), result.getId());
        assertEquals(itemDto.getName(), result.getName());
        assertEquals(itemDto.getImageUrl(), result.getImageUrl());
        assertEquals(itemDto.getDescription(), result.getDescription());
        assertEquals(itemDto.getPrice(), result.getPrice());
        assertEquals(itemDto.getRating(), result.getRating());
        assertEquals(itemDto.getSpecifications(), result.getSpecifications());
        assertEquals(itemDto.getCreatedAt(), result.getCreatedAt());
        assertEquals(itemDto.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void toEntity_ShouldThrowException_WhenItemDtoIsNull() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ItemMapper.toEntity((ItemDto) null)
        );
        assertEquals("Validation failed for field 'itemDto': ItemDto cannot be null", exception.getMessage());
        assertEquals("VALIDATION_FAILED", exception.getErrorCode());
    }

    @Test
    void toEntity_ShouldGenerateIdAndTimestamps_WhenCreateItemDto() {
        // Given
        CreateItemDto createItemDto = createTestCreateItemDto();

        // When
        Item result = ItemMapper.toEntity(createItemDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(createItemDto.getName(), result.getName());
        assertEquals(createItemDto.getImageUrl(), result.getImageUrl());
        assertEquals(createItemDto.getDescription(), result.getDescription());
        assertEquals(createItemDto.getPrice(), result.getPrice());
        assertEquals(createItemDto.getRating(), result.getRating());
        assertEquals(createItemDto.getSpecifications(), result.getSpecifications());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertEquals(result.getCreatedAt(), result.getUpdatedAt()); // Should be equal for new items
    }

    @Test
    void toEntity_ShouldThrowException_WhenCreateItemDtoIsNull() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ItemMapper.toEntity((CreateItemDto) null)
        );
        assertEquals("Validation failed for field 'createItemDto': CreateItemDto cannot be null", exception.getMessage());
        assertEquals("VALIDATION_FAILED", exception.getErrorCode());
    }

    @Test
    void updateEntity_ShouldUpdateOnlyProvidedFields_WhenValidUpdateDto() {
        // Given
        Item existingItem = createTestItem();
        UpdateItemDto updateDto = UpdateItemDto.builder()
                .name("Updated Name")
                .price(new BigDecimal("199.99"))
                .build();

        // When
        Item result = ItemMapper.updateEntity(existingItem, updateDto);

        // Then
        assertNotNull(result);
        assertEquals(existingItem.getId(), result.getId());
        assertEquals("Updated Name", result.getName()); // Updated
        assertEquals(existingItem.getImageUrl(), result.getImageUrl()); // Unchanged
        assertEquals(existingItem.getDescription(), result.getDescription()); // Unchanged
        assertEquals(new BigDecimal("199.99"), result.getPrice()); // Updated
        assertEquals(existingItem.getRating(), result.getRating()); // Unchanged
        assertEquals(existingItem.getSpecifications(), result.getSpecifications()); // Unchanged
        assertEquals(existingItem.getCreatedAt(), result.getCreatedAt()); // Unchanged
        assertTrue(result.getUpdatedAt().isAfter(existingItem.getUpdatedAt())); // Updated
    }

    @Test
    void updateEntity_ShouldPreserveAllFields_WhenUpdateDtoHasNulls() {
        // Given
        Item existingItem = createTestItem();
        UpdateItemDto updateDto = UpdateItemDto.builder()
                .build(); // All fields null except ID

        // When
        Item result = ItemMapper.updateEntity(existingItem, updateDto);

        // Then
        assertNotNull(result);
        assertEquals(existingItem.getId(), result.getId());
        assertEquals(existingItem.getName(), result.getName());
        assertEquals(existingItem.getImageUrl(), result.getImageUrl());
        assertEquals(existingItem.getDescription(), result.getDescription());
        assertEquals(existingItem.getPrice(), result.getPrice());
        assertEquals(existingItem.getRating(), result.getRating());
        assertEquals(existingItem.getSpecifications(), result.getSpecifications());
        assertEquals(existingItem.getCreatedAt(), result.getCreatedAt());
        assertTrue(result.getUpdatedAt().isAfter(existingItem.getUpdatedAt())); // Only timestamp updated
    }

    @Test
    void updateEntity_ShouldThrowException_WhenExistingItemIsNull() {
        // Given
        UpdateItemDto updateDto = createTestUpdateItemDto();

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ItemMapper.updateEntity(null, updateDto)
        );
        assertEquals("Validation failed for field 'existingItem': Existing item cannot be null", exception.getMessage());
        assertEquals("VALIDATION_FAILED", exception.getErrorCode());
    }

    @Test
    void updateEntity_ShouldThrowException_WhenUpdateDtoIsNull() {
        // Given
        Item existingItem = createTestItem();

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ItemMapper.updateEntity(existingItem, null)
        );
        assertEquals("Validation failed for field 'updateItemDto': UpdateItemDto cannot be null", exception.getMessage());
        assertEquals("VALIDATION_FAILED", exception.getErrorCode());
    }

    @Test
    void toDtoList_ShouldMapAllItems_WhenValidList() {
        // Given
        List<Item> items = List.of(
                createTestItem(),
                createTestItem(),
                createTestItem()
        );

        // When
        List<ItemDto> result = ItemMapper.toDtoList(items);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(dto -> dto.getId() != null));
        assertTrue(result.stream().allMatch(dto -> dto.getName() != null));
    }

    @Test
    void toDtoList_ShouldThrowException_WhenListIsNull() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ItemMapper.toDtoList(null)
        );
        assertEquals("Validation failed for field 'items': Items list cannot be null", exception.getMessage());
        assertEquals("VALIDATION_FAILED", exception.getErrorCode());
    }

    @Test
    void toEntityList_ShouldMapAllDtos_WhenValidList() {
        // Given
        List<ItemDto> itemDtos = List.of(
                createTestItemDto(),
                createTestItemDto(),
                createTestItemDto()
        );

        // When
        List<Item> result = ItemMapper.toEntityList(itemDtos);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(item -> item.getId() != null));
        assertTrue(result.stream().allMatch(item -> item.getName() != null));
    }

    @Test
    void toEntityList_ShouldThrowException_WhenListIsNull() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> ItemMapper.toEntityList(null)
        );
        assertEquals("Validation failed for field 'itemDtos': ItemDtos list cannot be null", exception.getMessage());
        assertEquals("VALIDATION_FAILED", exception.getErrorCode());
    }

    /**
     * Helper method to create a test Item.
     */
    private Item createTestItem() {
        return new Item(
                UUID.randomUUID(),
                "Test Item",
                "https://example.com/image.jpg",
                "Test description",
                new BigDecimal("99.99"),
                new BigDecimal("4.5"),
                List.of("Spec 1", "Spec 2"),
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()
        );
    }

    /**
     * Helper method to create a test ItemDto.
     */
    private ItemDto createTestItemDto() {
        return ItemDto.builder()
                .id(UUID.randomUUID())
                .name("Test Item DTO")
                .imageUrl("https://example.com/dto-image.jpg")
                .description("Test DTO description")
                .price(new BigDecimal("149.99"))
                .rating(new BigDecimal("4.8"))
                .specifications(List.of("DTO Spec 1", "DTO Spec 2"))
                .createdAt(LocalDateTime.now().minusHours(2))
                .updatedAt(LocalDateTime.now().minusMinutes(30))
                .build();
    }

    /**
     * Helper method to create a test CreateItemDto.
     */
    private CreateItemDto createTestCreateItemDto() {
        return CreateItemDto.builder()
                .name("New Test Item")
                .imageUrl("https://example.com/new-image.jpg")
                .description("New test description")
                .price(new BigDecimal("79.99"))
                .rating(new BigDecimal("4.2"))
                .specifications(List.of("New Spec 1", "New Spec 2"))
                .build();
    }

    /**
     * Helper method to create a test UpdateItemDto.
     */
    private UpdateItemDto createTestUpdateItemDto() {
        return UpdateItemDto.builder()
                .name("Updated Test Item")
                .imageUrl("https://example.com/updated-image.jpg")
                .description("Updated test description")
                .price(new BigDecimal("129.99"))
                .rating(new BigDecimal("4.7"))
                .specifications(List.of("Updated Spec 1", "Updated Spec 2"))
                .build();
    }
}
