# Testes — Endpoint Listagem de Clientes

> **Para agentes:** Use superpowers:executing-plans para executar tarefa por tarefa com checkpoints.

**Objetivo:** Implementar testes unitários (application layer) e testes de integração (REST layer) para o endpoint `GET /api/customers` que lista clientes filtrados por status.

**Arquitetura:** Camada Application (ListCustomersUseCase) testada com Mockito; camada REST (CustomerController) testada com @WebMvcTest + banco real.

**Stack:** Spring Boot 3.5.16, JUnit 5, Mockito, AssertJ, Testcontainers/PostgreSQL.

**Spec:** Cobertura 80%+ no JaCoCo; nomenclatura `shouldXWhenY`; mocks apenas em portas (repository), nunca em domain.

---

## Global Constraints

- **Spring Boot version:** 3.5.16
- **JUnit:** `org.junit.jupiter:junit-jupiter-api` (já no parent)
- **Mockito:** `org.mockito:mockito-core` (já no parent)
- **AssertJ:** `org.assertj:assertj-core` (já no parent)
- **Test naming:** inglês, padrão `should<Behavior>When<Condition>`
- **Sem @SpringBootTest:** se regra é pura, teste puro
- **Banco de teste:** Postgres real via Testcontainers (não H2)

---

## Estrutura de Arquivos

**Novos:**
- `src/test/java/com/postech/oficinamecanica/application/customer/ListCustomersUseCaseTest.java` — testes unitários (Mockito)
- `src/test/java/com/postech/oficinamecanica/interfaces/rest/customer/CustomerControllerTest.java` — testes integração (REST)

**Recursos de teste:**
- `src/test/resources/` — já existe; arquivos globais aqui

---

## Tarefa 1: Testes Unitários — ListCustomersUseCase

**Arquivos:**
- Create: `src/test/java/com/postech/oficinamecanica/application/customer/ListCustomersUseCaseTest.java`

**Interfaces:**
- Consumes: `CustomerRepository.findByStatus(EntityStatus status): List<Customer>`
- Consumes: `EntityStatus.ACTIVE`, `EntityStatus.INACTIVE`
- Consumes: `ListCustomersUseCase.execute(String statusParam): List<Customer>`

- [ ] **Step 1: Write failing test — default status (null parameter)**

```java
package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCustomersUseCaseTest {
    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private ListCustomersUseCase useCase;

    @Test
    void shouldReturnActiveCustomersWhenStatusParamIsNull() {
        Customer customer = new Customer(
            1L,
            new Document("52998224725"),
            "Maria Souza",
            "11987654321",
            "maria@email.com",
            EntityStatus.ACTIVE,
            Instant.now(),
            Instant.now()
        );
        
        when(repository.findByStatus(EntityStatus.ACTIVE))
            .thenReturn(List.of(customer));

        List<Customer> result = useCase.execute(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Maria Souza");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=ListCustomersUseCaseTest::shouldReturnActiveCustomersWhenStatusParamIsNull
```

Expected: FAIL com "class not found" ou "cannot find symbol"

- [ ] **Step 3: Write minimal implementation (test confirms code exists)**

Valide que `ListCustomersUseCase.execute(null)` já existe e é puro. Nada a implementar; teste apenas valida comportamento.

- [ ] **Step 4: Run test to verify it passes**

```bash
mvn test -Dtest=ListCustomersUseCaseTest::shouldReturnActiveCustomersWhenStatusParamIsNull
```

Expected: PASS

- [ ] **Step 5: Add test — explicit ACTIVE status (case-insensitive)**

```java
@Test
void shouldReturnActiveCustomersWhenStatusIsExplicitActive() {
    Customer customer = new Customer(
        1L,
        new Document("52998224725"),
        "Maria Souza",
        "11987654321",
        "maria@email.com",
        EntityStatus.ACTIVE,
        Instant.now(),
        Instant.now()
    );
    
    when(repository.findByStatus(EntityStatus.ACTIVE))
        .thenReturn(List.of(customer));

    List<Customer> result = useCase.execute("active");

    assertThat(result).hasSize(1);
}
```

- [ ] **Step 6: Run test**

```bash
mvn test -Dtest=ListCustomersUseCaseTest::shouldReturnActiveCustomersWhenStatusIsExplicitActive
```

Expected: PASS

- [ ] **Step 7: Add test — INACTIVE status**

```java
@Test
void shouldReturnInactiveCustomersWhenStatusIsInactive() {
    Customer customer = new Customer(
        2L,
        new Document("98765432109"),
        "João Silva",
        "11912345678",
        "joao@email.com",
        EntityStatus.INACTIVE,
        Instant.now(),
        Instant.now()
    );
    
    when(repository.findByStatus(EntityStatus.INACTIVE))
        .thenReturn(List.of(customer));

    List<Customer> result = useCase.execute("INACTIVE");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getStatus()).isEqualTo(EntityStatus.INACTIVE);
}
```

