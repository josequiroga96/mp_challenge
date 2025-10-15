package com.mp.challenge.models;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Item
 * <p>
 * This model is used to represent an item in the system.
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 14/10/2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
