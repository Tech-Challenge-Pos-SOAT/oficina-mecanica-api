# Arquitetura DDD

Responde: "onde essa classe mora e o que ela pode importar?"

## Princípios

- Todo identificador: inglês. Nomes de [`modelo-de-dados.md`](modelo-de-dados.md).
- Estrutura segue **uma fatia end-to-end**: `Customer` cruza todas as camadas.
- `domain` é PURO: sem Spring, JPA, DTO, outra camada. Só regra de negócio.

## Mapa de Pacotes

Exemplo completo: **Customer** (padrão copiável para Vehicle, Material, Service, Employee, ServiceOrder)

```
com.postech.oficinamecanica
├── domain
│   ├── shared
│   │   └── EntityStatus.java
│   └── customer
│       ├── Customer.java
│       ├── Document.java              # VO
│       ├── InvalidDocumentException.java
│       ├── DuplicateDocumentException.java
│       └── CustomerAlreadyInactiveException.java
│
├── application
│   └── customer
│       ├── CreateCustomerUseCase.java
│       ├── CreateCustomerCommand.java
│       └── CustomerRepository.java     # PORTA (interface)
│
├── infrastructure
│   ├── config
│   │   └── OpenApiConfig.java
│   └── persistence
│       └── customer
│           ├── CustomerJpaEntity.java
│           ├── CustomerJpaRepository.java
│           ├── CustomerRepositoryImpl.java
│           └── CustomerPersistenceMapper.java
│
└── interfaces
    └── rest
        └── customer
            ├── CustomerController.java
            ├── CreateCustomerRequest.java
            ├── CustomerResponse.java
            └── CustomerRestMapper.java
```

**Regras:**
- `domain/shared`: APENAS EntityStatus (genuinamente compartilhado).
- `*Repository` em `application/`: interface (porta).
- `*RepositoryImpl` em `infrastructure/persistence/`: implementa porta.
- `*PersistenceMapper` em `infrastructure/persistence/`: domain ↔ JPA (MapStruct).
- `*RestMapper` em `interfaces/rest/`: HTTP ↔ domain (MapStruct).
- Nenhum import de `infrastructure/` em `interfaces/rest/`.

## Direção de Dependência

```
interfaces.rest ---> application ---> domain
                          ^              ^
                          |              |
                    infrastructure ------+
```

**Regras imutáveis:**
1. `domain` importa NADA.
2. `application` importa só `domain`.
3. `infrastructure` implementa interfaces de `application`.
4. `interfaces.rest` importa só `application`.

Teste: apagando `infrastructure`, `domain` + `application` compilam?

---

## Exemplo Completo: Customer

Schema SQL (fonte de verdade):
```sql
CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    document VARCHAR(18) NOT NULL UNIQUE,  -- CPF (11) ou CNPJ (14)
    phone VARCHAR(20),
    email VARCHAR(255) UNIQUE,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE / INACTIVE
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### domain/shared

**EntityStatus.java**
```java
public enum EntityStatus {
    ACTIVE, INACTIVE
}
```

### domain/customer

**Document.java** — Value Object
- Valida formato (CPF 11 dígitos, CNPJ 14 dígitos)
- Imutável
- Sem Spring/JPA

```java
public record Document(String value) {
    public Document {
        if (!isValid(value)) throw new InvalidDocumentException(value);
    }
    
    private static boolean isValid(String value) {
        // validação de CPF/CNPJ
    }
}
```

**Customer.java** — Entidade agregado-raiz
- Regras de negócio
- Sem anotações JPA
- Sem getter/setter sem sentido; métodos expressam comportamento

```java
public class Customer {
    private final Long id;
    private final Document document;
    private String name;
    private String phone;
    private String email;
    private EntityStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Factory: cria customer novo
    public static Customer create(Document document, String name, 
                                   String phone, String email) {
        if (document == null) throw new IllegalArgumentException("document");
        return new Customer(null, document, name, phone, email, 
                           EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }
    
    // Comportamento
    public void deactivate() {
        if (status == EntityStatus.INACTIVE) 
            throw new CustomerAlreadyInactiveException(id);
        this.status = EntityStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }
    
    public void updateContact(String phone, String email) {
        this.phone = phone;
        this.email = email;
        this.updatedAt = Instant.now();
    }
}
```

**Exceptions**
- `InvalidDocumentException` — Document inválido
- `DuplicateDocumentException` — Document já existe
- `CustomerAlreadyInactiveException` — Deactivate de inativo

### application/customer

**CustomerRepository.java** — PORTA (interface, sem JPA)
```java
public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(Long id);
    Optional<Customer> findByDocument(Document document);
    void delete(Long id);
}
```

**CreateCustomerCommand.java** — Entrada do use case
```java
public record CreateCustomerCommand(
    String document,  // String até Document validar
    String name,
    String phone,
    String email
) {}
```

**CreateCustomerUseCase.java** — Orquestra
```java
@Service
public class CreateCustomerUseCase {
    private final CustomerRepository repository;
    