- [ ] **Step 8: Run test**

```bash
mvn test -Dtest=ListCustomersUseCaseTest::shouldReturnInactiveCustomersWhenStatusIsInactive
```

Expected: PASS

- [ ] **Step 9: Add test — empty result**

```java
@Test
void shouldReturnEmptyListWhenNoCustomersMatchStatus() {
    when(repository.findByStatus(EntityStatus.INACTIVE))
        .thenReturn(List.of());

    List<Customer> result = useCase.execute("INACTIVE");

    assertThat(result).isEmpty();
}
```

- [ ] **Step 10: Run test**

```bash
mvn test -Dtest=ListCustomersUseCaseTest::shouldReturnEmptyListWhenNoCustomersMatchStatus
```

Expected: PASS

- [ ] **Step 11: Run all ListCustomersUseCaseTest tests**

```bash
mvn test -Dtest=ListCustomersUseCaseTest
```

Expected: 4/4 PASS

- [ ] **Step 12: Commit**

```bash
git add src/test/java/com/postech/oficinamecanica/application/customer/ListCustomersUseCaseTest.java
git commit -m "test: add unit tests for ListCustomersUseCase"
```

---

## Tarefa 2: Testes de Integração — CustomerController

**Arquivos:**
- Create: `src/test/java/com/postech/oficinamecanica/interfaces/rest/customer/CustomerControllerTest.java`

**Interfaces:**
- Consumes: `GET /api/customers?status=ACTIVE` → `List<CustomerResponse>`
- Consumes: `CustomerRestMapper.toResponse(Customer): CustomerResponse`
- Consumes: `ListCustomersUseCase.execute(String): List<Customer>`

- [ ] **Step 1: Write failing test — list all customers default (ACTIVE)**

```java
package com.postech.oficinamecanica.interfaces.rest.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.oficinamecanica.application.customer.ListCustomersUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListCustomersUseCase listCustomersUseCase;

    @MockitoBean
    private CustomerRestMapper mapper;

    @Test
    void shouldReturnActiveCustomersWhenNoStatusFilterProvided() throws Exception {
        Customer customer = new Customer(
            1L,
            new Document("52998224725"),
            "Maria Souza",
            "11987654321",
            "maria@email.com",
            EntityStatus.ACTIVE,
            Instant.now(),
            Instant.now()
        );

        CustomerResponse response = new CustomerResponse(
            1L,
            "52998224725",
            "Maria Souza",
            "11987654321",
            "maria@email.com",
            "ACTIVE",
            Instant.now(),
            Instant.now()
        );

        when(listCustomersUseCase.execute(null))
            .thenReturn(List.of(customer));
        when(mapper.toResponse(customer))
            .thenReturn(response);

        mockMvc.perform(get("/api/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Maria Souza"))
            .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=CustomerControllerTest::shouldReturnActiveCustomersWhenNoStatusFilterProvided
```

Expected: FAIL — test class not found or MockMvc setup issue

- [ ] **Step 3: Create CustomerResponse if missing**

Check if `CustomerResponse` exists in `src/main/java/com/postech/oficinamecanica/interfaces/rest/customer/CustomerResponse.java`. If not, create:

```java
package com.postech.oficinamecanica.interfaces.rest.customer;

import java.time.Instant;

public record CustomerResponse(
    Long id,
    String document,
    String name,
    String phone,
    String email,
    String status,
    Instant createdAt,
    Instant updatedAt
) {}
```

(If already exists, skip this step.)

- [ ] **Step 4: Run test again**

```bash
mvn test -Dtest=CustomerControllerTest::shouldReturnActiveCustomersWhenNoStatusFilterProvided
```

Expected: PASS

- [ ] **Step 5: Add test — filter by ACTIVE status (case-insensitive)**

```java
@Test
void shouldReturnActiveCustomersWhenStatusFilterIsActive() throws Exception {
    Customer customer = new Customer(
        1L,
        new Document("52998224725"),
        "Maria Souza",
        "11987654321",
        "maria@email.com",
        EntityStatus.ACTIVE,
        Instant.now(),
        Instant.now()
    );

    CustomerResponse response = new CustomerResponse(
        1L,
        "52998224725",
        "Maria Souza",
        "11987654321",
        "maria@email.com",
        "ACTIVE",
        Instant.now(),
        Instant.now()
    );

    when(listCustomersUseCase.execute("active"))
        .thenReturn(List.of(customer));
    when(mapper.toResponse(customer))
        .thenReturn(response);

    mockMvc.perform(get("/api/customers?status=active"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Maria Souza"));
}
```

- [ ] **Step 6: Run test**

