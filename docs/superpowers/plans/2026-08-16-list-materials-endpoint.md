# Endpoint Listagem de Materiais — Plano de Implementação

> **Para trabalhadores agentic:** SUB-SKILL OBRIGATÓRIA: Use superpowers:subagent-driven-development (recomendado) ou superpowers:executing-plans para implementar este plano tarefa por tarefa. Passos usam sintaxe checkbox (`- [ ]`).

**Objetivo:** Expor `GET /api/materials` filtrando por status (ACTIVE/INACTIVE), ordenado por ID ascendente, com testes de integração em Postgres real.

**Arquitetura:** Fatia vertical DDD completa (`domain` → `application` → `infrastructure` → `interfaces.rest`), copiando exatamente a fatia `Customer` que já existe no repositório. `Material` é agregado próprio, sem Value Object (não há regra a encapsular nesta leitura). MapStruct converte JPA → domain → DTO. A query JPA aplica filtro e ordenação.

**Stack:** Spring Boot 3.5.16, Java 21, Spring Data JPA, MapStruct 1.6.3, Flyway, PostgreSQL 16, JUnit 5 + Mockito + Testcontainers, springdoc-openapi 2.8.5.

**Especificação:** Este plano é a especificação (originou-se do pedido: endpoint GET `api/materials`, filtro por status, campos `id, name, description, price, stock_quantity, stock_minimum, status, createdAt, updatedAt`, ordenação por ID ascendente, com testes de integração).

**Autenticação está fora deste plano por decisão explícita do usuário (16/08/2026).** O pedido original incluía "autenticado com JWT", mas o projeto não tem nenhum método de autenticação hoje — nem starter de security no `pom.xml`, nem emissão de token (a tabela `employee` existe no schema sem entidade, repositório ou caso de uso). O endpoint nasce **público**; proteger `/api/**` é plano próprio, aplicado depois a todos os controllers de uma vez. Ver "Fora de Escopo".

Fontes de verdade consultadas: [`docs/contexts/modelo-de-dados.md`](../../contexts/modelo-de-dados.md) (schema/nomes), [`docs/contexts/arquitetura-ddd.md`](../../contexts/arquitetura-ddd.md) (camadas), [`docs/contexts/testes-automatizados.md`](../../contexts/testes-automatizados.md) (tipologia de teste), [`docs/contexts/openapi-annotations.md`](../../contexts/openapi-annotations.md) (anotações), e o código de `Customer` já mergeado.

---

## Restrições Globais

- **Código 100% inglês**; documentação e conversa em português. Comentários só quando o "por quê" não é óbvio.
- **Spring Boot 3.5.16** (o `pom.xml` é a fonte de verdade). `docs/contexts/testes-automatizados.md` e `openapi-annotations.md` afirmam "Spring Boot 4" e citam pacotes de teste do Boot 4 — **isso está errado para este repositório**. Use os imports do Boot 3.5, os mesmos que `CustomerControllerTest` já usa (`org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest`).
- **Sem Lombok.** Construtor e getters escritos à mão (padrão de `Customer.java`).
- **DTOs sempre `record`**; entidade de domínio é `class` com campos `final` + getters.
- **Tipos vindos do schema:** `NUMERIC(10,2)` → `BigDecimal` (nunca `double`); `TIMESTAMP` → `Instant`; `INT` → `Integer`; `VARCHAR` de domínio fechado → `EntityStatus`.
- **Nomes de campo saem do schema em camelCase:** `stock_quantity` → `stockQuantity`, `stock_minimum` → `stockMinimum`. **O JSON de resposta usa camelCase** (`stockQuantity`, `stockMinimum`), coerente com `CustomerResponse` que já expõe `createdAt`/`updatedAt`.
- **Flyway cria o schema** (`ddl-auto=validate`). A tabela `material` **já existe** em `V1__initial_schema.sql` — **não crie nem edite migration existente**. Dados novos = migration nova (`V3__...`).
- **MapStruct:** `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)`.
- **OpenAPI:** anotações apenas em `interfaces.rest`; nunca em `domain`/`application`.
- **Nome de teste:** `should<ExpectedBehavior>When<Condition>`, em inglês.
- **Banco de teste é Postgres real via Testcontainers** — nunca H2. **Docker precisa estar rodando** para os testes de integração.
- **Nome de classe de teste de integração termina em `Test`, não em `IT`.** O `pom.xml` não configura surefire nem failsafe, e o CI roda `mvn -B test`: os includes padrão do surefire são `**/Test*.java`, `**/*Test.java`, `**/*Tests.java`, `**/*TestCase.java`. Uma classe `MaterialControllerIT` **não seria executada nem local nem no CI**. Por isso os nomes usados aqui são `MaterialControllerIntegrationTest` e `MaterialJpaRepositoryIntegrationTest`.
- **Commits:** `CLAUDE.md` proíbe commit sem pedido explícito do usuário. Os passos de commit abaixo só devem ser executados quando o usuário autorizar. Padrão de mensagem: conventional commits (`feat:`, `fix:`, `test:`).
- **Branch atual:** `feature/crud-material`.
- **Nenhuma dependência nova.** Todo o plano usa o que já está no `pom.xml`. Se algum passo parecer exigir biblioteca nova, pare e pergunte (`CLAUDE.md` proíbe adicionar dependência sem aprovação).

