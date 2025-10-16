package com.mp.challenge.controllers.contracts;

import com.mp.challenge.components.dtos.HealthResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * IHealthController
 * <p>
 * Contract interface for Health Controller operations. This interface defines
 * the health check endpoint with comprehensive Swagger documentation.
 * <p>
 * Features:
 * <ul>
 *   <li>Health check endpoint definition</li>
 *   <li>Complete Swagger/OpenAPI documentation</li>
 *   <li>Response examples and schemas</li>
 *   <li>Clean separation of concerns</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 16/10/2025
 */
@RequestMapping("/public")
@Tag(name = "Health", description = "Health check and system status operations")
public interface IHealthController {

    /**
     * Performs a health check to verify that the server is running correctly.
     * <p>
     * This endpoint provides a simple way to verify the application's operational status.
     * It returns a standardized response containing the server status, a descriptive message,
     * the current timestamp, and the service name.
     * <p>
     * The response always indicates a successful status as long as the endpoint is accessible,
     * which means the Spring Boot application is running and the web layer is functional.
     *
     * @return ResponseEntity&lt;HealthResponseDto&gt; containing:
     * <ul>
     *     <li>status: "200 OK" indicating the server is operational</li>
     *     <li>message: A descriptive message confirming server functionality</li>
     *     <li>timestamp: The current date and time when the check was performed</li>
     *     <li>service: The name of the application service</li>
     * </ul>
     * @since 16/10/2025
     */
    @Operation(
            summary = "Health check",
            description = "Performs a health check to verify that the server is running correctly. " +
                    "Returns the application status, message, timestamp, and service name."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Server is running correctly",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = HealthResponseDto.class),
                    examples = @ExampleObject(
                            name = "Success Response",
                            value = "{\n" +
                                    "  \"status\": \"200 OK\",\n" +
                                    "  \"message\": \"The server is running correctly\",\n" +
                                    "  \"service\": \"MercadoLibre Challenge Application\",\n" +
                                    "  \"timestamp\": \"2025-10-16T10:30:00Z\"\n" +
                                    "}"
                    )
            )
    )
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<HealthResponseDto> healthCheck();
}
