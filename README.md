# Challenge API

A high-performance, enterprise-grade REST API for item management built with Spring Boot 3.5.6 and Java 21. This application demonstrates modern software engineering practices including Virtual Threads, Compare-and-Swap operations, comprehensive testing, and clean architecture principles.

## 🚀 Features

### Core Functionality
- **Complete CRUD Operations** - Create, Read, Update, Delete items with full validation
- **Advanced Search & Filtering** - Search by name, price range, and minimum rating
- **Batch Operations** - Retrieve multiple items by IDs efficiently
- **Partial Updates** - PATCH operations for selective field updates
- **Specification Management** - Add specifications to existing items
- **Health Monitoring** - Built-in health check endpoint

### Technical Excellence
- **Java 21 Virtual Threads** - High-performance async processing
- **Thread-Safe Storage** - Compare-and-Swap (CAS) operations for data consistency
- **Comprehensive Validation** - Jakarta Bean Validation with custom error handling
- **Exception Management** - Custom exception hierarchy with proper HTTP status mapping
- **API Documentation** - Interactive Swagger UI with detailed examples
- **Extensive Testing** - 132+ test cases with 100% coverage of critical paths

## 🏗️ Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    Controllers Layer                        │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │  ItemController │  │ HealthController│                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                     Services Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │   ItemService   │  │  AsyncConfig    │                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                   Repository Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │ ItemRepository  │  │ JsonFileStorage │                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    Infrastructure                           │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │   JSON Storage  │  │  Virtual Threads│                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

- **Controllers**: REST endpoints with comprehensive Swagger documentation
- **Services**: Business logic with async processing using Virtual Threads
- **Repositories**: Data access layer with thread-safe CAS operations
- **Infrastructure**: JSON file storage with atomic operations and debouncing
- **DTOs**: Data transfer objects with validation annotations
- **Exceptions**: Custom exception hierarchy for proper error handling
- **Mappers**: Entity-DTO conversion utilities

## 🛠️ Technology Stack

### Core Technologies
- **Java 21** - Latest LTS with Virtual Threads support
- **Spring Boot 3.5.6** - Modern Spring framework
- **Maven** - Dependency management and build tool
- **Lombok** - Boilerplate code reduction

### Key Dependencies
- **Spring Web** - REST API development
- **Spring Validation** - Input validation
- **SpringDoc OpenAPI** - API documentation
- **Jackson** - JSON serialization/deserialization
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions

### Development Tools
- **Spring Boot DevTools** - Development productivity
- **Maven Wrapper** - Consistent build environment
- **Logback** - Structured logging

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.6+** (or use included Maven wrapper)
- **Git** for version control

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/josequiroga96/mp_challenge.git
cd mp_challenge
```

### 2. Build the Application
```bash
# Using Maven wrapper (recommended)
./mvnw clean install

# Or using system Maven
mvn clean install
```

### 3. Run the Application
```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using system Maven
mvn spring-boot:run

# Or run the JAR directly
java -jar target/challenge-1.0.0-SNAPSHOT.jar
```

### 4. Access the Application
- **API Base URL**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Documentation**: `http://localhost:8080/api-docs`

## 📚 API Documentation

### Base Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/health` | Health check endpoint |
| `POST` | `/api/items` | Create a new item |
| `GET` | `/api/items` | Get all items |
| `GET` | `/api/items/{id}` | Get item by ID |
| `PUT` | `/api/items/{id}` | Update item |
| `PATCH` | `/api/items/{id}` | Partial update item |
| `DELETE` | `/api/items/{id}` | Delete item |
| `GET` | `/api/items/search` | Search items by name |
| `GET` | `/api/items/search/price` | Search by price range |
| `GET` | `/api/items/search/rating` | Search by minimum rating |
| `GET` | `/api/items/count` | Get item count |
| `GET` | `/api/items/{id}/exists` | Check if item exists |
| `POST` | `/api/items/{id}/specifications` | Add specifications |
| `POST` | `/api/items/batch` | Get items by IDs |

### Example Requests