---

## Estrutura de Arquivos

```
src/main/java/com/postech/oficinamecanica
├── domain/material
│   ├── Material.java                    [CRIAR] agregado; dados de catálogo + estoque, sem regra nesta fase
│   └── package-info.java                [CORRIGIR] declara "domain.peca" (typo pré-existente)
├── application/material
│   ├── MaterialRepository.java          [CRIAR] porta (interface, sem JPA)
│   └── ListMaterialsUseCase.java        [CRIAR] resolve status padrão + delega ao repositório
├── infrastructure/persistence/material
│   ├── MaterialJpaEntity.java           [CRIAR] mapeia tabela material
│   ├── MaterialJpaRepository.java       [CRIAR] Spring Data + query com ORDER BY id ASC
│   ├── MaterialPersistenceMapper.java   [CRIAR] MapStruct JPA → domain
│   └── MaterialRepositoryImpl.java      [CRIAR] implementa a porta
├── interfaces/rest/material
│   ├── MaterialResponse.java            [CRIAR] DTO de saída (record + @Schema)
│   ├── MaterialRestMapper.java          [CRIAR] MapStruct domain → DTO
│   └── MaterialController.java          [CRIAR] GET /api/materials
src/main/resources
└── db/migration/V3__seed_materials.sql  [CRIAR] dados de catálogo para os testes de integração
src/test/java/com/postech/oficinamecanica
├── application/material/ListMaterialsUseCaseTest.java                     [CRIAR] JUnit + Mockito
├── infrastructure/persistence/material/MaterialJpaRepositoryIntegrationTest.java [CRIAR] @DataJpaTest + Testcontainers
├── interfaces/rest/material/MaterialControllerTest.java                   [CRIAR] @WebMvcTest
└── interfaces/rest/material/MaterialControllerIntegrationTest.java        [CRIAR] @SpringBootTest + Testcontainers
```

Nenhum arquivo existente é modificado, exceto o `package-info.java` do pacote `domain/material` (typo pré-existente no nome do pacote).

**Decisões de decomposição:**
- Sem `MaterialTransaction`, sem `stock` como VO, sem `toPersistence` no mapper de persistência: nada disso é usado por um GET (YAGNI). Escrita de material é outro plano.
- `Material` fica com 9 argumentos de construtor, acima da diretriz "≤3 argumentos" de `principios-de-codigo.md`. É consciente: `Customer` (8 argumentos) estabeleceu o padrão e agrupar em VOs aqui seria abstração especulativa.
- Nada em `infrastructure/security` — sem autenticação no projeto, qualquer config de segurança aqui seria código morto.

---

### Tarefa 1: Camada Domínio — Material

**Arquivos:**
- Criar: `src/main/java/com/postech/oficinamecanica/domain/material/Material.java`
- Modificar: `src/main/java/com/postech/oficinamecanica/domain/material/package-info.java`

**Interfaces:**
- Consome: `EntityStatus` de `com.postech.oficinamecanica.domain.shared` (enum já existente com `ACTIVE`, `INACTIVE`).
- Produz: classe `Material` com construtor `Material(Long id, String name, String description, BigDecimal price, Integer stockQuantity, Integer stockMinimum, EntityStatus status, Instant createdAt, Instant updatedAt)` e getters `getId()`, `getName()`, `getDescription()`, `getPrice()`, `getStockQuantity()`, `getStockMinimum()`, `getStatus()`, `getCreatedAt()`, `getUpdatedAt()`.

**Sem teste nesta tarefa, de propósito.** `Material` não tem invariante nem comportamento neste escopo (listagem read-only) — testar getters seria teste tautológico, proibido por `docs/contexts/testes-automatizados.md`. `Customer.java` também não tem teste de domínio. As regras de estoque (baixa, mínimo) entram quando existir caso de uso que as exercite.

- [ ] **Passo 1: Corrigir o package-info do pacote material**

O arquivo existe e declara o pacote errado (`domain.peca`, resquício de nomeação em português). Substitua o conteúdo inteiro de `src/main/java/com/postech/oficinamecanica/domain/material/package-info.java` por:

```java
package com.postech.oficinamecanica.domain.material;
```

- [ ] **Passo 2: Criar a entidade de domínio Material**

Criar `src/main/java/com/postech/oficinamecanica/domain/material/Material.java`:

```java
package com.postech.oficinamecanica.domain.material;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.math.BigDecimal;
import java.time.Instant;

public class Material {
    private final Long id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final Integer stockQuantity;
    private final Integer stockMinimum;
    private final EntityStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Material(Long id, String name, String description, BigDecimal price,
                    Integer stockQuantity, Integer stockMinimum, EntityStatus status,
                    Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.stockMinimum = stockMinimum;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Integer getStockMinimum() { return stockMinimum; }
    public EntityStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
```

- [ ] **Passo 3: Compilar**

Executar: `mvn -q clean compile`

Esperado: BUILD SUCCESS.

- [ ] **Passo 4: Commit** (só com autorização do usuário)

```bash
git add src/main/java/com/postech/oficinamecanica/domain/material
git commit -m "feat: add Material domain entity"
```

