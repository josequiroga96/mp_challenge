# MercadoLibre Challenge Application — Diagramas de Diseño

Este documento resume la arquitectura y los flujos clave de la API (Spring Boot 3.5.6, Java 21 VT, persistencia JSON con CAS). Los diagramas están en **Mermaid** para que puedas renderizarlos en GitHub, VS Code, Obsidian o tu wiki.

---

## 1) C4 — Contexto (Nivel 1)

```mermaid
C4Context
    title Contexto: Sistema de Gestión de Productos
    Person(user, "Cliente/Consumidor de API", "Front web, Postman, AB")
    System_Boundary(sys, "MercadoLibre") {
        System(api, "CHALLENGE APPLICATION", "Spring Boot 3.5.6, Java 21 VT")
    }

    Rel(user, api, "HTTP/JSON")
```

---

## 2) C4 — Contenedores (Nivel 2)

```mermaid
C4Container
    title Contenedores principales
    Person(user, "Cliente API")
    System_Boundary(app, "MercadoLibre Challenge Application") {
        Container(controller, "Controllers", "Spring Web", "Expone endpoints REST (ItemController, HealthController)")
        Container(service, "Services", "Java 21", "Lógica de negocio, validaciones, orquestación, VTs")
        Container(repo, "Repository", "Java 21", "Abstracción de acceso a datos")
        Container(storage, "Storage", "Java 21 + Jackson", "Lectura/escritura JSON, CAS thread-safe")
    }

    Rel(user, controller, "HTTP/JSON")
    Rel(controller, service, "Invocación directa")
    Rel(service, repo, "CRUD + búsquedas")
    Rel(repo, storage, "CAS read/write")
```

---

## 3) C4 — Componentes (Nivel 3)

```mermaid
C4Component
    title Componentes de la capa de aplicación
    Person(user, "Cliente API")
    
    System_Boundary(app, "MercadoLibre Challenge Application") {
        Container_Boundary(controllers, "Controllers Layer") {
            Component(healthCtrl, "HealthController", "Spring Web", "GET /public/health")
            Component(itemCtrl, "ItemController", "Spring Web", "CRUD + búsquedas + batch + specs")
        }
        
        Container_Boundary(services, "Services Layer") {
            Component(itemService, "ItemService", "Java 21", "Reglas de negocio, validaciones, orquestación")
        }

        Container_Boundary(storage, "Storage Layer") {
            Component(fileCAS, "JsonFileStorageCAS", "Java 21 + Jackson", "Serialización, fsync/flush, CAS")
        }
        
        Container_Boundary(repositories, "Repository Layer") {
            Component(itemRepo, "ItemRepositoryCAS", "Java 21", "Abstracción de acceso a datos, CAS")
        }

        Container_Boundary(database, "Persistence") {
            SystemDb(file, "JSON File", "File Persistence")
        }
    }
    
    Rel(user, healthCtrl, "HTTP GET", "Health check")
    Rel(user, itemCtrl, "HTTP/JSON", "CRUD operations")
    Rel(itemCtrl, itemService, "Method calls", "Business logic")
    Rel(itemService, itemRepo, "Method calls", "Data access")
    Rel(itemRepo, fileCAS, "CAS operations", "Thread-safe I/O")
    Rel(fileCAS, file, "Read/Write")
```

---

## 4) Diagramas de Secuencia — Flujos Completos

### 4.1) Health Check

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant HC as HealthController

    C->>HC: GET /public/health
    HC-->>C: 200 OK {status: "UP"}
```

### 4.2) Create Item

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant IC as ItemController
    participant IS as ItemService
    participant MR as ItemMapper
    participant IR as ItemRepositoryCAS
    participant FS as JsonFileStorageCAS

    C->>IC: POST /api/items {CreateItemDto}
    IC->>IS: createItem(dto)
    IS->>MR: toDomain(dto)
    MR-->>IS: Item (con UUID generado)
    IS->>IR: save(Item) (virtual thread)
    IR->>FS: CAS write -> items.json
    FS-->>IR: OK (nuevo estado)
    IR-->>IS: Item (persistido)
    IS->>MR: toDto(Item)
    MR-->>IS: ItemDto
    IS-->>IC: ItemDto
    IC-->>C: 201 Created {ItemDto}
```

