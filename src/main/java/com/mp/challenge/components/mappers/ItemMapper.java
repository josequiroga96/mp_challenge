package com.mp.challenge.components.mappers;

import com.mp.challenge.components.dtos.CreateItemDto;
import com.mp.challenge.components.dtos.ItemDto;
import com.mp.challenge.components.dtos.UpdateItemDto;
import com.mp.challenge.components.exceptions.ValidationException;
import com.mp.challenge.models.Item;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ItemMapper
 * <p>
 * Mapper component responsible for converting between Item entity and various DTOs.
 * This mapper provides a clean separation between the domain model and the API layer,
 * ensuring proper data transformation while maintaining business logic integrity.
 * <p>
 * Features:
 * <ul>
 *   <li>Bidirectional mapping between Item and ItemDto</li>
 *   <li>Creation mapping from CreateItemDto with auto-generated fields</li>
 *   <li>Update mapping from UpdateItemDto with selective field updates</li>
 *   <li>Batch mapping for collections</li>
 *   <li>Null-safe operations with proper validation</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 15/10/2025
 */
@UtilityClass
public class ItemMapper {

    /**
     * Converts an Item entity to ItemDto.
     *
     * @param item the Item entity to convert
     * @return ItemDto representation of the item
     * @throws ValidationException if item is null
     */
    public static ItemDto toDto(Item item) {
        if (item == null) {
            throw new ValidationException("item", "Item cannot be null");
        }

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .imageUrl(item.getImageUrl())
                .description(item.getDescription())
                .price(item.getPrice())
                .rating(item.getRating())
                .specifications(item.getSpecifications())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    /**
     * Converts an ItemDto to Item entity.
     *
     * @param itemDto the ItemDto to convert
     * @return Item entity representation of the DTO
     * @throws ValidationException if itemDto is null
     */
    public static Item toEntity(ItemDto itemDto) {
        if (itemDto == null) {
            throw new ValidationException("itemDto", "ItemDto cannot be null");
        }

        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getImageUrl(),
                itemDto.getDescription(),
                itemDto.getPrice(),
                itemDto.getRating(),
                itemDto.getSpecifications(),
                itemDto.getCreatedAt(),
                itemDto.getUpdatedAt()
        );
    }

    /**
     * Converts a CreateItemDto to Item entity with auto-generated fields.
     * This method generates a new UUID and sets current timestamps.
     *
     * @param createItemDto the CreateItemDto to convert
     * @return Item entity with auto-generated ID and timestamps
     * @throws ValidationException if createItemDto is null
     */
    public static Item toEntity(CreateItemDto createItemDto) {
        if (createItemDto == null) {
            throw new ValidationException("createItemDto", "CreateItemDto cannot be null");
        }

        LocalDateTime now = LocalDateTime.now();

        return new Item(
                UUID.randomUUID(),
                createItemDto.getName(),
                createItemDto.getImageUrl(),
                createItemDto.getDescription(),
                createItemDto.getPrice(),
                createItemDto.getRating(),
                createItemDto.getSpecifications(),
                now,
                now
        );
    }

    /**
     * Updates an existing Item entity with data from UpdateItemDto.
     * Only non-null fields from the DTO will be applied to the entity.
     * The updatedAt timestamp is automatically set to current time.
     *
     * @param existingItem  the existing Item entity to update
     * @param updateItemDto the UpdateItemDto containing update data
     * @return updated Item entity
     * @throws ValidationException if either parameter is null
     */
    public static Item updateEntity(Item existingItem, UpdateItemDto updateItemDto) {
        if (existingItem == null) {
            throw new ValidationException("existingItem", "Existing item cannot be null");
        }
        if (updateItemDto == null) {
            throw new ValidationException("updateItemDto", "UpdateItemDto cannot be null");
        }

        // Create a new Item with updated fields
        return new Item(
                existingItem.getId(), // ID never changes
                updateItemDto.getName() != null ? updateItemDto.getName() : existingItem.getName(),
                updateItemDto.getImageUrl() != null ? updateItemDto.getImageUrl() : existingItem.getImageUrl(),
                updateItemDto.getDescription() != null ? updateItemDto.getDescription() : existingItem.getDescription(),
                updateItemDto.getPrice() != null ? updateItemDto.getPrice() : existingItem.getPrice(),
                updateItemDto.getRating() != null ? updateItemDto.getRating() : existingItem.getRating(),
                updateItemDto.getSpecifications() != null ? updateItemDto.getSpecifications() : existingItem.getSpecifications(),
                existingItem.getCreatedAt(), // createdAt never changes
                LocalDateTime.now() // updatedAt always set to current time
        );
    }

    /**
     * Converts a list of Item entities to a list of ItemDtos.
     *
     * @param items the list of Item entities to convert
     * @return list of ItemDtos
     * @throws ValidationException if items list is null
     */
    public static List<ItemDto> toDtoList(List<Item> items) {
        if (items == null) {
            throw new ValidationException("items", "Items list cannot be null");
        }

        return items.stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    /**
     * Converts a list of ItemDtos to a list of Item entities.
     *
     * @param itemDtos the list of ItemDtos to convert
     * @return list of Item entities
     * @throws ValidationException if itemDtos list is null
     */
    public static List<Item> toEntityList(List<ItemDto> itemDtos) {
        if (itemDtos == null) {
            throw new ValidationException("itemDtos", "ItemDtos list cannot be null");
        }

        return itemDtos.stream()
                .map(ItemMapper::toEntity)
                .toList();
    }

    /**
     * Creates a partial Item entity from UpdateItemDto for validation purposes.
     * This method creates an Item with only the provided fields, useful for
     * validation scenarios where you need to test specific field combinations.
     *
     * @param updateItemDto the UpdateItemDto to convert
     * @return Item entity with only the provided fields
     * @throws ValidationException if updateItemDto is null
     */
    public static Item toPartialEntity(UpdateItemDto updateItemDto) {
        if (updateItemDto == null) {
            throw new ValidationException("updateItemDto", "UpdateItemDto cannot be null");
        }

        return new Item(
                updateItemDto.getId(),
                updateItemDto.getName(),
                updateItemDto.getImageUrl(),
                updateItemDto.getDescription(),
                updateItemDto.getPrice(),
                updateItemDto.getRating(),
                updateItemDto.getSpecifications(),
                null, // createdAt not available in update DTO
                null  // updatedAt not available in update DTO
        );
    }

    /**
     * Creates a copy of an Item entity with updated timestamp.
     * Useful for scenarios where you need to mark an item as updated
     * without changing any other fields.
     *
     * @param item the Item entity to copy
     * @return new Item entity with updated timestamp
     * @throws ValidationException if item is null
     */
    public static Item touchEntity(Item item) {
        if (item == null) {
            throw new ValidationException("item", "Item cannot be null");
        }

        return new Item(
                item.getId(),
                item.getName(),
                item.getImageUrl(),
                item.getDescription(),
                item.getPrice(),
                item.getRating(),
                item.getSpecifications(),
                item.getCreatedAt(),
                LocalDateTime.now()
        );
    }
}