---

### Tarefa 2: Camada Aplicação — Porta e Use Case (TDD)

**Arquivos:**
- Criar: `src/test/java/com/postech/oficinamecanica/application/material/ListMaterialsUseCaseTest.java`
- Criar: `src/main/java/com/postech/oficinamecanica/application/material/MaterialRepository.java`
- Criar: `src/main/java/com/postech/oficinamecanica/application/material/ListMaterialsUseCase.java`

**Interfaces:**
- Consome: `Material` (tarefa 1), `EntityStatus` (`domain.shared`).
- Produz: interface `MaterialRepository` com `List<Material> findByStatus(EntityStatus status)`; `@Service ListMaterialsUseCase` com construtor `ListMaterialsUseCase(MaterialRepository repository)` e método `List<Material> execute(String statusParam)`.

**Regra de negócio que mora aqui (não no controller):** `statusParam` nulo ou em branco vira `ACTIVE`; qualquer outro valor é normalizado com `toUpperCase()` e convertido por `EntityStatus.valueOf(...)`, que lança `IllegalArgumentException` em valor desconhecido. `arquitetura-ddd.md` traz exatamente este exemplo como o jeito **correto** (controller só converte HTTP → tipo). `ListCustomersUseCase` já implementa isso; copie o comportamento.

- [ ] **Passo 1: Escrever o teste que falha**

Criar `src/test/java/com/postech/oficinamecanica/application/material/ListMaterialsUseCaseTest.java`:

```java
package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListMaterialsUseCaseTest {
    @Mock
    private MaterialRepository repository;

    @InjectMocks
    private ListMaterialsUseCase useCase;

    @Test
    void shouldReturnActiveMaterialsWhenStatusParamIsNull() {
        when(repository.findByStatus(EntityStatus.ACTIVE))
            .thenReturn(List.of(aMaterial(1L, "Filtro de Oleo", EntityStatus.ACTIVE)));

        List<Material> result = useCase.execute(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Filtro de Oleo");
    }

    @Test
    void shouldReturnActiveMaterialsWhenStatusParamIsBlank() {
        when(repository.findByStatus(EntityStatus.ACTIVE))
            .thenReturn(List.of(aMaterial(1L, "Filtro de Oleo", EntityStatus.ACTIVE)));

        List<Material> result = useCase.execute("   ");

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnActiveMaterialsWhenStatusIsLowercase() {
        when(repository.findByStatus(EntityStatus.ACTIVE))
            .thenReturn(List.of(aMaterial(1L, "Filtro de Oleo", EntityStatus.ACTIVE)));

        List<Material> result = useCase.execute("active");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void shouldReturnInactiveMaterialsWhenStatusIsInactive() {
        when(repository.findByStatus(EntityStatus.INACTIVE))
            .thenReturn(List.of(aMaterial(5L, "Bateria 60Ah", EntityStatus.INACTIVE)));

        List<Material> result = useCase.execute("INACTIVE");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    void shouldReturnEmptyListWhenNoMaterialMatchesStatus() {
        when(repository.findByStatus(EntityStatus.INACTIVE)).thenReturn(List.of());

        List<Material> result = useCase.execute("INACTIVE");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRejectUnknownStatus() {
        assertThatThrownBy(() -> useCase.execute("ARCHIVED"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static Material aMaterial(Long id, String name, EntityStatus status) {
        return new Material(
            id,
            name,
            "Descricao de catalogo",
            new BigDecimal("32.50"),
            25,
            5,
            status,
            Instant.now(),
            Instant.now()
        );
    }
}
```

- [ ] **Passo 2: Rodar o teste e confirmar que falha**

Executar: `mvn -q test -Dtest=ListMaterialsUseCaseTest`

Esperado: FALHA de compilação — `cannot find symbol: class MaterialRepository` / `class ListMaterialsUseCase`. Em Java a fase vermelha do TDD é o erro de compilação; não crie stubs antes.

- [ ] **Passo 3: Criar a porta MaterialRepository**

Criar `src/main/java/com/postech/oficinamecanica/application/material/MaterialRepository.java`:

```java
package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.util.List;

public interface MaterialRepository {
    List<Material> findByStatus(EntityStatus status);
}
```

- [ ] **Passo 4: Criar o ListMaterialsUseCase**

Criar `src/main/java/com/postech/oficinamecanica/application/material/ListMaterialsUseCase.java`:

```java
package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListMaterialsUseCase {
    private final MaterialRepository repository;

    public ListMaterialsUseCase(MaterialRepository repository) {
        this.repository = repository;
    }

    public List<Material> execute(String statusParam) {
        EntityStatus status = (statusParam == null || statusParam.isBlank())
            ? EntityStatus.ACTIVE
            : EntityStatus.valueOf(statusParam.toUpperCase());

        return repository.findByStatus(status);
    }
}
```

- [ ] **Passo 5: Rodar o teste e confirmar que passa**

Executar: `mvn -q test -Dtest=ListMaterialsUseCaseTest`

Esperado: 6 testes, 0 falhas.

- [ ] **Passo 6: Commit** (só com autorização do usuário)