    public Customer execute(CreateCustomerCommand cmd) {
        // 1. Validar Document (lança InvalidDocumentException se ruim)
        Document document = new Document(cmd.document());
        
        // 2. Verificar duplicata (lança DuplicateDocumentException)
        repository.findByDocument(document)
            .ifPresent(c -> { throw new DuplicateDocumentException(document); });
        
        // 3. Criar entidade (regra de negócio: factory)
        Customer customer = Customer.create(document, cmd.name(), 
                                            cmd.phone(), cmd.email());
        
        // 4. Persistir
        return repository.save(customer);
    }
}
```

### infrastructure/persistence/customer

**CustomerJpaEntity.java** — Mapa para BD
```java
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
}
```

**CustomerJpaRepository.java** — Spring Data (interno de infrastructure)
```java
public interface CustomerJpaRepository 
    extends JpaRepository<CustomerJpaEntity, Long> {
    Optional<CustomerJpaEntity> findByDocument(String document);
}
```

**CustomerRepositoryImpl.java** — Implementa porta
```java
@Repository
public class CustomerRepositoryImpl implements CustomerRepository {
    private final CustomerJpaRepository jpaRepository;
    private final CustomerPersistenceMapper mapper;
    
    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity entity = mapper.toPersistence(customer);
        CustomerJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Customer> findByDocument(Document document) {
        return jpaRepository.findByDocument(document.value())
            .map(mapper::toDomain);
    }
    
    // outros
}
```

**CustomerPersistenceMapper.java** — Converte domain ↔ JPA
```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerPersistenceMapper {
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "document", source = "document")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    Customer toDomain(CustomerJpaEntity entity);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "document", source = "document.value")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    CustomerJpaEntity toPersistence(Customer domain);
    
    // VO com construtor validante
    default Document map(String value) {
        return value == null ? null : new Document(value);
    }
    
    default String map(Document document) {
        return document == null ? null : document.value();
    }
}
```

### interfaces/rest/customer

**CreateCustomerRequest.java** — DTO entrada HTTP
```java
public record CreateCustomerRequest(
    @NotBlank String document,
    @NotBlank String name,
    @NotBlank String phone,
    String email
) {}
```

**CustomerResponse.java** — DTO saída HTTP
```java
public record CustomerResponse(
    Long id,
    String name,
    String document,
    String phone,
    String email,
    String status,
    Instant createdAt,
    Instant updatedAt
) {}
```

**CustomerRestMapper.java** — HTTP ↔ domain
```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerRestMapper {
    
    @Mapping(target = "document", source = "document")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "email", source = "email")
    CreateCustomerCommand toCommand(CreateCustomerRequest request);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "document", source = "document.value")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    CustomerResponse toResponse(Customer domain);
}
```

**CustomerController.java** — REST adapter
```java
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CreateCustomerUseCase createCustomerUseCase;
    private final CustomerRestMapper mapper;
    
    @PostMapping
    public ResponseEntity<CustomerResponse> create(
        @Valid @RequestBody CreateCustomerRequest request) {
        
        CreateCustomerCommand cmd = mapper.toCommand(request);
        Customer customer = createCustomerUseCase.execute(cmd);
        
        return ResponseEntity
            .status(CREATED)
            .body(mapper.toResponse(customer));
    }
}
```

---

## Padrão: Copiar pra outra entidade

1. `domain/[entity]`: Entidade + VOs + Exceptions
2. `application/[entity]`: Repository (interface) + Commands + UseCase(s)
3. `infrastructure/persistence/[entity]`: JpaEntity + JpaRepository + Impl + Mapper (MapStruct)
4. `interfaces/rest/[entity]`: Request + Response + RestMapper (MapStruct) + Controller

Respeitar fluxo: `Controller → RestMapper → UseCase → Repository → Domain`

---

## MapStruct

Detalhes, configuração e padrões avançados em [`mapstruct.md`](mapstruct.md).