### 4.3) Read Item (Get by ID)

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant IC as ItemController
    participant IS as ItemService
    participant IR as ItemRepositoryCAS
    participant FS as JsonFileStorageCAS

    C->>IC: GET /api/items/{id}
    IC->>IS: getItemById(id)
    IS->>IR: findById(id) (virtual thread)
    IR->>FS: CAS read -> items.json
    FS-->>IR: ItemCollection
    IR-->>IS: Item (si existe)
    IS-->>IC: ItemDto
    IC-->>C: 200 OK {ItemDto} o 404 Not Found
```

### 4.4) Read All Items

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant IC as ItemController
    participant IS as ItemService
    participant IR as ItemRepositoryCAS
    participant FS as JsonFileStorageCAS

    C->>IC: GET /api/items
    IC->>IS: getAllItems()
    IS->>IR: findAll() (virtual thread)
    IR->>FS: CAS read -> items.json
    FS-->>IR: ItemCollection
    IR-->>IS: List<Item>
    IS-->>IC: List<ItemDto>
    IC-->>C: 200 OK [ItemDto...]
```

### 4.5) Update Item (PUT)

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant IC as ItemController
    participant IS as ItemService
    participant MR as ItemMapper
    participant IR as ItemRepositoryCAS
    participant FS as JsonFileStorageCAS

    C->>IC: PUT /api/items/{id} {UpdateItemDto}
    IC->>IS: updateItem(id, dto)
    IS->>IR: findById(id) (virtual thread)
    IR->>FS: CAS read -> items.json
    FS-->>IR: ItemCollection
    IR-->>IS: Item (existente)
    IS->>MR: updateFromDto(Item, dto)
    MR-->>IS: Item (actualizado)
    IS->>IR: save(Item) (virtual thread)
    IR->>FS: CAS write -> items.json
    FS-->>IR: OK (nuevo estado)
    IR-->>IS: Item (persistido)
    IS->>MR: toDto(Item)
    MR-->>IS: ItemDto
    IS-->>IC: ItemDto
    IC-->>C: 200 OK {ItemDto} o 404 Not Found
```

### 4.6) Patch Item (PATCH)

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant IC as ItemController
    participant IS as ItemService
    participant MR as ItemMapper
    participant IR as ItemRepositoryCAS
    participant FS as JsonFileStorageCAS

    C->>IC: PATCH /api/items/{id} {PatchItemDto}
    IC->>IS: patchItem(id, dto)
    IS->>IR: findById(id) (virtual thread)
    IR->>FS: CAS read -> items.json
    FS-->>IR: ItemCollection
    IR-->>IS: Item (existente)
    IS->>MR: patchFromDto(Item, dto)
    MR-->>IS: Item (parcialmente actualizado)
    IS->>IR: save(Item) (virtual thread)
    IR->>FS: CAS write -> items.json
    FS-->>IR: OK (nuevo estado)
    IR-->>IS: Item (persistido)
    IS->>MR: toDto(Item)
    MR-->>IS: ItemDto
    IS-->>IC: ItemDto
    IC-->>C: 200 OK {ItemDto} o 404 Not Found
```

### 4.7) Delete Item

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant IC as ItemController
    participant IS as ItemService
    participant IR as ItemRepositoryCAS
    participant FS as JsonFileStorageCAS

    C->>IC: DELETE /api/items/{id}
    IC->>IS: deleteItem(id)
    IS->>IR: findById(id) (virtual thread)
    IR->>FS: CAS read -> items.json
    FS-->>IR: ItemCollection
    IR-->>IS: Item (si existe)
    IS->>IR: deleteById(id) (virtual thread)
    IR->>FS: CAS write -> items.json
    FS-->>IR: OK (nuevo estado)
    IR-->>IS: true
    IS-->>IC: true
    IC-->>C: 204 No Content o 404 Not Found
