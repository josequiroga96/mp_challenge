# MercadoLibre Challenge - Product Management REST API

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A complete REST API for product management developed with Spring Boot 3.5.6 and Java 21, designed to demonstrate best practices in software development, error handling, documentation, and testing.

## 🚀 Key Features

- **Complete REST API** with CRUD operations
- **Advanced search** by name, rating, and price range
- **Batch operations** for efficiency
- **JSON file persistence** with thread-safe operations (CAS)
- **Virtual Threads** for high concurrency
- **Complete documentation** with architecture diagrams
- **Automated load testing suite**
- **Robust error handling** with consistent HTTP codes
- **Interactive API documentation** with Swagger UI
- **Comprehensive performance analysis**

## 📊 Performance Metrics

- **Throughput**: Up to 10,000+ RPS in write operations
- **P50 Latency**: 12ms for typical operations
- **P99 Latency**: 22ms for typical operations
- **Concurrency**: Supports up to 200 concurrent users without degradation
- **Availability**: 99.9% with integrated health checks

## 🛠️ Technology Stack

### Backend
- **Spring Boot 3.5.6** - Main framework
- **Java 21** - Language with Virtual Threads
- **Spring Web** - REST endpoints
- **SpringDoc OpenAPI** - Interactive documentation with Swagger UI
- **Jackson** - Custom JSON serialization

### Persistence
- **JSON File Storage** - JSON file persistence
- **Compare-and-Swap (CAS)** - Thread-safe operations
- **ConcurrentHashMap** - In-memory concurrency handling

### Testing
- **JUnit 5** - Unit tests
- **Spring Boot Test** - Integration tests
- **Apache Bench (ab)** - Load testing

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- Git (optional)

### 1. Clone and Compile
```bash
git clone https://github.com/josequiroga96/mp_challenge.git
cd mp_challenge
mvn clean compile
```

### 2. Run Application
```bash
mvn spring-boot:run
```

### 3. Verify it Works
```bash
# Health check
curl http://localhost:8080/public/health

# List products
curl http://localhost:8080/api/items
```

### 4. Access Swagger UI
Open your browser and go to: **http://localhost:8080/swagger-ui.html**

## 📚 Documentation

### Main Documents
- **[run.md](run.md)** - Detailed execution instructions
- **[docs/PROJECT_PLAN.md](docs/PROJECT_PLAN.md)** - Complete project plan
- **[docs/DIAGRAMS.md](docs/DIAGRAMS.md)** - Architecture diagrams
- **[docs/PERFORMANCE.md](docs/PERFORMANCE.md)** - Performance analysis
- **[docs/PROMPTS.md](docs/PROMPTS.md)** - GenAI prompts used

### Interactive API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Specification**: http://localhost:8080/v3/api-docs
- **API Docs**: http://localhost:8080/api-docs

## 📊 API Endpoints

### Health Check
```bash
GET /public/health
```

### CRUD Operations
```bash
# Create product
POST /api/items
{
  "name": "Gaming Laptop",
  "description": "High-performance gaming laptop",
  "price": 1699.99,
  "rating": 4.8
}

# Get all products
GET /api/items

# Get product by ID
GET /api/items/{id}

# Update product
PUT /api/items/{id}

# Partial update
PATCH /api/items/{id}

# Delete product
DELETE /api/items/{id}
```

### Search Operations
```bash
# Search by name
GET /api/items/search?name=laptop

# Search by rating
GET /api/items/search/rating?minRating=4.0

# Search by price
GET /api/items/search/price?minPrice=100&maxPrice=500

# Count products
GET /api/items/count
```

### Batch Operations
```bash
# Get multiple products
POST /api/items/batch
{
  "ids": ["id1", "id2", "id3"]
}

# Add specifications
POST /api/items/{id}/specifications
{
  "specifications": ["16GB RAM", "RTX 4080"]
}
```

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Load Testing
```bash
cd docs/performance-tests
./run-load-tests.sh comprehensive
```

