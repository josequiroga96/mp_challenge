# How to Run the MercadoLibre Challenge Project

## 🚀 Quick Start

### Prerequisites
- **Java 21** or higher
- **Maven 3.6+**
- **Git** (optional, for cloning)

### 1. Compile and Run
```bash
# Compile the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

### 2. Verify it Works
```bash
# Health check
curl http://localhost:8080/public/health

# List products
curl http://localhost:8080/api/items
```

## 📋 Detailed Instructions

### Option 1: From Source Code

#### 1. Clone Repository
```bash
git clone https://github.com/josequiroga96/mp_challenge.git
cd mp_challenge
```

#### 2. Compile Project
```bash
mvn clean compile
```

#### 3. Run Tests (Optional)
```bash
mvn test
```

#### 4. Run Application
```bash
mvn spring-boot:run
```

### Option 2: Executable JAR

#### 1. Generate JAR
```bash
mvn clean package
```

#### 2. Run JAR
```bash
java -jar target/challenge-0.0.1-SNAPSHOT.jar
```

## 🔧 Configuration

### Environment Variables (Optional)
```bash
export SERVER_PORT=8080
export LOGGING_LEVEL_COM_MP_CHALLENGE=INFO
```

### Configuration File
The application uses `src/main/resources/application.properties`:
```properties
server.port=8080
logging.level.com.mp.challenge=INFO
```

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Load Testing (Optional)
```bash
cd docs/performance-tests
./run-load-tests.sh smoke
```

## 📊 Available Endpoints

### Health Check
```bash
GET http://localhost:8080/public/health
```

### CRUD Operations
```bash
# Create product
POST http://localhost:8080/api/items
Content-Type: application/json
{
  "name": "Gaming Laptop",
  "description": "High-performance gaming laptop",
  "imageUrl": "https://example.com/laptop.jpg",
  "price": 1699.99,
  "rating": 4.8,
  "specifications": ["16GB RAM", "RTX 4080"]
}

# Get all products
GET http://localhost:8080/api/items

# Get product by ID
GET http://localhost:8080/api/items/{id}

# Update product (full)
PUT http://localhost:8080/api/items/{id}
Content-Type: application/json
{
  "name": "Updated Laptop",
  "description": "Updated description",
  "price": 1799.99,
  "rating": 4.9
}

# Partial update
PATCH http://localhost:8080/api/items/{id}
Content-Type: application/json
{
  "price": 1599.99
}

# Delete product
DELETE http://localhost:8080/api/items/{id}
```

### Search Operations
```bash
# Search by name
GET http://localhost:8080/api/items/search?name=laptop

# Search by minimum rating
GET http://localhost:8080/api/items/search/rating?minRating=4.0

# Search by price range
GET http://localhost:8080/api/items/search/price?minPrice=100&maxPrice=500

# Count products
GET http://localhost:8080/api/items/count
```

### Batch Operations
```bash
# Get multiple products by IDs
POST http://localhost:8080/api/items/batch
Content-Type: application/json
{
  "ids": ["id1", "id2", "id3"]
}

# Add specifications to a product
POST http://localhost:8080/api/items/{id}/specifications
Content-Type: application/json
{
  "specifications": ["32GB RAM", "RTX 4090", "2TB SSD"]
}
```

## 📚 API Documentation with Swagger

### Access Swagger UI
Once the application is running, you can access the interactive API documentation at:

**Swagger UI**: http://localhost:8080/swagger-ui.html

### Swagger Features
- **Interactive API Explorer**: Test all endpoints directly from the browser
- **Request/Response Examples**: See sample data for all operations
- **Schema Documentation**: Complete data models and validation rules
- **Authentication Testing**: Test different scenarios and error cases
- **Export Options**: Download OpenAPI specification

### What You Can Do in Swagger UI
1. **Browse all endpoints** organized by functionality
2. **Try out operations** with real data
3. **View request/response schemas** with examples
4. **Test different scenarios** (success, error cases)
5. **Download OpenAPI spec** for integration

### Swagger Endpoints
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

### Example Swagger Usage
1. Open http://localhost:8080/swagger-ui.html
2. Click on any endpoint (e.g., `POST /api/items`)
3. Click "Try it out"
4. Fill in the request body with sample data
5. Click "Execute" to test the endpoint
6. View the response and status code

## 🐛 Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# Change port
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

#### Memory Issues
```bash
# Increase JVM memory
export MAVEN_OPTS="-Xmx2g -Xms1g"
mvn spring-boot:run
```

#### Detailed Logs
```bash
# Run with debug logs
mvn spring-boot:run -Dlogging.level.com.mp.challenge=DEBUG
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
├── run.md                  # This file
└── pom.xml                 # Maven configuration
```

## 🔍 Verification

### 1. Health Check
```bash
curl -s http://localhost:8080/public/health | jq
```
**Expected response:**
```json
{
  "status": "UP",
  "timestamp": "2025-10-18T10:30:00Z"
}
```

### 2. Create Test Product
```bash
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Laptop",
    "description": "Test description",
    "price": 999.99,
    "rating": 4.5
  }'
```

### 3. List Products
```bash
curl -s http://localhost:8080/api/items | jq
```

### 4. Access Swagger UI
Open your browser and go to: http://localhost:8080/swagger-ui.html

## 📈 Monitoring

### Application Logs
Logs are saved in:
- `logs/application.log` - General logs
- `logs/error.log` - Error logs only

### Performance Metrics
To run load tests:
```bash
cd docs/performance-tests
./run-load-tests.sh basic
```

## 🆘 Support

If you have issues:

1. **Check Java 21**: `java -version`
2. **Check Maven**: `mvn -version`
3. **Check logs**: `tail -f logs/application.log`
4. **Check port**: `lsof -i :8080`
5. **Access Swagger**: http://localhost:8080/swagger-ui.html

**Contact**: jose.quiroga96@gmail.com