package com.mp.challenge.components.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenApiConfig
 * <p>
 * Configuration class for OpenAPI/Swagger documentation. This class defines
 * the API documentation metadata, contact information, and server configuration.
 * <p>
 * Features:
 * <ul>
 *   <li>Comprehensive API documentation with metadata</li>
 *   <li>Contact information and licensing details</li>
 *   <li>Server configuration for different environments</li>
 *   <li>Integration with SpringDoc OpenAPI</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 16/10/2025
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Configures the OpenAPI documentation for the application.
     *
     * @return OpenAPI configuration bean
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Challenge API")
                        .description("""
                                A comprehensive REST API for managing items with full CRUD operations.
                                
                                ## Features
                                - **Item Management**: Complete CRUD operations for items
                                - **Search & Filtering**: Advanced search capabilities by name, price range, and rating
                                - **Validation**: Comprehensive input validation with custom exceptions
                                - **Async Processing**: Virtual Threads support for high-performance async operations
                                - **Documentation**: Interactive API documentation with Swagger UI
                                
                                ## Technology Stack
                                - **Java 21** with Virtual Threads
                                - **Spring Boot 3.5.6**
                                - **JSON File Storage** with Compare-and-Swap (CAS) mechanism
                                - **Custom Exception Handling**
                                - **Jakarta Bean Validation**
                                
                                ## API Endpoints
                                - `GET /api/items` - Retrieve all items
                                - `GET /api/items/{id}` - Get item by ID
                                - `POST /api/items` - Create new item
                                - `PUT /api/items/{id}` - Update existing item
                                - `DELETE /api/items/{id}` - Delete item
                                - `GET /api/items/search` - Search items with filters
                                
                                ## Error Handling
                                The API uses custom exceptions for better error handling:
                                - `ValidationException` (400) - Input validation errors
                                - `ItemNotFoundException` (404) - Resource not found
                                - `BusinessLogicException` (409) - Business rule violations
                                - `JsonStorageException` (500) - Storage operation errors
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Jose Quiroga")
                                .email("jmquiroga1996@gmail.com")
                                .url("https://github.com/josequiroga96"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server")
                ));
    }
}
