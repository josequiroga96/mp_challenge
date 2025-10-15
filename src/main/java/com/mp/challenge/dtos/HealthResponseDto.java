package com.mp.challenge.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * HealthResponseDto
 * <p>
 * This DTO is used to return the health check response.
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
public class HealthResponseDto {
    
    @NotBlank(message = "Status cannot be blank")
    private String status;

    @NotBlank(message = "Message cannot be blank")
    private String message;

    @NotBlank(message = "Service name cannot be blank")
    private String service;

    @NotNull(message = "Timestamp is required")
    @JsonFormat(pattern = "dd-MM-yyyy'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
}
