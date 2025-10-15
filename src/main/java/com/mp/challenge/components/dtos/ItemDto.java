package com.mp.challenge.components.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ItemDto
 * <p>
 * Data Transfer Object for Item operations. This DTO is used to transfer item data
 * between different layers of the application, providing a clean interface for
 * API requests and responses while maintaining data validation and consistency.
 * <p>
 * Features:
 * <ul>
 *   <li>Comprehensive validation annotations for all fields</li>
 *   <li>Builder pattern for flexible object creation</li>
 *   <li>Proper JSON formatting for timestamps</li>
 *   <li>UUID validation for ID fields</li>
 *   <li>Decimal precision validation for price and rating</li>
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
public class ItemDto {
    
    @NotNull(message = "ID is required")
    private UUID id;

    @NotBlank(message = "Name is required and cannot be empty")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Image URL is required")
    @Pattern(regexp = "https?://.+", message = "Image URL must be a valid HTTP or HTTPS URL")
    private String imageUrl;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Rating must be at least 0")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5")
    @Digits(integer = 1, fraction = 2, message = "Rating must have at most 1 integer digit and 2 decimal places")
    private BigDecimal rating;

    @Size(max = 20, message = "Cannot have more than 20 specifications")
    private List<@NotBlank(message = "Specification cannot be blank") @Size(max = 100, message = "Specification cannot exceed 100 characters") String> specifications;

    @JsonFormat(pattern = "dd-MM-yyyy'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "dd-MM-yyyy'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
}