### Available Test Types
- **Smoke Test** - Basic functionality verification
- **Basic Load Test** - Standard load testing
- **Scaling Test** - Concurrency scaling tests
- **Spike Test** - Traffic spike tests
- **Stress Test** - System stress tests

## 🏗️ Architecture

### Design Patterns
- **Repository Pattern** - Data access abstraction
- **Service Layer Pattern** - Business logic separation
- **DTO Pattern** - Data transfer between layers
- **Mapper Pattern** - DTO to entity conversion
- **Global Exception Handler** - Centralized error handling

### Architecture Diagrams
For detailed diagrams, see [docs/DIAGRAMS.md](docs/DIAGRAMS.md):
- C4 Context, Container and Component diagrams
- Sequence diagrams for all operations
- Concurrency and performance diagrams

## 📈 Performance

### Key Metrics
- **Read Operations**: 7,000+ RPS
- **Write Operations**: 5,000+ RPS
- **P50 Latency**: 12ms
- **P99 Latency**: 22ms
- **Concurrent Users**: 200+ without degradation

### Detailed Analysis
For complete performance analysis, see [docs/PERFORMANCE.md](docs/PERFORMANCE.md).

## 🤖 GenAI Tools Used

### ChatGPT 5 (OpenAI)
- Initial architecture design for JSON persistence
- Concurrency patterns and Compare-and-Swap (CAS)
- Design decisions for thread-safe operations

### Claude Sonnet 4 (Anthropic)
- Code development and refactoring
- Performance analysis and optimizations
- Technical documentation generation
- Architecture diagram creation

### Cursor IDE
- Main editor with AI integration
- Intelligent autocompletion
- Assisted refactoring

### GitHub Copilot
- Real-time code suggestions
- Consistent code patterns

For detailed prompts used, see [docs/PROMPTS.md](docs/PROMPTS.md).

## 🔧 Configuration

### Environment Variables
```bash
export SERVER_PORT=8080
export LOGGING_LEVEL_COM_MP_CHALLENGE=INFO
```

### Configuration File
`src/main/resources/application.properties`:
```properties
server.port=8080
logging.level.com.mp.challenge=INFO
```

## 📁 Project Structure

```
challenge/
├── src/main/java/com/mp/challenge/
│   ├── controllers/          # REST Controllers
│   ├── services/            # Business Logic
│   ├── repositories/        # Data Access
│   ├── models/             # Entities and DTOs
│   └── components/         # Auxiliary Components
├── src/main/resources/
│   ├── application.properties
│   └── data/               # JSON data files
├── src/test/               # Unit tests
├── docs/                   # Complete documentation
├── run.md                  # Execution instructions
└── pom.xml                 # Maven configuration
```

## 🐛 Troubleshooting

### Common Issues

#### Port Already in Use
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

#### Memory Issues
```bash
export MAVEN_OPTS="-Xmx2g -Xms1g"
mvn spring-boot:run
```

#### Detailed Logs
```bash
mvn spring-boot:run -Dlogging.level.com.mp.challenge=DEBUG
```

## 📊 Monitoring

### Logs
- `logs/application.log` - General logs
- `logs/error.log` - Error logs only

### Metrics
- Health check: `GET /public/health`
- Load testing: `docs/performance-tests/run-load-tests.sh`
- Swagger UI: http://localhost:8080/swagger-ui.html

## 🚀 Next Steps

### Planned Optimizations
- Implement caching for read operations
- Optimize price search (current P99: 152ms)
- Consider database migration for greater scalability

### Future Features
- Pagination for large listings
- Advanced search filters
- Authentication and authorization
- API versioning

## 📞 Contact

**Developer**: José Quiroga  
**Email**: jose.quiroga96@gmail.com  
**GitHub**: [josequiroga96](https://github.com/josequiroga96)  
**LinkedIn**: [José Quiroga](https://www.linkedin.com/in/josequiroga96)  

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for more details.

---

**Developed with ❤️ using Spring Boot 3.5.6, Java 21, and GenAI tools**
