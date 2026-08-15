# Endpoint Listagem de Clientes — Plano de Implementação (EXEMPLO PLANO) 

> **Para trabalhadores agentic:** SUB-SKILL OBRIGATÓRIA: Use superpowers:subagent-driven-development (recomendado) ou superpowers:executing-plans para implementar esta tarefa por tarefa. Passos usam sintaxe checkbox (`- [ ]`).

**Objetivo:** Construir um endpoint autenticado via JWT para funcionários listarem todos os clientes, filtrável por status, ordenado por ID ascendente.

**Arquitetura:** Stack DDD completo (domínio → aplicação → infraestrutura → interfaces). `Customer` é a raiz agregada; padrão repository isola persistência. MapStruct gerencia mapeamento entre camadas. Query JPA aplica filtros e ordenação.

**Stack de Tecnologia:** Spring Boot 3.5.16, Spring Data JPA, MapStruct 1.6.3, PostgreSQL, JUnit 5 com TestContainers.

**Especificação:** Resposta lista clientes com: id, name, document, phone, email, status, createdAt, updatedAt. Filtros disponíveis: `?status=ACTIVE` ou `?status=INACTIVE`. Retorna todos os clientes ordenados por ID ascendente. Autenticação obrigatória (token JWT esperado; endpoint assume token válido — sem enforcement explícito de auth nesta tarefa, Security config assumida separada). Erros: status inválido retorna 400 com ErrorResponse (via GlobalExceptionHandler).

## Restrições Globais

- Código 100% inglês; comentários mínimos.
- Sem Lombok, sem geração de construtor.
- Sintaxe Spring Boot 3 (records JPA, anotações de método).
- Schema BD existe: tabela `customer` com colunas id, name, document, phone, email, status, created_at, updated_at.
- Contexto de segurança assumido disponível (validação JWT ocorre antes do controller — esta tarefa assume @RequestHeader ou SecurityContextHolder disponível se necessário; confirmar Security config existe separadamente).

---

## Estrutura de Arquivos

```
com.postech.oficinamecanica
├── domain/customer
│   ├── Customer.java           [entidade, raiz agregada]
│   ├── EntityStatus.java       [enum, compartilhado]
│   └── InvalidDocumentException.java
├── application/customer
│   ├── CustomerRepository.java [interface, porta]
│   └── ListCustomersUseCase.java [query, sem command]
├── infrastructure/persistence/customer
│   ├── CustomerJpaEntity.java  [entidade JPA]
│   ├── CustomerJpaRepository.java [Spring Data]
│   ├── CustomerRepositoryImpl.java [implementa porta]
│   └── CustomerPersistenceMapper.java [MapStruct]
├── interfaces/rest/customer
│   ├── CustomerResponse.java    [DTO]
│   ├── CustomerRestMapper.java  [MapStruct]
│   └── CustomerController.java  [adaptador HTTP]
└── interfaces/rest/config
    ├── ErrorResponse.java       [DTO erro]
    └── GlobalExceptionHandler.java [mapeia exceções]
```

---

### Tarefa 1: Camada Domínio — Entidade Customer & Value Objects

**Arquivos:**
- Criar: `src/main/java/com/postech/oficinamecanica/domain/shared/EntityStatus.java`
- Criar: `src/main/java/com/postech/oficinamecanica/domain/customer/Document.java`
- Criar: `src/main/java/com/postech/oficinamecanica/domain/customer/Customer.java`
- Criar: `src/main/java/com/postech/oficinamecanica/domain/customer/InvalidDocumentException.java`

**Interfaces:**
- Produz: `EntityStatus.ACTIVE`, `EntityStatus.INACTIVE`; `Document` record com getter `value()`; `Customer` com campos id, document (VO Document), name, phone, email, status (EntityStatus), createdAt (Instant), updatedAt (Instant); getters para todos os campos.

- [ ] **Passo 1: Criar enum EntityStatus**

```java
package com.postech.oficinamecanica.domain.shared;

public enum EntityStatus {
    ACTIVE, INACTIVE
}
```

Salvar em `src/main/java/com/postech/oficinamecanica/domain/shared/EntityStatus.java`.

- [ ] **Passo 2: Criar value object Document**

```java
package com.postech.oficinamecanica.domain.customer;

public record Document(String value) {
    public Document {
        if (value == null || value.isBlank()) {
            throw new InvalidDocumentException("Document cannot be blank");
        }
    }
}
```

Salvar em `src/main/java/com/postech/oficinamecanica/domain/customer/Document.java`.

