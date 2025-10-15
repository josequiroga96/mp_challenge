package com.mp.challenge.components.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Map;

/**
 * ErrorResponse
 * <p>
 * This DTO is used to return the error response.
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 15/10/2025
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {
    @NotBlank(message = "Error code cannot be blank")
    private final String errorCode;

    private final String userMessage;
    private final String technicalMessage;
    private final Map<String, String> details;

    @NotNull(message = "Timestamp cannot be null")
    @JsonFormat(pattern = "dd-MM-yyyy'T'HH:mm:ss.SSS")
    private final long timestamp;
}