```bash
git add src/main/java/com/postech/oficinamecanica/application/material src/test/java/com/postech/oficinamecanica/application/material
git commit -m "feat: add ListMaterialsUseCase with status filter default"
```

---

### Tarefa 3: Migration de Dados — Catálogo de Materiais

**Arquivos:**
- Criar: `src/main/resources/db/migration/V3__seed_materials.sql`

**Interfaces:**
- Consome: tabela `material` criada em `V1__initial_schema.sql`.
- Produz: 5 linhas em `material`, ids 1 a 5 (`BIGSERIAL` numa tabela vazia), sendo 4 `ACTIVE` e 1 `INACTIVE`. Esses valores são as asserções dos testes de integração das tarefas 4 e 7 — não altere sem atualizar os testes.

**Por que uma migration e não um script de teste:** `V2__new_customers.sql` já semeia clientes do mesmo jeito, e os testes de integração usam Flyway no container. Um só caminho de carga, sem `@Sql` paralelo.

- [ ] **Passo 1: Criar a migration**

Criar `src/main/resources/db/migration/V3__seed_materials.sql`:

```sql
INSERT INTO material (name, description, price, stock_quantity, stock_minimum, status) VALUES
('Óleo Motor 5W30 Sintético',   'Galão de 4 litros para motores flex',  189.90, 40, 10, 'ACTIVE'),
('Filtro de Óleo',              'Compatível com linha leve Fiat/VW',     32.50, 25,  5, 'ACTIVE'),
('Pastilha de Freio Dianteira', 'Jogo com 4 pastilhas cerâmicas',       215.00, 12,  4, 'ACTIVE'),
('Correia Dentada',             NULL,                                    98.75,  8,  3, 'ACTIVE'),
('Bateria 60Ah',                'Descontinuada pelo fornecedor',        549.90,  0,  2, 'INACTIVE');
```

`Correia Dentada` fica com `description` nula de propósito: prova que a coluna nullable atravessa JPA → domain → JSON sem quebrar.

- [ ] **Passo 2: Validar a migration contra o banco local**

Docker precisa estar rodando (o `spring-boot-docker-compose` sobe o Postgres do `docker-compose.yml`).

Executar: `mvn -q spring-boot:run`

Esperado: log `Migrating schema "public" to version "3 - seed materials"` e aplicação sobe sem erro. Encerre com Ctrl+C.

Se o banco local já tiver sido criado antes desta migration, o Flyway aplica só a V3 — sem `clean`, sem recriar volume.

- [ ] **Passo 3: Commit** (só com autorização do usuário)

```bash
git add src/main/resources/db/migration/V3__seed_materials.sql
git commit -m "feat: seed material catalog data"
```

---

### Tarefa 4: Camada Infraestrutura — JPA, Mapper e Implementação da Porta (com teste de integração)

**Arquivos:**
- Criar: `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialJpaEntity.java`
- Criar: `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialJpaRepository.java`
- Criar: `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialPersistenceMapper.java`
- Criar: `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialRepositoryImpl.java`
- Criar: `src/test/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialJpaRepositoryIntegrationTest.java`

**Interfaces:**
- Consome: `MaterialRepository` (tarefa 2), `Material` (tarefa 1), `EntityStatus`, dados da `V3` (tarefa 3).
- Produz: `MaterialJpaEntity` mapeando a tabela `material`; `MaterialJpaRepository.findByStatusOrderById(EntityStatus status)` retornando `List<MaterialJpaEntity>`; `MaterialPersistenceMapper.toDomain(MaterialJpaEntity)` retornando `Material`; `@Repository MaterialRepositoryImpl` implementando `findByStatus`.

**Por que só `toDomain` no mapper:** esta fatia é read-only. `CustomerPersistenceMapper` tem `toPersistence` sem uso; não replique código morto.

- [ ] **Passo 1: Escrever o teste de integração de persistência que falha**

Criar `src/test/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialJpaRepositoryIntegrationTest.java`:

```java
package com.postech.oficinamecanica.infrastructure.persistence.material;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class MaterialJpaRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.docker.compose.enabled", () -> "false");
    }

    @Autowired
    private MaterialJpaRepository repository;

    @Test
    void shouldReturnOnlyActiveMaterialsOrderedByIdAscending() {
        List<MaterialJpaEntity> result = repository.findByStatusOrderById(EntityStatus.ACTIVE);

        assertThat(result).hasSize(4);
        assertThat(result).extracting(MaterialJpaEntity::getId).containsExactly(1L, 2L, 3L, 4L);
        assertThat(result).extracting(MaterialJpaEntity::getStatus)
            .containsOnly(EntityStatus.ACTIVE);
    }

    @Test
    void shouldReturnOnlyInactiveMaterialsWhenFilteringByInactive() {
        List<MaterialJpaEntity> result = repository.findByStatusOrderById(EntityStatus.INACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Bateria 60Ah");
    }

    @Test
    void shouldMapMonetaryAndStockColumnsWithoutPrecisionLoss() {
        MaterialJpaEntity first = repository.findByStatusOrderById(EntityStatus.ACTIVE).get(0);

        assertThat(first.getPrice()).isEqualByComparingTo("189.90");
        assertThat(first.getStockQuantity()).isEqualTo(40);
        assertThat(first.getStockMinimum()).isEqualTo(10);
        assertThat(first.getCreatedAt()).isNotNull();
        assertThat(first.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldAcceptNullDescription() {
        MaterialJpaEntity withoutDescription = repository.findByStatusOrderById(EntityStatus.ACTIVE).get(3);

        assertThat(withoutDescription.getName()).isEqualTo("Correia Dentada");
        assertThat(withoutDescription.getDescription()).isNull();
    }
}
```