- [ ] **Passo 3: Criar InvalidDocumentException**

```java
package com.postech.oficinamecanica.domain.customer;

public class InvalidDocumentException extends RuntimeException {
    public InvalidDocumentException(String message) {
        super(message);
    }
}
```

Salvar em `src/main/java/com/postech/oficinamecanica/domain/customer/InvalidDocumentException.java`.

- [ ] **Passo 4: Criar entidade Customer**

```java
package com.postech.oficinamecanica.domain.customer;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.time.Instant;

public class Customer {
    private final Long id;
    private final Document document;
    private final String name;
    private final String phone;
    private final String email;
    private final EntityStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Customer(Long id, Document document, String name, String phone, 
                    String email, EntityStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.document = document;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Document getDocument() { return document; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public EntityStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
```

Salvar em `src/main/java/com/postech/oficinamecanica/domain/customer/Customer.java`.

- [ ] **Passo 5: Verificar compilação do domínio**

Executar: `mvn clean compile -pl . -am`

Esperado: BUILD SUCCESS

---

### Tarefa 2: Camada Aplicação — Interface Repository & Use Case

**Arquivos:**
- Criar: `src/main/java/com/postech/oficinamecanica/application/customer/CustomerRepository.java`
- Criar: `src/main/java/com/postech/oficinamecanica/application/customer/ListCustomersUseCase.java`

**Interfaces:**
- Consome: `EntityStatus` (de domain/shared), `Customer` (de domain/customer)
- Produz: Interface `CustomerRepository` com método `List<Customer> findByStatus(EntityStatus status)`; service `ListCustomersUseCase` com método `List<Customer> execute(EntityStatus status)`

- [ ] **Passo 1: Criar interface CustomerRepository (porta)**

```java
package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.util.List;

public interface CustomerRepository {
    List<Customer> findByStatus(EntityStatus status);
}
```

Salvar em `src/main/java/com/postech/oficinamecanica/application/customer/CustomerRepository.java`.

- [ ] **Passo 2: Criar ListCustomersUseCase**

```java
package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListCustomersUseCase {
    private final CustomerRepository repository;

    public ListCustomersUseCase(CustomerRepository repository) {
        this.repository = repository;
    }

    public List<Customer> execute(EntityStatus status) {
        return repository.findByStatus(status);
    }
}
```

Salvar em `src/main/java/com/postech/oficinamecanica/application/customer/ListCustomersUseCase.java`.

- [ ] **Passo 3: Verificar compilação da camada aplicação**

Executar: `mvn clean compile -pl . -am`

Esperado: BUILD SUCCESS

---

### Tarefa 3: Camada Infraestrutura — Entidade JPA, Implementação Repository, Mappers

**Arquivos:**
- Criar: `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/customer/CustomerJpaEntity.java`
- Criar: `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/customer/CustomerJpaRepository.java`
- Criar: `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/customer/CustomerRepositoryImpl.java`
- Criar: `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/customer/CustomerPersistenceMapper.java`

**Interfaces:**
- Consome: `CustomerRepository` (de application/customer), `Customer` (de domain/customer), `EntityStatus` (de domain/shared), `Document` (de domain/customer)
- Produz: `CustomerJpaEntity` com anotações JPA; `CustomerRepositoryImpl` implementando `CustomerRepository` com `findByStatus` retornando resultados ordenados (ORDER BY id ASC); `CustomerPersistenceMapper` interface MapStruct mapeando JPA ↔ domínio

- [ ] **Passo 1: Criar CustomerJpaEntity**

```java
package com.postech.oficinamecanica.infrastructure.persistence.customer;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "customer")
public class CustomerJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String document;

    private String phone;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public CustomerJpaEntity() {}

    public CustomerJpaEntity(Long id, String name, String document, String phone, 
                             String email, EntityStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.document = document;
        this.phone = phone;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDocument() { return document; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public EntityStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
```

Salvar em `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/customer/CustomerJpaEntity.java`.

- [ ] **Passo 2: Criar CustomerJpaRepository (Spring Data)**

```java
package com.postech.oficinamecanica.infrastructure.persistence.customer;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, Long> {
    @Query("SELECT c FROM CustomerJpaEntity c WHERE c.status = :status ORDER BY c.id ASC")
    List<CustomerJpaEntity> findByStatusOrderById(@Param("status") EntityStatus status);
}
```

Salvar em `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/customer/CustomerJpaRepository.java`.

