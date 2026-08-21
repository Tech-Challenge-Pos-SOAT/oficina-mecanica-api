# GET /api/materials/{id} Endpoint Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a new REST endpoint `GET /api/materials/{id}` to retrieve details of a single material by its unique ID.

**Architecture:** Following DDD and Hexagonal architecture, this endpoint uses a dedicated Application Use Case (`GetMaterialUseCase`) that interacts with the `MaterialRepository`. If the material is found, it is mapped to a REST DTO and returned. If not, a domain-specific `MaterialNotFoundException` is thrown, which is mapped to a 404 Not Found response in the `GlobalExceptionHandler`.

**Tech Stack:** Java 21, Spring Boot 3.5.x, MapStruct, JUnit 5, Mockito, AssertJ, Spring MockMvc.

**Spec:** Approved in chat (Bounded task design).

## Global Constraints

- Keep the domain pure (no HTTP dependencies in domain/application packages).
- Use MapStruct for DTO mappings and JPA conversions.
- Adhere to the established coding styles, directory structures, and Portuguese error messages.
- Always run specific unit tests via `mvn test -Dtest=ClassName` to bypass Testcontainers/Docker requirements if Docker is not running in the environment.

---

### Task 1: Domain Exception - `MaterialNotFoundException`

Create a new domain-specific exception to represent when a material is not found by ID.

**Files:**
- Create: `src/main/java/com/postech/oficinamecanica/domain/material/MaterialNotFoundException.java`

**Interfaces:**
- Consumes: Material ID (`Long`)
- Produces: `MaterialNotFoundException` instance extending `RuntimeException`

- [ ] **Step 1: Write the domain exception**

```java
package com.postech.oficinamecanica.domain.material;

public class MaterialNotFoundException extends RuntimeException {
    private final Long id;

    public MaterialNotFoundException(Long id) {
        super("Material não encontrado com o ID: " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/postech/oficinamecanica/domain/material/MaterialNotFoundException.java
git commit -m "feat(domain): add MaterialNotFoundException"
```

---

### Task 2: Application Repository Boundary - Add `findById` to `MaterialRepository`

Add the `findById` method to the repository interface and implement it in the persistence layer.

**Files:**
- Modify: `src/main/java/com/postech/oficinamecanica/application/material/MaterialRepository.java`
- Modify: `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialRepositoryImpl.java`

**Interfaces:**
- Consumes: Material ID (`Long`)
- Produces: `Optional<Material>`

- [ ] **Step 1: Update the repository interface**

In `MaterialRepository.java`, add the import and the method declaration:

```java
import java.util.Optional;
```

```java
Optional<Material> findById(Long id);
```

- [ ] **Step 2: Implement findById in MaterialRepositoryImpl**

In `MaterialRepositoryImpl.java`, implement the method using `jpaRepository` and the existing `mapper`:

```java
    @Override
    public Optional<Material> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
```

- [ ] **Step 3: Compile the application**

