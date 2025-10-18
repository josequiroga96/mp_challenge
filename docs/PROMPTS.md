# Prompts Utilizados - MercadoLibre Challenge

Este documento contiene los prompts principales utilizados con herramientas GenAI durante el desarrollo del proyecto.

---

## 🤖 ChatGPT 5

### 1. Diseño Inicial de Persistencia JSON
```
Spring Boot 3.5.6, Java 21:
How to use a JSON file as a database?

For example -> I have a User model:
User(name, surname, email, age,...)

I want the services to create a users.json file (if it does not exist) to record users' data. After that, I will be able to retrieve the data

Usually, I use a service/repository format. For this case, I can't use a real database; for that reason, I want to create a new service to replicate a database logic, but with JSON files

My objective for this task is can provide service using the best practices and the best design patterns
```

### 2. Optimización de Concurrencia
```
I prefer atomic variables and non-blocking algorithms
Is it possible to apply this logic to JsonFileStorage? What is better for this situation, locks or non-blocking?
```

### 3. Implementación de CAS (Compare-and-Swap)
```
I prefer option 3, lock-free for reads + one writer
Let's do that
```

---

## 🤖 Claude Sonnet 4 (Anthropic)

### 1. Optimización de Pruebas de Carga
```
Podemos actualizar el run-suite-plus para que el nombre sea secuencial o algo y siempre lo pueda crear? Mi idea es hacer pruebas de calentamiento, de lectura, escritura, mix, de escalado de concurrencia, spikes, stress. Está bien si llega a romper pero no quiero que falle por cosas de negocio sino porque encontramos los limites
```

### 2. Organización de Resultados
```
Bien, veo que se generaron muchas carpetas, en una esta basic, en otra smoke, en otra stress Podemos poner todos los reportes en una sola carpeta? Para evitar confusión
```

### 3. Generación de Reporte de Performance
```
PERFORMANCE.md quedó deprecado

Con todo los datos nuevos, quiero que en ese md, me pongas el reporte detallado y profesional de lo que sacamos de las pruebas de stress
```

### 4. Creación de Diagramas de Arquitectura
```
Los diagramas 5, 7 y 8 están fallando
```

### 5. Expansión de Diagramas de Secuencia
```
En el punto 4: ## 4) Diagrama de Secuencia — Flujo CRUD típico

Agregame todos los demás flujos por favor, el crud, las busquedas
```

---

## 🎯 Cursor IDE

### 1. Refactoring de Código
```
Refactor this code to use proper DTOs and improve error handling
```

### 2. Generación de Tests
```
Generate comprehensive unit tests for this service class
```

### 3. Optimización de Performance
```
Optimize this method for better performance and add proper logging
```

### 4. Documentación de Código
```
Add comprehensive JavaDoc comments to this class
```

---

## 🔧 Prompts Técnicos Específicos

### 1. Serialización JSON Personalizada
```
I need to create custom Jackson serializers for ItemCollection to maintain Map structure in JSON instead of converting to Array. The current issue is that after write operations, the JSON structure changes from object with UUID keys to array of objects, making it unreadable.
```

### 2. Implementación de Virtual Threads
```
Configure Spring Boot 3.5.6 with Java 21 Virtual Threads for async operations. I need to optimize the AsyncConfig for high concurrency scenarios.
```

### 3. Pruebas de Carga con Apache Bench
```
Create a comprehensive load testing suite using Apache Bench that includes:
- Warmup tests
- Read operations (list, count, search)
- Write operations (create, update, patch, delete)
- Mixed operations
- Concurrency scaling tests
- Spike tests
- Stress tests

The tests should generate unique payloads to avoid business logic conflicts and focus on finding system limits.
```

### 4. Análisis de Performance
```
Analyze these Apache Bench results and create a professional performance report including:
- Throughput analysis (RPS)
- Latency percentiles (P50, P90, P99)
- Concurrency limits
- Performance bottlenecks
- Recommendations for optimization
```

### 5. Diagramas de Arquitectura C4
```
Create C4 architecture diagrams for this Spring Boot application:
- Context diagram (Level 1)
- Container diagram (Level 2) 
- Component diagram (Level 3)
- Sequence diagrams for all CRUD operations
- Performance and scalability diagrams

Use Mermaid syntax and ensure all diagrams are properly formatted.
```