#### Create Item
```bash
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High-performance gaming laptop",
    "imageUrl": "https://example.com/laptop.jpg",
    "price": 1999.99,
    "rating": 4.8,
    "specifications": ["RTX 4080", "32GB RAM", "1TB SSD"]
  }'
```

#### Search Items by Price Range
```bash
curl "http://localhost:8080/api/items/search/price?minPrice=100&maxPrice=500"
```

#### Partial Update (PATCH)
```bash
curl -X PATCH http://localhost:8080/api/items/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "price": 1799.99,
    "rating": 4.9
  }'
```

## 🧪 Testing

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Classes
```bash
# Controller tests
./mvnw test -Dtest=ItemControllerTest

# Service tests
./mvnw test -Dtest=ItemServiceTest

# Repository tests
./mvnw test -Dtest=ItemRepositoryCASTest
```

### Test Coverage
The project includes comprehensive test coverage:
- **37 Controller Tests** - All endpoints and error scenarios
- **40+ Service Tests** - Business logic and async operations
- **11 Infrastructure Tests** - Storage and CAS operations
- **2 Health Controller Tests** - Health check functionality
- **1 Integration Test** - Full application context

## 🏛️ Architecture Details

### Virtual Threads Implementation
The application leverages Java 21's Virtual Threads for high-performance async operations:

```java
@Bean
public Executor virtualThreadExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setVirtualThreads(true);
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(200);
    return executor;
}
```

### Compare-and-Swap Storage
Thread-safe JSON storage using atomic operations:

```java
public Item save(Item item) {
    UnaryOperator<ItemCollection> updateFunction = createSaveUpdateFunction(item);
    ItemCollection updatedData = storage.updateAtomically(updateFunction);
    return retrieveSavedItem(item, updatedData);
}
```

### Exception Handling
Comprehensive exception hierarchy with proper HTTP status mapping:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleItemNotFound(ItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ex.toErrorResponse());
    }
}
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/mp/challenge/
│   │   ├── components/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── dtos/            # Data Transfer Objects
│   │   │   ├── exceptions/      # Custom exceptions
│   │   │   ├── handlers/        # Global exception handler
│   │   │   ├── infrastructure/  # Storage infrastructure
│   │   │   └── mappers/         # Entity-DTO mappers
│   │   ├── controllers/         # REST controllers
│   │   ├── models/              # Domain models
│   │   ├── repositories/        # Data access layer
│   │   └── services/            # Business logic layer
│   └── resources/
│       ├── application.properties
│       ├── data/items.json      # JSON storage file
│       └── logback-spring.xml   # Logging configuration
└── test/
    └── java/com/mp/challenge/   # Comprehensive test suite
```

## 🔒 Security Considerations

- **Input Validation** - All inputs validated using Jakarta Bean Validation
- **SQL Injection Prevention** - No SQL queries (JSON storage)
- **XSS Protection** - Proper content type handling
- **Error Information** - Sanitized error messages to prevent information leakage

## 📊 Performance Features

- **Virtual Threads** - Efficient async processing
- **Atomic Operations** - Lock-free reads with CAS updates
- **Debounced Persistence** - Batched file writes for performance
- **Connection Pooling** - Optimized thread pool configuration
- **Memory Management** - Efficient object lifecycle management

## 🐛 Troubleshooting

### Common Issues

1. **Java Version Mismatch**
   ```bash
   java -version  # Ensure Java 21+
   ```

2. **Port Already in Use**
   ```bash
   # Change port in application.properties
   server.port=8081
   ```

3. **Storage File Permissions**
   ```bash
   # Ensure write permissions for storage directory
   chmod 755 src/main/resources/data/
   ```

### Logs
Application logs are available in:
- `logs/application.log` - General application logs
- `logs/error.log` - Error-specific logs
- `logs/json-storage.log` - Storage operation logs

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Jose Quiroga**
- Email: jmquiroga1996@gmail.com
- GitHub: [@josequiroga96](https://github.com/josequiroga96)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Java team for Virtual Threads in Java 21
- Open source community for the amazing tools and libraries

---

**Built with ❤️ using Java 21, Spring Boot 3.5.6, and modern software engineering practices.**