```

### 4.8) Search by Name

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant IC as ItemController
    participant IS as ItemService
    participant IR as ItemRepositoryCAS
    participant FS as JsonFileStorageCAS

    C->>IC: GET /api/items/search?name=query
    IC->>IS: searchByName(name)
    IS->>IR: findByNameContaining(name) (virtual thread)
    IR->>FS: CAS read -> items.json
    FS-->>IR: ItemCollection
    IR-->>IS: List<Item> (filtrados)
    IS-->>IC: List<ItemDto>
    IC-->>C: 200 OK [ItemDto...]
```

### 4.9) Search by Rating

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant IC as ItemController
    participant IS as ItemService
    participant IR as ItemRepositoryCAS
    participant FS as JsonFileStorageCAS

    C->>IC: GET /api/items/search/rating?minRating=4.0
    IC->>IS: searchByRating(minRating)
    IS->>IR: findByRatingGreaterThanEqual(minRating) (virtual thread)
    IR->>FS: CAS read -> items.json
    FS-->>IR: ItemCollection
    IR-->>IS: List<Item> (filtrados)
    IS-->>IC: List<ItemDto>
    IC-->>C: 200 OK [ItemDto...]
```

### 4.10) Search by Price Range

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant IC as ItemController
    participant IS as ItemService
    participant IR as ItemRepositoryCAS
    participant FS as JsonFileStorageCAS

    C->>IC: GET /api/items/search/price?minPrice=100&maxPrice=500
    IC->>IS: searchByPriceRange(minPrice, maxPrice)
    IS->>IR: findByPriceBetween(minPrice, maxPrice) (virtual thread)
    IR->>FS: CAS read -> items.json
    FS-->>IR: ItemCollection
    IR-->>IS: List<Item> (filtrados)
    IS-->>IC: List<ItemDto>
    IC-->>C: 200 OK [ItemDto...]
```

### 4.11) Count Items

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant IC as ItemController
    participant IS as ItemService
    participant IR as ItemRepositoryCAS
    participant FS as JsonFileStorageCAS

    C->>IC: GET /api/items/count
    IC->>IS: getItemCount()
    IS->>IR: count() (virtual thread)
    IR->>FS: CAS read -> items.json
    FS-->>IR: ItemCollection
    IR-->>IS: Long (count)
    IS-->>IC: Long
    IC-->>C: 200 OK {count: 42}
```

### 4.12) Batch Read Items

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant IC as ItemController
    participant IS as ItemService
    participant IR as ItemRepositoryCAS
    participant FS as JsonFileStorageCAS

    C->>IC: POST /api/items/batch {ids: [id1, id2, id3]}
    IC->>IS: getItemsByIds(ids)
    IS->>IR: findAllByIds(ids) (virtual thread)
    IR->>FS: CAS read -> items.json
    FS-->>IR: ItemCollection
    IR-->>IS: List<Item> (filtrados por IDs)
    IS-->>IC: List<ItemDto>
    IC-->>C: 200 OK [ItemDto...]
```

### 4.13) Add Specifications

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente
    participant IC as ItemController
    participant IS as ItemService
    participant IR as ItemRepositoryCAS
    participant FS as JsonFileStorageCAS

    C->>IC: POST /api/items/{id}/specifications {AddSpecificationDto}
    IC->>IS: addSpecifications(id, dto)
    IS->>IR: findById(id) (virtual thread)
    IR->>FS: CAS read -> items.json
    FS-->>IR: ItemCollection
    IR-->>IS: Item (existente)
    IS->>IS: addSpecifications(Item, dto.specifications)
    IS->>IR: save(Item) (virtual thread)
    IR->>FS: CAS write -> items.json
    FS-->>IR: OK (nuevo estado)
    IR-->>IS: Item (persistido)
    IS-->>IC: ItemDto
    IC-->>C: 200 OK {ItemDto} o 404 Not Found
