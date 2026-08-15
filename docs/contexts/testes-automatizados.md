# Testes Automatizados

## Objetivo

Meta: **80% cobertura** (JaCoCo) em domínios críticos.
Relatório: `target/site/jacoco/index.html` após `mvn test`.
Nome: inglês, como todo código.

## Spring Boot 4 — Imports

O Boot 4 moveu pacotes de teste. **Consulte Context7 antes de copiar de memória 3.x.**

| Anotação | Boot 4 correto |
|---|---|
| `@DataJpaTest` | `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest` |
| `@AutoConfigureTestDatabase` | `org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase` |

Starters granulares: `spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`, etc. (não monolítico `spring-boot-starter-test`).
Falta um starter? Pergunte antes de adicionar.

## Tipologia

| O que | Como | Spring | Banco |
|---|---|---|---|
| Value Object | JUnit puro | não | não |
| Agregado invariante | JUnit puro | não | não |
| Use case | JUnit + Mockito | não | não |
| Repository/JPA | `@DataJpaTest` + Testcontainers | sim | Postgres real |
| Controller/HTTP | `@WebMvcTest` | sim | não |

**Maior parte não toca BD**: regra, VO, use case = JUnit puro, sem Spring/container.
Banco só em testes JPA. Simplicidade ali, não em trocar BD de teste.
**Banco de teste**: Postgres real via Testcontainers (não H2; diverge em tipo/função/erro).
**Sem `@SpringBootTest`**: se regra é pura, teste puro. Subir contexto custoso e não acha mais.

## Nomenclatura

`should<ExpectedBehavior>When<Condition>` em vocabulário do domínio.
Exemplos:
- `shouldRejectDocumentWithInvalidCheckDigit`
- `shouldStartDiagnosisWhenOrderIsReceived`

## Exemplo Ruim
```java
@Test void test1() { assertTrue(true); }  // assert tautológico
@Test void testeDeStatus() { ... }  // em português
static ServiceOrder shared;  // estado compartilhado
@Test void test3() { mock(ServiceOrder.class); }  // mock de classe sob teste
```

❌ Nome vago; testes tautológicos; mock de classe sob teste; estado compartilhado.

## Exemplo Bom — Regra de Negócio
```java
class CustomerTest {
    @Test
    void shouldCreateCustomerWithValidDocument() {
        Customer customer = Customer.create(
            new Document("529.982.247-25"), 
            "Maria Souza", 
            "11987654321", 
            "maria@email.com"
        );
        assertThat(customer.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(customer.getName()).isEqualTo("Maria Souza");
    }
    @Test
    void shouldNotAllowDeactivatingTwice() {
        Customer customer = Customer.create(
            new Document("529.982.247-25"), 
            "Maria Souza", 
            "11987654321", 
            "maria@email.com"
        );
        customer.deactivate();
        assertThatThrownBy(customer::deactivate)
                .isInstanceOf(CustomerAlreadyInactiveException.class);
    }
}
```

## Exemplo Bom — Value Object
```java
class DocumentTest {
    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25", "52998224725"})
    void shouldAcceptValidCpfWithOrWithoutFormatting(String value) {
        assertThat(new Document(value).unformatted()).isEqualTo("52998224725");
    }
    @ParameterizedTest
    @ValueSource(strings = {"111.111.111-11", "529.982.247-26", "123", ""})
    void shouldRejectInvalidDocument(String value) {
        assertThatThrownBy(() -> new Document(value))
                .isInstanceOf(InvalidDocumentException.class);
    }
}
```

## Exemplo Bom — Use Case
```java
@ExtendWith(MockitoExtension.class)
class CreateCustomerUseCaseTest {
    @Mock private CustomerRepository repository;
    @InjectMocks private CreateCustomerUseCase useCase;
    
    @Test
    void shouldFailWhenDocumentAlreadyExists() {
        CreateCustomerCommand cmd = new CreateCustomerCommand(
            "529.982.247-25", "Maria Souza", "11987654321", "maria@email.com"
        );
        Customer existing = Customer.create(
            new Document("529.982.247-25"), "Outro Nome", "11912345678", null
        );
        when(repository.findByDocument(any())).thenReturn(Optional.of(existing));
        
        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(DuplicateDocumentException.class);
        verify(repository, never()).save(any());
    }
}
```

Mock só em portas, nunca em domain (domínio é barato de verdade).

## Exemplo Bom — Persistência
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class CustomerRepositoryImplTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    
    @Autowired private CustomerJpaRepository repository;
    @Test
    void shouldPersistAndFindCustomerByDocument() {
        repository.save(aCustomerEntity("Maria Souza", "52998224725"));
        assertThat(repository.findByDocument("52998224725")).isPresent();
    }
    private static CustomerJpaEntity aCustomerEntity(String name, String document) {
        return new CustomerJpaEntity(
            null,  // id gerado
            name,
            document,
            null,  // phone
            null,  // email
            EntityStatus.ACTIVE,
            Instant.now(),
            Instant.now()
        );
    }
}
```

Container `static` reutilizado; `Replace.NONE` não substitui DataSource; Flyway roda antes dos testes.

## Comandos

```bash
mvn test                              # rodar suite
open target/site/jacoco/index.html    # cobertura
```