- [ ] **Passo 3: Criar CustomerPersistenceMapper (MapStruct)**

```java
package com.postech.oficinamecanica.infrastructure.persistence.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerPersistenceMapper {
    
    @Mapping(target = "document", source = "document")
    Customer toDomain(CustomerJpaEntity entity);

    @Mapping(target = "document", source = "document.value")
    CustomerJpaEntity toPersistence(Customer domain);

    default Document map(String value) {
        return value == null ? null : new Document(value);
    }

    default String map(Document document) {
        return document == null ? null : document.value();
    }
}
```

Salvar em `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/customer/CustomerPersistenceMapper.java`.

- [ ] **Passo 4: Criar CustomerRepositoryImpl (implementa porta)**

```java
package com.postech.oficinamecanica.infrastructure.persistence.customer;

import com.postech.oficinamecanica.application.customer.CustomerRepository;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CustomerRepositoryImpl implements CustomerRepository {
    private final CustomerJpaRepository jpaRepository;
    private final CustomerPersistenceMapper mapper;

    public CustomerRepositoryImpl(CustomerJpaRepository jpaRepository, CustomerPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Customer> findByStatus(EntityStatus status) {
        return jpaRepository.findByStatusOrderById(status)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
}
```

Salvar em `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/customer/CustomerRepositoryImpl.java`.

- [ ] **Passo 5: Verificar compilação da camada infraestrutura**

Executar: `mvn clean compile -pl . -am`

Esperado: BUILD SUCCESS

---

### Tarefa 4: Camada Interfaces — DTOs REST, Mappers, Controller

**Arquivos:**
- Criar: `src/main/java/com/postech/oficinamecanica/interfaces/rest/customer/CustomerResponse.java`
- Criar: `src/main/java/com/postech/oficinamecanica/interfaces/rest/customer/CustomerRestMapper.java`
- Criar: `src/main/java/com/postech/oficinamecanica/interfaces/rest/customer/CustomerController.java`

**Interfaces:**
- Consome: `Customer` (de domain/customer), `ListCustomersUseCase` (de application/customer), `EntityStatus` (de domain/shared)
- Produz: Record `CustomerResponse` com campos: id, name, document, phone, email, status, createdAt, updatedAt; `CustomerController` com endpoint `GET /api/customers` aceitando parâmetro query opcional `status`; retorna `List<CustomerResponse>`

- [ ] **Passo 1: Criar DTO CustomerResponse com @Schema**

```java
package com.postech.oficinamecanica.interfaces.rest.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record CustomerResponse(
    @Schema(description = "Identificador único", example = "1")
    Long id,
    
    @Schema(description = "Nome completo do cliente", example = "João Silva Santos")
    String name,
    
    @Schema(description = "CPF ou CNPJ formatado", example = "123.456.789-01")
    String document,
    
    @Schema(description = "Telefone com DDD", example = "(31) 99123-4567")
    String phone,
    
    @Schema(description = "Email do cliente", example = "joao.silva@email.com")
    String email,
    
    @Schema(description = "Status do cliente", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    String status,
    
    @Schema(description = "Data e hora de criação (ISO-8601)", example = "2026-08-15T10:30:00Z")
    Instant createdAt,
    
    @Schema(description = "Data e hora da última atualização (ISO-8601)", example = "2026-08-15T10:30:00Z")
    Instant updatedAt
) {}
```

Salvar em `src/main/java/com/postech/oficinamecanica/interfaces/rest/customer/CustomerResponse.java`.

- [ ] **Passo 2: Criar CustomerRestMapper (MapStruct)**

```java
package com.postech.oficinamecanica.interfaces.rest.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerRestMapper {
    
    @Mapping(target = "document", source = "document.value")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    CustomerResponse toResponse(Customer domain);
}
```

Salvar em `src/main/java/com/postech/oficinamecanica/interfaces/rest/customer/CustomerRestMapper.java`.

- [ ] **Passo 3: Criar CustomerController com OpenAPI completo**

