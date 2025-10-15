package com.mp.challenge.controllers;

import com.mp.challenge.dtos.HealthResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HealthController
 * <p>
 * This controller is responsible for providing a health check endpoint to verify the application's operational status.
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 14/10/2025
 */
@Slf4j
@RestController
@RequestMapping("/public")
public class HealthController {

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
     * @since 14/10/2025
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponseDto> healthCheck() {
        log.debug("Health check requested");
        HealthResponseDto response = new HealthResponseDto(
                "200 OK",
                "The server is running correctly",
                "MercadoLibre Challenge Application",
                java.time.LocalDateTime.now()
        );
        log.debug("Health check response generated: {}", response.getStatus());
        return ResponseEntity.ok(response);
    }
}
