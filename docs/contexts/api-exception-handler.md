# API Exception Handler

Exceções de domínio em `domain/`. Mapeamento para HTTP em `interfaces/rest/config/`.

**Regra:** 
- Domain não conhece HTTP (puro, reutilizável)
- Application propaga exceção (sem try-catch)
- GlobalExceptionHandler mapeia domínio → HTTP status + response

**Arquitetura:**
```
domain/customer/InvalidDocumentException.java
    ↓ lança
application/customer/CreateCustomerUseCase.java
    ↓ propaga
interfaces/rest/config/GlobalExceptionHandler.java
    ↓ mapeia
HTTP response (status + ErrorResponse)
```

## Implementação

### 1. Exceção de Domínio
```java
// domain/customer/InvalidDocumentException.java
public class InvalidDocumentException extends RuntimeException {
    public InvalidDocumentException(String document) {
        super("Invalid document: " + document);
    }
}
```

### 2. GlobalExceptionHandler
```java
// interfaces/rest/config/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidDocumentException.class)
    public ResponseEntity<ErrorResponse> handle(InvalidDocumentException e) {
        return respond("INVALID_DOCUMENT", e.getMessage(), BAD_REQUEST);
    }
    
    @ExceptionHandler(DuplicateDocumentException.class)
    public ResponseEntity<ErrorResponse> handle(DuplicateDocumentException e) {
        return respond("DUPLICATE_DOCUMENT", e.getMessage(), CONFLICT);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception e) {
        return respond("INTERNAL_ERROR", "Unexpected error", INTERNAL_SERVER_ERROR);
    }
    
    private ResponseEntity<ErrorResponse> respond(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, status.value()));
    }
}
```

### 3. DTO de Resposta
```java
// interfaces/rest/config/ErrorResponse.java
public record ErrorResponse(String code, String message, int status) {}
```

### 4. Use Case: Sem Try-Catch
```java
// application/customer/CreateCustomerUseCase.java
public Customer execute(CreateCustomerCommand cmd) {
    Document document = new Document(cmd.document());  // lança se inválido
    repository.findByDocument(document)
        .ifPresent(c -> { throw new DuplicateDocumentException(document); });
    return repository.save(Customer.create(document, cmd.name(), cmd.phone(), cmd.email()));
}
```

### 5. Controller: Propaga Exceção
```java
@PostMapping
public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
    Customer customer = useCase.execute(mapper.toCommand(request));
    return ResponseEntity.status(CREATED).body(mapper.toResponse(customer));
}
```
Use case lança → GlobalExceptionHandler captura → HTTP response.

## Por Quê

- Domain puro (reutilizável: CLI, evento, microserviço)
- Application simples (orquestra, não trata)
- Centralizado (um lugar, consistência)
- Testável (use case lança, handler mapeia)