Notas que evitam duas horas de depuração:
- `Replace.NONE` impede o Boot de trocar o DataSource por um embutido; o Flyway roda as migrations V1→V3 no container antes dos testes.
- `ddl-auto=validate` explícito garante que o Hibernate não tente criar/derrubar tabelas por cima do Flyway.
- `spring.docker.compose.enabled=false` evita que o suporte a docker-compose do Boot tente subir o `docker-compose.yml` durante o teste.
- O container é `static`: sobe uma vez por classe, não por método.

- [ ] **Passo 2: Rodar e confirmar que falha**

Executar: `mvn -q test -Dtest=MaterialJpaRepositoryIntegrationTest`

Esperado: FALHA de compilação — `cannot find symbol: class MaterialJpaRepository`.

- [ ] **Passo 3: Criar MaterialJpaEntity**

Criar `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialJpaEntity.java`:

```java
package com.postech.oficinamecanica.infrastructure.persistence.material;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "material")
public class MaterialJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private Integer stockMinimum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public MaterialJpaEntity() {}

    public MaterialJpaEntity(Long id, String name, String description, BigDecimal price,
                             Integer stockQuantity, Integer stockMinimum, EntityStatus status,
                             Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.stockMinimum = stockMinimum;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Integer getStockMinimum() { return stockMinimum; }
    public EntityStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
```

Sem `@Column(name = ...)`: a estratégia de nomes padrão do Hibernate converte `stockQuantity` → `stock_quantity` e `createdAt` → `created_at`, batendo com o schema. Sem setters: nada escreve material nesta fatia.

- [ ] **Passo 4: Criar MaterialJpaRepository**

Criar `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialJpaRepository.java`:

```java
package com.postech.oficinamecanica.infrastructure.persistence.material;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MaterialJpaRepository extends JpaRepository<MaterialJpaEntity, Long> {
    @Query("SELECT m FROM MaterialJpaEntity m WHERE m.status = :status ORDER BY m.id ASC")
    List<MaterialJpaEntity> findByStatusOrderById(@Param("status") EntityStatus status);
}
```

- [ ] **Passo 5: Criar MaterialPersistenceMapper**

Criar `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialPersistenceMapper.java`:

```java
package com.postech.oficinamecanica.infrastructure.persistence.material;

import com.postech.oficinamecanica.domain.material.Material;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MaterialPersistenceMapper {

    Material toDomain(MaterialJpaEntity entity);
}
```

Nenhum `@Mapping` é necessário: os nove parâmetros do construtor de `Material` têm getters de mesmo nome em `MaterialJpaEntity`. Se o build acusar `Unmapped target property`, é sinal de nome divergente — corrija o nome, não silencie a política.

- [ ] **Passo 6: Criar MaterialRepositoryImpl**

Criar `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialRepositoryImpl.java`:

```java
package com.postech.oficinamecanica.infrastructure.persistence.material;

import com.postech.oficinamecanica.application.material.MaterialRepository;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class MaterialRepositoryImpl implements MaterialRepository {
    private final MaterialJpaRepository jpaRepository;
    private final MaterialPersistenceMapper mapper;

    public MaterialRepositoryImpl(MaterialJpaRepository jpaRepository, MaterialPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Material> findByStatus(EntityStatus status) {
        return jpaRepository.findByStatusOrderById(status)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
}
```

- [ ] **Passo 7: Rodar o teste de integração de persistência**

Docker precisa estar rodando.

Executar: `mvn -q test -Dtest=MaterialJpaRepositoryIntegrationTest`

Esperado: 4 testes, 0 falhas. Nos logs, o Testcontainers baixa/sobe `postgres:16-alpine` e o Flyway aplica V1→V3.

- [ ] **Passo 8: Commit** (só com autorização do usuário)

```bash
git add src/main/java/com/postech/oficinamecanica/infrastructure/persistence/material src/test/java/com/postech/oficinamecanica/infrastructure/persistence/material
git commit -m "feat: add Material persistence layer with status filter query"
```

---

### Tarefa 5: Camada Interfaces — DTO, Mapper e Controller (TDD)

**Arquivos:**
- Criar: `src/test/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialControllerTest.java`
- Criar: `src/main/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialResponse.java`
- Criar: `src/main/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialRestMapper.java`
- Criar: `src/main/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialController.java`

**Interfaces:**
- Consome: `ListMaterialsUseCase` (tarefa 2), `Material` (tarefa 1), `ErrorResponse` de `interfaces.rest.config` (já existe: `record ErrorResponse(String code, String message, int status)`).
- Produz: `record MaterialResponse(Long id, String name, String description, BigDecimal price, Integer stockQuantity, Integer stockMinimum, String status, Instant createdAt, Instant updatedAt)`; `MaterialRestMapper.toResponse(Material domain)`; `MaterialController` com `GET /api/materials` aceitando query param opcional `status` e retornando `List<MaterialResponse>`.