```

---

## 5) Concurrencia — CAS y Virtual Threads

```mermaid
flowchart TD
    subgraph VT["Virtual Threads (Java 21)"]
      A1["VT#1 Update Item X"] -->|CAS| S[("JsonFileStorageCAS")]
      A2["VT#2 Update Item X"] -->|CAS| S
      A3["VT#3 Update Item Y"] -->|CAS| S
    end

    S -->|"Comparar estado actual vs esperado"| D[("file.json (snapshot actual)")]
    S -->|"Si coincide"| W["Escritura atomica con fsync/flush (opcional)"]
    S -->|"Si no coincide"| R["Retry o 409 segun politica"]
```

**Notas**  
- **CAS** evita condiciones de carrera sin locks globales largos.  
- **VT** permite gran cantidad de operaciones concurrentes con bajo overhead.  

---

## 6) Modelo de Datos (ER básico)

```mermaid
erDiagram
    ITEM ||--o{ SPECIFICATION : has
    ITEM {
      UUID id PK
      string name
      string description
      string imageUrl
      decimal price
      float rating
      datetime createdAt
      datetime updatedAt
    }
    SPECIFICATION {
      UUID itemId FK
      string value
    }
```

> Implementación real: `ItemCollection` utilizando `ConcurrentHashMap<UUID, Item>`; serialización **custom** para preservar estructura de mapa en JSON.

---

## 7) Endpoints — Mapa lógico

```mermaid
flowchart TB
    subgraph READS["Lecturas"]
      L1["GET /api/items"]
      L2["GET /api/items/count"]
      L3["GET /api/items/search?name="]
      L4["GET /api/items/search/rating?minRating="]
      L5["GET /api/items/search/price?minPrice=&maxPrice="]
      L6["POST /api/items/batch"]
    end
    subgraph WRITES["Escrituras"]
      W1["POST /api/items"]
      W2["PUT /api/items/{id}"]
      W3["PATCH /api/items/{id}"]
      W4["POST /api/items/{id}/specifications"]
      W5["DELETE /api/items/{id}"]
    end

    L1 -->|Service| Ls[("Repository CAS")]
    L2 --> Ls
    L3 --> Ls
    L4 --> Ls
    L5 --> Ls
    L6 --> Ls
    W1 --> Ls
    W2 --> Ls
    W3 --> Ls
    W4 --> Ls
    W5 --> Ls
```

---

## 8) Despliegue Monolítico

```mermaid
flowchart TD
    subgraph Host["Host (VM/Container)"]
      App["Spring Boot (jar)<br/>Java 21 + VT"]
      Logs[("logback-spring.xml")]
      Data[("data/*.json")]
    end
    Client["Cliente / AB / Postman"] -->|"HTTP 8080"| App
    App --> Logs
    App --> Data
```

---

## 9) Performance — Lecturas y Concurrencia

```mermaid
graph TD
    A[Lecturas típicas] --> B[p50 ~ 12 ms]
    A --> C[p99 ~ 22 ms]
    D[Concurrencia] --> E[Estable hasta ~200 usuarios]
    E --> F[Degradación desde ~200]
    F --> G[Límite alrededor de ~500]
    G --> H[Fallo cerca de ~2000]
```

> Datos a modo ilustrativo para contextualizar el diagrama, basados en los reportes de Apache Benchmark.

---

## 10) Manejo de Errores

```mermaid
flowchart TD
    subgraph API
      X[GlobalExceptionHandler]
      Y[Custom Exceptions]
    end
    Req[Request inválido] --> Y --> X --> Resp[HTTP 4xx JSON]
    Err[Error interno] --> X --> Resp5[HTTP 5xx JSON]
```

**Beneficios**: respuestas homogéneas, logging centralizado, trazabilidad.

---

## 11) Patrones Clave

- **Repository Pattern**: swap de persistencia sin impactar capa de negocio.  
- **Service Layer**: testabilidad, separación de concerns.  
- **DTO + Mapper**: versionado, contratos claros, serialización controlada.  
- **Global Exception Handler**: errores consistentes.  
- **CAS + VT**: concurrencia eficiente sin locks gruesos.

---