```java
package com.postech.oficinamecanica.interfaces.rest.customer;

import com.postech.oficinamecanica.application.customer.ListCustomersUseCase;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.interfaces.rest.config.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Gestão de clientes")
public class CustomerController {
    private final ListCustomersUseCase listCustomersUseCase;
    private final CustomerRestMapper mapper;

    public CustomerController(ListCustomersUseCase listCustomersUseCase, CustomerRestMapper mapper) {
        this.listCustomersUseCase = listCustomersUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(
        summary = "Listar clientes por status",
        description = "Retorna todos os clientes filtrados por status (ACTIVE/INACTIVE), ordenados por ID ascendente. Padrão: ACTIVE se não informado."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clientes recuperada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CustomerResponse.class)))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Status inválido (deve ser ACTIVE ou INACTIVE)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public List<CustomerResponse> list(
        @Parameter(
            description = "Filtro de status: ACTIVE ou INACTIVE (insensível a maiúsculas)",
            example = "ACTIVE",
            required = false
        )
        @RequestParam(required = false) String status
    ) {
        EntityStatus entityStatus = (status == null || status.isBlank()) 
            ? EntityStatus.ACTIVE 
            : EntityStatus.valueOf(status.toUpperCase());
        
        return listCustomersUseCase.execute(entityStatus)
            .stream()
            .map(mapper::toResponse)
            .toList();
    }
}
```

Salvar em `src/main/java/com/postech/oficinamecanica/interfaces/rest/customer/CustomerController.java`.

- [ ] **Passo 4: Verificar compilação da camada interfaces**

Executar: `mvn clean compile -pl . -am`

Esperado: BUILD SUCCESS

---

### Tarefa 5: Tratamento de Erros — GlobalExceptionHandler & ErrorResponse

**Arquivos:**
- Criar: `src/main/java/com/postech/oficinamecanica/interfaces/rest/config/ErrorResponse.java`
- Criar: `src/main/java/com/postech/oficinamecanica/interfaces/rest/config/GlobalExceptionHandler.java`

**Interfaces:**
- Produz: Record `ErrorResponse` com campos code, message, status; `GlobalExceptionHandler` com handler para IllegalArgumentException (status enum inválido) retornando 400

- [ ] **Passo 1: Criar ErrorResponse DTO**

```java
package com.postech.oficinamecanica.interfaces.rest.config;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
    @Schema(description = "Código de erro", example = "INVALID_STATUS")
    String code,
    
    @Schema(description = "Mensagem de erro em português", example = "Status deve ser ACTIVE ou INACTIVE")
    String message,
    
    @Schema(description = "Status HTTP", example = "400")
    int status
) {}
```

Salvar em `src/main/java/com/postech/oficinamecanica/interfaces/rest/config/ErrorResponse.java`.

- [ ] **Passo 2: Criar GlobalExceptionHandler**