**Status inválido → 400:** nada a implementar. `GlobalExceptionHandler` (já existe) trata `IllegalArgumentException` devolvendo 400 com `code=INVALID_STATUS`.

- [ ] **Passo 1: Escrever o teste de controller que falha**

Criar `src/test/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialControllerTest.java`:

```java
package com.postech.oficinamecanica.interfaces.rest.material;

import com.postech.oficinamecanica.application.material.ListMaterialsUseCase;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaterialController.class)
class MaterialControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListMaterialsUseCase listMaterialsUseCase;

    @MockitoBean
    private MaterialRestMapper mapper;

    @Test
    void shouldReturnMaterialsWhenNoStatusFilterProvided() throws Exception {
        Material material = aMaterial(1L, "Filtro de Oleo", EntityStatus.ACTIVE);
        when(listMaterialsUseCase.execute(null)).thenReturn(List.of(material));
        when(mapper.toResponse(material)).thenReturn(aResponse(1L, "Filtro de Oleo", "ACTIVE"));

        mockMvc.perform(get("/api/materials"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Filtro de Oleo"))
            .andExpect(jsonPath("$[0].price").value(32.50))
            .andExpect(jsonPath("$[0].stockQuantity").value(25))
            .andExpect(jsonPath("$[0].stockMinimum").value(5))
            .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void shouldForwardStatusFilterToUseCase() throws Exception {
        Material material = aMaterial(5L, "Bateria 60Ah", EntityStatus.INACTIVE);
        when(listMaterialsUseCase.execute("INACTIVE")).thenReturn(List.of(material));
        when(mapper.toResponse(material)).thenReturn(aResponse(5L, "Bateria 60Ah", "INACTIVE"));

        mockMvc.perform(get("/api/materials?status=INACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("INACTIVE"));
    }

    @Test
    void shouldReturnEmptyListWhenNoMaterialMatchesFilter() throws Exception {
        when(listMaterialsUseCase.execute(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/materials"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsUnknown() throws Exception {
        when(listMaterialsUseCase.execute("ARCHIVED"))
            .thenThrow(new IllegalArgumentException("No enum constant ARCHIVED"));

        mockMvc.perform(get("/api/materials?status=ARCHIVED"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_STATUS"));
    }

    private static Material aMaterial(Long id, String name, EntityStatus status) {
        return new Material(id, name, "Descricao de catalogo", new BigDecimal("32.50"),
            25, 5, status, Instant.now(), Instant.now());
    }

    private static MaterialResponse aResponse(Long id, String name, String status) {
        return new MaterialResponse(id, name, "Descricao de catalogo", new BigDecimal("32.50"),
            25, 5, status, Instant.now(), Instant.now());
    }
}
```

Nota: o teste de 400 exige que `GlobalExceptionHandler` esteja no slice. `@WebMvcTest` carrega `@RestControllerAdvice` automaticamente — não o adicione manualmente ao `@WebMvcTest(...)`. A estrutura é a mesma de `CustomerControllerTest`, que já existe e passa.

- [ ] **Passo 2: Rodar e confirmar que falha**

Executar: `mvn -q test -Dtest=MaterialControllerTest`

Esperado: FALHA de compilação — `cannot find symbol: class MaterialController` / `MaterialResponse` / `MaterialRestMapper`.

- [ ] **Passo 3: Criar MaterialResponse**

Criar `src/main/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialResponse.java`:

```java
package com.postech.oficinamecanica.interfaces.rest.material;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

public record MaterialResponse(
    @Schema(description = "Identificador único", example = "1")
    Long id,

    @Schema(description = "Nome do material", example = "Filtro de Óleo")
    String name,

    @Schema(description = "Descrição do material (opcional)", example = "Compatível com linha leve Fiat/VW")
    String description,

    @Schema(description = "Preço de catálogo vigente", example = "32.50")
    BigDecimal price,

    @Schema(description = "Saldo atual em estoque", example = "25")
    Integer stockQuantity,

    @Schema(description = "Estoque mínimo; abaixo disso o material precisa de reposição", example = "5")
    Integer stockMinimum,

    @Schema(description = "Status do material", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    String status,

    @Schema(description = "Data e hora de criação (ISO-8601)", example = "2026-08-16T10:30:00Z")
    Instant createdAt,

    @Schema(description = "Data e hora da última atualização (ISO-8601)", example = "2026-08-16T10:30:00Z")
    Instant updatedAt
) {}
```

- [ ] **Passo 4: Criar MaterialRestMapper**

Criar `src/main/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialRestMapper.java`:

```java
package com.postech.oficinamecanica.interfaces.rest.material;

import com.postech.oficinamecanica.domain.material.Material;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MaterialRestMapper {

    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    MaterialResponse toResponse(Material domain);
}
```

O nome do parâmetro **precisa** ser `domain`: a `expression` é copiada literalmente para o código gerado.

- [ ] **Passo 5: Criar MaterialController**

Criar `src/main/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialController.java`:

```java
package com.postech.oficinamecanica.interfaces.rest.material;

import com.postech.oficinamecanica.application.material.ListMaterialsUseCase;
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
@RequestMapping("/api/materials")
@Tag(name = "Materials", description = "Catálogo de materiais e saldo de estoque")
public class MaterialController {
    private final ListMaterialsUseCase listMaterialsUseCase;
    private final MaterialRestMapper mapper;

    public MaterialController(ListMaterialsUseCase listMaterialsUseCase, MaterialRestMapper mapper) {
        this.listMaterialsUseCase = listMaterialsUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(
        summary = "Listar materiais por status",
        description = "Retorna os materiais filtrados por status (ACTIVE/INACTIVE), ordenados por ID ascendente. Padrão: ACTIVE se não informado."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de materiais recuperada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MaterialResponse.class)))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Status inválido (deve ser ACTIVE ou INACTIVE)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public List<MaterialResponse> list(
        @Parameter(
            description = "Filtro de status: ACTIVE ou INACTIVE (insensível a maiúsculas)",
            example = "ACTIVE",
            required = false
        )
        @RequestParam(required = false) String status
    ) {
        return listMaterialsUseCase.execute(status)
            .stream()
            .map(mapper::toResponse)
            .toList();
    }
}
```

Só 200 e 400 estão documentados porque só eles existem hoje — `openapi-annotations.md`, regra 6: "lista status reais hoje, não antecipe 401/403". Quando a autenticação entrar, o 401 é adicionado no plano dela.

- [ ] **Passo 6: Rodar o teste de controller**

Executar: `mvn -q test -Dtest=MaterialControllerTest`

Esperado: 4 testes, 0 falhas.

- [ ] **Passo 7: Commit** (só com autorização do usuário)

```bash
git add src/main/java/com/postech/oficinamecanica/interfaces/rest/material src/test/java/com/postech/oficinamecanica/interfaces/rest/material
git commit -m "feat: add GET /api/materials endpoint"
```

---

### Tarefa 6: Teste de Integração HTTP — Endpoint de Ponta a Ponta

**Arquivos:**
- Criar: `src/test/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialControllerIntegrationTest.java`

**Interfaces:**
- Consome: aplicação inteira (`@SpringBootTest`), dados da `V3` (tarefa 3), endpoint da tarefa 5.
- Produz: cobertura de integração de `GET /api/materials` — filtro, ordenação, payload completo, campo nulo e status inválido, contra Postgres real com as migrations V1→V3 aplicadas pelo Flyway.

Esta é a última tarefa: ao final dela o endpoint está entregue e verificado.

- [ ] **Passo 1: Criar o teste de integração HTTP**

Criar `src/test/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialControllerIntegrationTest.java`:

```java
package com.postech.oficinamecanica.interfaces.rest.material;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class MaterialControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.docker.compose.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnActiveMaterialsWhenNoStatusFilterProvided() throws Exception {
        mockMvc.perform(get("/api/materials"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(4)))
            .andExpect(jsonPath("$[*].status", contains("ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE")));
    }

    @Test
    void shouldReturnMaterialsOrderedByIdAscending() throws Exception {
        mockMvc.perform(get("/api/materials").param("status", "ACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[2].id").value(3))
            .andExpect(jsonPath("$[3].id").value(4));
    }

    @Test
    void shouldReturnEveryContractFieldForFirstMaterial() throws Exception {
        mockMvc.perform(get("/api/materials"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Óleo Motor 5W30 Sintético"))
            .andExpect(jsonPath("$[0].description").value("Galão de 4 litros para motores flex"))
            .andExpect(jsonPath("$[0].price").value(189.90))
            .andExpect(jsonPath("$[0].stockQuantity").value(40))
            .andExpect(jsonPath("$[0].stockMinimum").value(10))
            .andExpect(jsonPath("$[0].status").value("ACTIVE"))
            .andExpect(jsonPath("$[0].createdAt").exists())
            .andExpect(jsonPath("$[0].updatedAt").exists());
    }

    @Test
    void shouldReturnNullDescriptionWhenMaterialHasNone() throws Exception {
        mockMvc.perform(get("/api/materials"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[3].name").value("Correia Dentada"))
            .andExpect(jsonPath("$[3].description").isEmpty());
    }

    @Test
    void shouldReturnInactiveMaterialsWhenStatusIsInactive() throws Exception {
        mockMvc.perform(get("/api/materials").param("status", "INACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(5))
            .andExpect(jsonPath("$[0].name").value("Bateria 60Ah"))
            .andExpect(jsonPath("$[0].status").value("INACTIVE"));
    }

    @Test
    void shouldAcceptLowercaseStatusFilter() throws Exception {
        mockMvc.perform(get("/api/materials").param("status", "inactive"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(5));
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsUnknown() throws Exception {
        mockMvc.perform(get("/api/materials").param("status", "ARCHIVED"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_STATUS"))
            .andExpect(jsonPath("$.status").value(400));
    }
}
```

- [ ] **Passo 2: Rodar o teste de integração**

Docker precisa estar rodando.

Executar: `mvn -q test -Dtest=MaterialControllerIntegrationTest`

Esperado: 7 testes, 0 falhas.

