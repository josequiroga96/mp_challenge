package com.mp.challenge.components.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AddSpecificationDto
 * <p>
 * Data Transfer Object for adding specifications to existing items.
 * This DTO is used to add new specifications to an item without affecting existing ones.
 * <p>
 * Features:
 * <ul>
 *   <li>List of specifications to add</li>
 *   <li>Validation for specification content</li>
 *   <li>Builder pattern for flexible object creation</li>
 *   <li>No ID field (provided in URL path)</li>
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
@Schema(description = "Data for adding specifications to an item")
public class AddSpecificationDto {
    
    @Schema(description = "List of specifications to add", example = "[\"Wireless\", \"Bluetooth 5.0\", \"Water Resistant\"]", required = true)
    @Size(min = 1, max = 10, message = "Must provide between 1 and 10 specifications")
    private List<@NotBlank(message = "Specification cannot be blank") 
                 @Size(max = 100, message = "Specification cannot exceed 100 characters") 
                 String> specifications;
}
