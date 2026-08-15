# Testes

<skill>
Meta: **80% cobertura** (JaCoCo) em dominios criticos.
Relatorio: `target/site/jacoco/index.html` apos `mvn test`.
Nome: ingles, como todo codigo.
</skill>

<boot4-imports>
O Boot 4 moveu pacotes de teste. **Consulte Context7 antes de copiar de memoria 3.x.**

| Anotacao | Boot 4 correto |
|---|---|
| `@DataJpaTest` | `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest` |
| `@AutoConfigureTestDatabase` | `org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase` |

Starters granulares: `spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`, etc. (nao monolitico `spring-boot-starter-test`).
Falta um starter? Pergunte antes de adicionar.
</boot4-imports>

<tipologia>
| O que | Como | Spring | Banco |
|---|---|---|---|
| Value Object | JUnit puro | nao | nao |
| Agregado invariante | JUnit puro | nao | nao |
| Use case | JUnit + Mockito | nao | nao |
| Repository/JPA | `@DataJpaTest` + Testcontainers | sim | Postgres real |
| Controller/HTTP | `@WebMvcTest` | sim | nao |

**Maior parte nao toca BD**: regra, VO, use case = JUnit puro, sem Spring/container.
Banco so em testes JPA. Simplicidade ali, nao em trocar BD de teste.
**Banco de teste**: Postgres real via Testcontainers (nao H2; diverge em tipo/funcao/erro).
**Sem `@SpringBootTest`**: se regra e pura, teste puro. Subir contexto custoso e nao acha mais.
</tipologia>

<nomenclatura>
`should<ExpectedBehavior>When<Condition>` em vocabulario do dominio.
Exemplos: `shouldRejectDocumentWithInvalidCheckDigit`, `shouldStartDiagnosisWhenOrderIsReceived`
</nomenclatura>

<exemplo-ruim>
```java
@Test void test1() { assertTrue(true); }  // assert tautologico
@Test void testeDeStatus() { ... }  // em portugues
static ServiceOrder shared;  // estado compartilhado
@Test void test3() { mock(ServiceOrder.class); }  // mock de classe sob teste
```
❌ Nome vago; testes tautologicos; mock de classe sob teste; estado compartilhado.
</exemplo-ruim>

<exemplo-bom-regra>
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
</exemplo-bom-regra>

<exemplo-bom-vo>
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
</exemplo-bom-vo>

<exemplo-bom-usecase>
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
</exemplo-bom-usecase>

<exemplo-bom-persistencia>
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
        var entity = new CustomerJpaEntity();
        entity.setName(name);
        entity.setDocument(document);
        entity.setStatus(EntityStatus.ACTIVE);
        return entity;
    }
}
```
Container `static` reutilizado; `Replace.NONE` nao substitui DataSource; Flyway roda antes dos testes.
</exemplo-bom-persistencia>

<comandos>
```bash
mvn test                              # rodar suite
open target/site/jacoco/index.html    # cobertura
```
</comandos>