Se `$[0].price` falhar com `expected 189.90 but was 189.9`: o JsonPath lê números JSON como `Double`, e `.value(189.90)` compara `Double` com `Double` — deve passar. Se seu ambiente devolver `BigDecimal`, troque por `jsonPath("$[0].price").value(org.hamcrest.Matchers.comparesEqualTo(new java.math.BigDecimal("189.90")))`.

- [ ] **Passo 3: Rodar a suíte inteira**

Executar: `mvn clean test`

Esperado: todos os testes passam (Customer + Material). Confira no resumo do surefire que `MaterialControllerIntegrationTest` e `MaterialJpaRepositoryIntegrationTest` aparecem — se não aparecerem, o nome da classe está fora dos includes padrão.

- [ ] **Passo 4: Verificação manual da aplicação real**

Subir a aplicação (Docker rodando; o `spring-boot-docker-compose` cuida do Postgres):

Executar: `mvn spring-boot:run`

```bash
curl -s http://localhost:8080/api/materials
```

Esperado: 4 materiais ACTIVE, ids 1→4, com os nove campos do contrato. `Correia Dentada` vem com `"description": null`.

```bash
curl -s "http://localhost:8080/api/materials?status=INACTIVE"
```

Esperado: 1 material — `Bateria 60Ah`, id 5.

```bash
curl -i "http://localhost:8080/api/materials?status=ARCHIVED"
```

Esperado: `HTTP/1.1 400` e corpo `{"code":"INVALID_STATUS","message":"Status deve ser ACTIVE ou INACTIVE","status":400}`.

Exemplo de item da resposta:

```json
{
  "id": 2,
  "name": "Filtro de Óleo",
  "description": "Compatível com linha leve Fiat/VW",
  "price": 32.50,
  "stockQuantity": 25,
  "stockMinimum": 5,
  "status": "ACTIVE",
  "createdAt": "2026-08-16T13:05:22.481Z",
  "updatedAt": "2026-08-16T13:05:22.481Z"
}
```

Abrir `http://localhost:8080/swagger-ui.html`: `GET /api/materials` aparece sob a tag **Materials**, com o parâmetro `status` documentado e o schema `MaterialResponse` com description/example em todos os campos.

O endpoint responde **sem token** — é o esperado: não existe autenticação no projeto (ver "Fora de Escopo").

Encerrar com Ctrl+C.

- [ ] **Passo 5: Commit** (só com autorização do usuário)

```bash
git add src/test/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialControllerIntegrationTest.java
git commit -m "test: add integration tests for GET /api/materials"
```

---

## Checklist de Entrega

- [ ] `mvn clean test` verde, com as 4 classes de teste de Material aparecendo no resumo do surefire
- [ ] `GET /api/materials` → 200 com 4 materiais ACTIVE, ids 1→4
- [ ] `?status=INACTIVE` → 1 material (id 5); `?status=inactive` idem
- [ ] `?status=ARCHIVED` → 400 com `code=INVALID_STATUS`
- [ ] Payload traz `id, name, description, price, stockQuantity, stockMinimum, status, createdAt, updatedAt`
- [ ] `price` é `BigDecimal` com 2 decimais em todo o caminho
- [ ] `description` nula atravessa até o JSON sem quebrar
- [ ] Swagger UI mostra a tag **Materials** com o parâmetro `status` documentado
- [ ] Nenhum import de `infrastructure` dentro de `interfaces.rest`
- [ ] Relatório JaCoCo gerado (`target/site/jacoco/index.html`)

## Fora de Escopo (candidatos a planos próprios)

1. **Autenticação — plano próprio, e é o mais urgente.** Removido deste plano em 16/08/2026 porque o projeto não tem método de autenticação nenhum: sem starter de security no `pom.xml`, `infrastructure/security/` só com `package-info.java`, e sem emissão de token (tabela `employee` existe no schema, mas sem entidade, repositório ou caso de uso). Consequência aceita: **`/api/customers` e `/api/materials` ficam públicos**. O plano de autenticação precisa cobrir, na mesma entrega: dependência de security aprovada, emissão de token (`POST /api/auth/login` com bcrypt sobre `employee`), proteção de `/api/**` liberando Swagger, `@ApiResponse` 401 nos controllers, security scheme `bearerAuth` no `OpenApiConfig`, e ajuste das fatias `@WebMvcTest` existentes — que passam a responder 401 no instante em que o Spring Security entra no classpath (`@AutoConfigureMockMvc(addFilters = false)` em `CustomerControllerTest` e `MaterialControllerTest`).
2. **Autorização por papel** — `role` já existe em `employee`, mas nada lê. Depende do item 1.
3. **CRUD de material** (POST/PUT/PATCH/DELETE), regras de estoque e `MaterialTransaction`.
4. **Limpar os `package-info.java`** que ainda declaram pacotes em português (`domain.cliente`, `domain.veiculo`, `domain.servico`, `domain.ordemservico`). Este plano corrige apenas o de `domain/material`, por estar no caminho.
5. **Corrigir `docs/contexts/testes-automatizados.md` e `openapi-annotations.md`**, que dizem "Spring Boot 4" enquanto o `pom.xml` está em 3.5.16.
