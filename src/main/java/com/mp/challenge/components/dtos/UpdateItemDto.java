package com.mp.challenge.components.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * UpdateItemDto
 * <p>
 * Data Transfer Object for updating existing items. This DTO is used specifically for
 * item update operations where the ID is required to identify the item to update,
 * but timestamps are not included as they are managed by the system.
 * <p>
 * Features:
 * <ul>
 *   <li>Required ID field for item identification</li>
 *   <li>Validation annotations for all fields</li>
 *   <li>Builder pattern for flexible object creation</li>
 *   <li>No timestamp fields (managed by system)</li>
 *   <li>All fields optional except ID for partial updates</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 15/10/2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemDto {
    
    @NotNull(message = "ID is required for updates")
    private UUID id;

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Pattern(regexp = "https?://.+", message = "Image URL must be a valid HTTP or HTTPS URL")
    private String imageUrl;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Rating must be at least 0")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5")
    @Digits(integer = 1, fraction = 2, message = "Rating must have at most 1 integer digit and 2 decimal places")
    private BigDecimal rating;

    @Size(max = 20, message = "Cannot have more than 20 specifications")
    private List<@NotBlank(message = "Specification cannot be blank") @Size(max = 100, message = "Specification cannot exceed 100 characters") String> specifications;
}