---

## 📊 Prompts de Análisis

### 1. Análisis de Métricas de Performance
```
Explain P50, P90, and P99 latency percentiles in simple terms with examples from our load testing results. Include what these metrics tell us about user experience and system performance.
```

### 2. Identificación de Cuellos de Botella
```
Based on these performance test results, identify the main bottlenecks in our system:
- Search by price operations show P99 of 152ms
- System degrades significantly with >200 concurrent users
- Write operations show high failure rates under stress

Provide specific recommendations for each bottleneck.
```

### 3. Recomendaciones de Escalabilidad
```
Given our current architecture (Spring Boot + JSON file storage + CAS), what are the next steps for scaling to handle 1000+ concurrent users? Consider both immediate optimizations and long-term architectural changes.
```

---

## 🛠️ Prompts de Desarrollo

### 1. Implementación de DTOs
```
Create DTOs for the following operations:
- CreateItemDto (for POST /api/items)
- PatchItemDto (for PATCH /api/items/{id})
- AddSpecificationDto (for POST /api/items/{id}/specifications)

Include proper validation annotations and ensure they follow Spring Boot best practices.
```

### 2. Manejo de Excepciones
```
Implement a comprehensive GlobalExceptionHandler that:
- Handles all custom exceptions
- Returns consistent HTTP status codes
- Provides meaningful error messages
- Includes proper logging
- Follows REST API best practices
```

### 3. Configuración de Logging
```
Configure Logback for this Spring Boot application with:
- Different log levels for different packages
- File rotation based on size and time
- Separate error logs
- Structured logging for better analysis
- Performance logging for monitoring
```

---

## 📈 Prompts de Documentación

### 1. README del Proyecto
```
Create a comprehensive README for this Spring Boot project that includes:
- Project description and features
- Technology stack
- Setup instructions
- API documentation
- Performance metrics
- Architecture overview
- Contributing guidelines
```

### 2. Documentación de API
```
Generate API documentation for all endpoints including:
- HTTP methods and paths
- Request/response examples
- Error codes and messages
- Performance characteristics
- Usage examples with curl commands
```

### 3. Reporte de Performance
```
Create a professional performance report that includes:
- Executive summary
- Test methodology
- Results by test type (smoke, basic, scaling, spike, stress)
- Latency analysis with percentiles
- Throughput analysis
- Concurrency limits
- Bottleneck identification
- Optimization recommendations
- Next steps for scaling
```

---

## 🎯 Prompts de Optimización

### 1. Optimización de Búsquedas
```
The search by price operation shows P99 latency of 152ms, which is significantly higher than other operations. Analyze the current implementation and suggest optimizations to reduce this to under 50ms.
```

### 2. Mejora de Concurrencia
```
Our system currently supports ~200 concurrent users before degradation. What changes can we make to support 500+ concurrent users while maintaining good performance?
```

### 3. Optimización de Serialización
```
The current JSON serialization is causing issues with the file structure. Design a better approach that maintains consistency and performance.
```

---

## 📝 Lecciones Aprendidas

### 1. Efectividad de Prompts
- **Prompts específicos** funcionan mejor que generales
- **Incluir contexto** del problema mejora la calidad de la respuesta
- **Ejemplos concretos** ayudan a la comprensión
- **Iteración** es clave para refinar resultados

### 2. Mejores Prácticas
- **Describir el problema** antes de pedir la solución
- **Incluir código relevante** cuando sea necesario
- **Especificar el formato** de salida deseado
- **Pedir explicaciones** para entender el razonamiento

### 3. Herramientas Complementarias
- **Claude** excelente para análisis y arquitectura
- **Cursor** ideal para refactoring y generación de código
- **GitHub Copilot** útil para completar patrones
- **Mermaid** perfecto para diagramas técnicos

---

**Documento generado**: Octubre 2025  
**Herramientas utilizadas**: ChatGPT 5, Claude Sonnet 4, Cursor IDE, GitHub Copilot  
**Total de prompts documentados**: 28+