```java
package com.postech.oficinamecanica.interfaces.rest.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(IllegalArgumentException e) {
        ErrorResponse error = new ErrorResponse(
            "INVALID_STATUS",
            "Status deve ser ACTIVE ou INACTIVE",
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "Erro interno do servidor",
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

Salvar em `src/main/java/com/postech/oficinamecanica/interfaces/rest/config/GlobalExceptionHandler.java`.

- [ ] **Passo 3: Verificar compilação**

Executar: `mvn clean compile -pl . -am`

Esperado: BUILD SUCCESS

---

### Tarefa 6: Teste de Integração — Camada HTTP

**Arquivos:**
- Criar: `src/test/java/com/postech/oficinamecanica/interfaces/rest/customer/CustomerControllerIT.java`

**Interfaces:**
- Consome: Infraestrutura Spring Test, TestContainers PostgreSQL, dados Customer em migration V2
- Produz: Teste de integração HTTP verificando GET /api/customers retorna 200 com lista correta, filtros funcionam, ordenação está correta

- [ ] **Passo 1: Criar classe teste de integração**

```java
package com.postech.oficinamecanica.interfaces.rest.customer;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CustomerControllerIT {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("test_db")
        .withUsername("test")
        .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListActiveCustomers() throws Exception {
        mockMvc.perform(get("/api/customers")
                .param("status", "ACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(4)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].name", is("João Silva Santos")))
            .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }

    @Test
    void shouldListInactiveCustomers() throws Exception {
        mockMvc.perform(get("/api/customers")
                .param("status", "INACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("Fernanda Lima Rocha")))
            .andExpect(jsonPath("$[0].status", is("INACTIVE")));
    }

    @Test
    void shouldDefaultToActiveStatus() throws Exception {
        mockMvc.perform(get("/api/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void shouldOrderByIdAscending() throws Exception {
        mockMvc.perform(get("/api/customers")
                .param("status", "ACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[2].id", is(3)))
            .andExpect(jsonPath("$[3].id", is(4)));
    }
}
```

Salvar em `src/test/java/com/postech/oficinamecanica/interfaces/rest/customer/CustomerControllerIT.java`.

- [ ] **Passo 2: Executar teste de integração**

Executar: `mvn clean test -Dtest=CustomerControllerIT`

Esperado: Testes passam. 4 clientes ACTIVE retornados, 1 INACTIVE quando filtrado, ordenação por ID ascendente confirmada.

- [ ] **Passo 3: Executar todos os testes**

Executar: `mvn clean test`

Esperado: Todos os testes passam, sem falhas.

---

### Tarefa 6: Teste Manual & Verificação

**Arquivos:** Nenhum criado; testar via curl ou Postman contra app em execução.

- [ ] **Passo 1: Iniciar aplicação**

Executar: `mvn spring-boot:run`

Esperado: App inicia na porta 8080 (padrão), logs sem erros.

- [ ] **Passo 2: Testar lista padrão (ACTIVE)**

Executar: `curl -s http://localhost:8080/api/customers | jq .`

Esperado: Retorna 4 clientes ACTIVE (João, Maria, Carlos, Auto Peças), ordenados por id 1, 2, 3, 4.

- [ ] **Passo 3: Testar filtro INACTIVE**

Executar: `curl -s http://localhost:8080/api/customers?status=INACTIVE | jq .`

Esperado: Retorna 1 cliente (Fernanda Lima Rocha, id 5), status INACTIVE.

- [ ] **Passo 4: Testar status inválido (caso limite)**

Executar: `curl -s http://localhost:8080/api/customers?status=UNKNOWN | jq .`

Esperado: 400 Bad Request (ou IllegalArgumentException se não validado). Nota: Adicionar validação no futuro se necessário.

- [ ] **Passo 5: Verificar formato de resposta**

Exemplo de resposta:
```json
[
  {
    "id": 1,
    "name": "João Silva Santos",
    "document": "123.456.789-01",
    "phone": "(31) 99123-4567",
    "email": "joao.silva@email.com",
    "status": "ACTIVE",
    "createdAt": "2026-08-15T10:00:00Z",
    "updatedAt": "2026-08-15T10:00:00Z"
  }
]
```

Todos os campos presentes, datas formatadas como ISO-8601 Instant.

- [ ] **Passo 6: Parar aplicação**

Executar: Ctrl+C

---

### Tarefa 7: Documentação OpenAPI

**Arquivos:** Já existe em `infrastructure/config/OpenApiConfig.java` — sem alterações necessárias se for genérico. Se customizado, verificar endpoint aparece em Swagger UI.

- [ ] **Passo 1: Verificar Swagger UI inclui endpoint**

Iniciar app, navegar para `http://localhost:8080/swagger-ui.html`

Esperado: `GET /api/customers` aparece sob tag "Customers" com parâmetros documentados (status: opcional, padrão ACTIVE).

- [ ] **Passo 2: Fazer commit de todas as mudanças**

```bash
git add -A
git commit -m "feat: add list customers endpoint

- Domínio: entidade Customer, value object Document, enum EntityStatus
- Aplicação: interface CustomerRepository, ListCustomersUseCase
- Infraestrutura: CustomerJpaEntity, CustomerJpaRepository, mappers
- Interfaces: CustomerResponse, CustomerRestMapper, CustomerController
- Testes de integração: CustomerControllerIT com filtragem de status, ordenação
- Endpoint: GET /api/customers?status=ACTIVE|INACTIVE (padrão ACTIVE)
- Resposta: Lista de clientes ordenados por ID ascendente"
```

Esperado: Commit sucede, nenhum hook pré-commit falha.

---

## Checklist Antes da Entrega

- ✅ Domínio: `Customer`, `Document` (VO), `EntityStatus` compilam
- ✅ Aplicação: `CustomerRepository` (interface), `ListCustomersUseCase` (@Service)
- ✅ Infraestrutura: entidade JPA, repo Spring Data, impl, mappers MapStruct
- ✅ Interfaces: `CustomerResponse` (record), `CustomerRestMapper`, `CustomerController` (@RestController)
- ✅ Testes de integração: HTTP 200, filtragem por status, ordenação por ID, padrão ACTIVE
- ✅ Verificação manual: curl funciona, docs Swagger visíveis
- ✅ Todo código compila, todos os testes passam
- ✅ Commit criado

---

Plano completo e salvo. Duas opções de execução:

**1. Dirigida por Subagent (recomendado)** — Subagent fresco por tarefa, review entre tarefas, iteração rápida

**2. Execução Inline** — Executar tarefas nesta sessão usando executing-plans, execução em lote com checkpoints

Qual abordagem?