```bash
mvn test -Dtest=CustomerControllerTest::shouldReturnActiveCustomersWhenStatusFilterIsActive
```

Expected: PASS

- [ ] **Step 7: Add test — filter by INACTIVE status**

```java
@Test
void shouldReturnInactiveCustomersWhenStatusFilterIsInactive() throws Exception {
    Customer customer = new Customer(
        2L,
        new Document("98765432109"),
        "João Silva",
        "11912345678",
        "joao@email.com",
        EntityStatus.INACTIVE,
        Instant.now(),
        Instant.now()
    );

    CustomerResponse response = new CustomerResponse(
        2L,
        "98765432109",
        "João Silva",
        "11912345678",
        "joao@email.com",
        "INACTIVE",
        Instant.now(),
        Instant.now()
    );

    when(listCustomersUseCase.execute("INACTIVE"))
        .thenReturn(List.of(customer));
    when(mapper.toResponse(customer))
        .thenReturn(response);

    mockMvc.perform(get("/api/customers?status=INACTIVE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].status").value("INACTIVE"));
}
```

- [ ] **Step 8: Run test**

```bash
mvn test -Dtest=CustomerControllerTest::shouldReturnInactiveCustomersWhenStatusFilterIsInactive
```

Expected: PASS

- [ ] **Step 9: Add test — empty result**

```java
@Test
void shouldReturnEmptyListWhenNoCustomersMatchFilter() throws Exception {
    when(listCustomersUseCase.execute(null))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/customers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
}
```

- [ ] **Step 10: Run test**

```bash
mvn test -Dtest=CustomerControllerTest::shouldReturnEmptyListWhenNoCustomersMatchFilter
```

Expected: PASS

- [ ] **Step 11: Add test — multiple customers in response**

```java
@Test
void shouldReturnMultipleCustomersWhenFound() throws Exception {
    Customer customer1 = new Customer(
        1L,
        new Document("52998224725"),
        "Maria Souza",
        "11987654321",
        "maria@email.com",
        EntityStatus.ACTIVE,
        Instant.now(),
        Instant.now()
    );

    Customer customer2 = new Customer(
        2L,
        new Document("12345678901"),
        "Pedro Santos",
        "11987654322",
        "pedro@email.com",
        EntityStatus.ACTIVE,
        Instant.now(),
        Instant.now()
    );

    CustomerResponse response1 = new CustomerResponse(
        1L, "52998224725", "Maria Souza", "11987654321", "maria@email.com", "ACTIVE", Instant.now(), Instant.now()
    );

    CustomerResponse response2 = new CustomerResponse(
        2L, "12345678901", "Pedro Santos", "11987654322", "pedro@email.com", "ACTIVE", Instant.now(), Instant.now()
    );

    when(listCustomersUseCase.execute(null))
        .thenReturn(List.of(customer1, customer2));
    when(mapper.toResponse(customer1))
        .thenReturn(response1);
    when(mapper.toResponse(customer2))
        .thenReturn(response2);

    mockMvc.perform(get("/api/customers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Maria Souza"))
        .andExpect(jsonPath("$[1].name").value("Pedro Santos"));
}
```

- [ ] **Step 12: Run test**

```bash
mvn test -Dtest=CustomerControllerTest::shouldReturnMultipleCustomersWhenFound
```

Expected: PASS

- [ ] **Step 13: Run all CustomerControllerTest tests**

```bash
mvn test -Dtest=CustomerControllerTest
```

Expected: 5/5 PASS

- [ ] **Step 14: Run complete test suite (unit + integration)**

```bash
mvn test
```

Expected: All tests PASS; JaCoCo coverage report generated

- [ ] **Step 15: Verify JaCoCo coverage**

```bash
open target/site/jacoco/index.html
```

Validate: `ListCustomersUseCase` and `CustomerController` ≥ 80% coverage.

- [ ] **Step 16: Commit**

```bash
git add src/test/java/com/postech/oficinamecanica/interfaces/rest/customer/CustomerControllerTest.java
git commit -m "test: add integration tests for CustomerController"
```

---

## Checklist Pré-Revisão

- [ ] Todas as classes de teste nomeadas com `*Test.java`
- [ ] Nomenclatura segue padrão `shouldXWhenY` em inglês
- [ ] Mocks usados apenas em portas (repository), nunca em domain
- [ ] `@WebMvcTest` para testes REST (não `@SpringBootTest`)
- [ ] `@ExtendWith(MockitoExtension.class)` para testes unitários
- [ ] Testes rodam com `mvn test` sem erros
- [ ] JaCoCo report mostra ≥ 80% nas classes principais
- [ ] Sem estado compartilhado entre testes (`static` fields)
- [ ] Sem asserts tautológicos (ex: `assertTrue(true)`)
