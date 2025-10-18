package com.mp.challenge.components.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * PatchItemDto
 * <p>
 * Data Transfer Object for partial updates of existing items using PATCH operations.
 * This DTO allows updating specific fields of an item without affecting others.
 * <p>
 * Features:
 * <ul>
 *   <li>All fields are optional for partial updates</li>
 *   <li>Validation annotations for data integrity</li>
 *   <li>Builder pattern for flexible object creation</li>
 *   <li>No ID field (provided in URL path)</li>
 *   <li>No timestamp fields (managed by system)</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 17/10/2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data for partial item updates")
public class PatchItemDto {
    
    @Schema(description = "Item name", example = "Updated Product Name", maxLength = 100)
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Image URL", example = "https://example.com/image.jpg")
    @Pattern(regexp = "https?://.+", message = "Image URL must be a valid HTTP or HTTPS URL")
    private String imageUrl;

    @Schema(description = "Item description", example = "Updated product description", maxLength = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Schema(description = "Item price", example = "99.99", minimum = "0.01")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal price;

    @Schema(description = "Item rating", example = "4.5", minimum = "0.0", maximum = "5.0")
    @DecimalMin(value = "0.0", message = "Rating must be at least 0")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5")
    @Digits(integer = 1, fraction = 2, message = "Rating must have at most 1 integer digit and 2 decimal places")
    private BigDecimal rating;
}