Run: `mvn compile`
Expected: Successful compilation without errors.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/postech/oficinamecanica/application/material/MaterialRepository.java src/main/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialRepositoryImpl.java
git commit -m "feat(persistence): implement findById in MaterialRepository"
```

---

### Task 3: Use Case Implementation - `GetMaterialUseCase`

Implement the application service use case with TDD.

**Files:**
- Create: `src/main/java/com/postech/oficinamecanica/application/material/GetMaterialUseCase.java`
- Create: `src/test/java/com/postech/oficinamecanica/application/material/GetMaterialUseCaseTest.java`

**Interfaces:**
- Consumes: `MaterialRepository` (via constructor)
- Produces: `GetMaterialUseCase.execute(Long id)` returning `Material` or throwing `MaterialNotFoundException`

- [ ] **Step 1: Write the failing tests first**

Create `src/test/java/com/postech/oficinamecanica/application/material/GetMaterialUseCaseTest.java`:

```java
package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.material.MaterialNotFoundException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMaterialUseCaseTest {

    @Mock
    private MaterialRepository repository;

    @InjectMocks
    private GetMaterialUseCase useCase;

    @Test
    void shouldReturnMaterialWhenFoundById() {
        Long id = 1L;
        Material expectedMaterial = new Material(
            id, "Filtro de Óleo", "Filtro Bosch", new BigDecimal("32.50"),
            25, 5, EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(repository.findById(id)).thenReturn(Optional.of(expectedMaterial));

        Material actualMaterial = useCase.execute(id);

        assertThat(actualMaterial).isEqualTo(expectedMaterial);
        verify(repository).findById(id);
    }

    @Test
    void shouldThrowExceptionWhenMaterialNotFound() {
        Long id = 999L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(id))
            .isInstanceOf(MaterialNotFoundException.class)
            .hasMessageContaining("Material não encontrado com o ID: 999");
        verify(repository).findById(id);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn test -Dtest=GetMaterialUseCaseTest`
Expected: Compilation failure because `GetMaterialUseCase` does not exist yet.

- [ ] **Step 3: Write minimal implementation**

Create `src/main/java/com/postech/oficinamecanica/application/material/GetMaterialUseCase.java`:

```java
package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.material.MaterialNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetMaterialUseCase {
    private final MaterialRepository repository;

    public GetMaterialUseCase(MaterialRepository repository) {
        this.repository = repository;
    }

    public Material execute(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new MaterialNotFoundException(id));
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=GetMaterialUseCaseTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/postech/oficinamecanica/application/material/GetMaterialUseCase.java src/test/java/com/postech/oficinamecanica/application/material/GetMaterialUseCaseTest.java
git commit -m "feat(application): implement GetMaterialUseCase with tests"
```

---

### Task 4: Global Exception Handling mapping for `MaterialNotFoundException`

Map `MaterialNotFoundException` to HTTP 404 (Not Found) with appropriate `ErrorResponse`.

**Files:**
- Modify: `src/main/java/com/postech/oficinamecanica/interfaces/rest/config/GlobalExceptionHandler.java`

**Interfaces:**
- Consumes: `MaterialNotFoundException`
- Produces: `ResponseEntity<ErrorResponse>` (Status 404, code: `"MATERIAL_NOT_FOUND"`)

- [ ] **Step 1: Write mapping implementation**

Add the `@ExceptionHandler` method inside `GlobalExceptionHandler.java`:

```java
    @org.springframework.web.bind.annotation.ExceptionHandler(com.postech.oficinamecanica.domain.material.MaterialNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMaterialNotFound(com.postech.oficinamecanica.domain.material.MaterialNotFoundException e) {
        ErrorResponse error = new ErrorResponse(
            "MATERIAL_NOT_FOUND",
            e.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
```
*(Make sure to import `org.springframework.web.bind.annotation.ExceptionHandler`, `HttpStatus`, and correct exception if needed, or use fully qualified names for precision.)*

- [ ] **Step 2: Compile to ensure no typos**

Run: `mvn compile`
Expected: SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/postech/oficinamecanica/interfaces/rest/config/GlobalExceptionHandler.java
git commit -m "feat(rest): handle MaterialNotFoundException in GlobalExceptionHandler"
```

---

### Task 5: Web REST Endpoint - `MaterialController` and Mappers

Implement the REST endpoint in the controller and write unit tests for both success and error paths.

**Files:**
- Modify: `src/main/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialController.java`
- Modify: `src/test/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialControllerTest.java`

**Interfaces:**
- Consumes: HTTP GET `/api/materials/{id}`
- Produces: `ResponseEntity<MaterialResponse>` (success) or `ErrorResponse` (404 Not Found)

- [ ] **Step 1: Write failing tests in MaterialControllerTest**

First, update `MaterialControllerTest.java` to import `MockitoBean` (or `@MockBean`/`@MockitoBean` depending on existing, which is `@MockitoBean private ListMaterialsUseCase listMaterialsUseCase;`).
Let's inject the new `GetMaterialUseCase` using `@MockitoBean`.

Add these imports:
```java
import com.postech.oficinamecanica.application.material.GetMaterialUseCase;
import com.postech.oficinamecanica.domain.material.MaterialNotFoundException;
```

Inside the class, mock the new usecase:
```java
    @MockitoBean
    private GetMaterialUseCase getMaterialUseCase;
```

Then add the two new test cases:

```java
    @Test
    void shouldReturnMaterialWhenFoundById() throws Exception {
        Long id = 1L;
        Material material = aMaterial(id, "Filtro de Oleo", EntityStatus.ACTIVE);
        when(getMaterialUseCase.execute(id)).thenReturn(material);
        when(mapper.toResponse(material)).thenReturn(aResponse(id, "Filtro de Oleo", "ACTIVE"));

        mockMvc.perform(get("/api/materials/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value("Filtro de Oleo"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnNotFoundWhenMaterialDoesNotExist() throws Exception {
        Long id = 999L;
        when(getMaterialUseCase.execute(id)).thenThrow(new MaterialNotFoundException(id));

        mockMvc.perform(get("/api/materials/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("MATERIAL_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Material não encontrado com o ID: 999"));
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=MaterialControllerTest`
Expected: Compilation failure because the HTTP GET endpoint with ID doesn't exist on `MaterialController`.

- [ ] **Step 3: Implement minimal code to pass the test**

In `MaterialController.java`, inject `GetMaterialUseCase` in the constructor:

```java
    private final ListMaterialsUseCase listMaterialsUseCase;
    private final GetMaterialUseCase getMaterialUseCase;
    private final MaterialRestMapper mapper;

    public MaterialController(ListMaterialsUseCase listMaterialsUseCase, GetMaterialUseCase getMaterialUseCase, MaterialRestMapper mapper) {
        this.listMaterialsUseCase = listMaterialsUseCase;
        this.getMaterialUseCase = getMaterialUseCase;
        this.mapper = mapper;
    }
```

And add the endpoint method mapping `/api/materials/{id}`:

```java
    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar material por ID",
        description = "Retorna os detalhes completos de um único material a partir de seu ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Material encontrado com sucesso",
            content = @Content(schema = @Schema(implementation = MaterialResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Material não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public MaterialResponse getById(
        @Parameter(description = "ID do material a ser buscado", example = "1")
        @org.springframework.web.bind.annotation.PathVariable Long id
    ) {
        return mapper.toResponse(getMaterialUseCase.execute(id));
    }
```

*(Note: Don't forget to import `org.springframework.web.bind.annotation.PathVariable` if not fully qualified).*

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -Dtest=MaterialControllerTest`
Expected: PASS

- [ ] **Step 5: Run all unit tests to check for regressions**

Run: `mvn test -Dtest=*Test,!*IntegrationTest`
Expected: PASS with 0 failures

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialController.java src/test/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialControllerTest.java
git commit -m "feat(rest): implement GET /api/materials/{id} endpoint with tests"
```

---

### Task 6: Add Integration Tests (Optional / Conditional)

Add integration tests to verify database interaction, flyway seed values, and exception mapping in a real container.

**Files:**
- Modify: `src/test/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialControllerIntegrationTest.java`

- [ ] **Step 1: Implement integration tests**

Add the following tests inside `MaterialControllerIntegrationTest.java`:

```java
    @Test
    void shouldReturnMaterialWhenFetchedById() throws Exception {
        mockMvc.perform(get("/api/materials/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Óleo Motor 5W30 Sintético"))
            .andExpect(jsonPath("$.price").value(189.90))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnNotFoundWhenFetchedWithNonExistentId() throws Exception {
        mockMvc.perform(get("/api/materials/{id}", 9999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("MATERIAL_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Material não encontrado com o ID: 9999"));
    }
```

- [ ] **Step 2: Commit**

```bash
git add src/test/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialControllerIntegrationTest.java
git commit -m "test(integration): verify GET /api/materials/{id} endpoint behaviors"
```
