# Endpoint Estoque Baixo — Plano de Implementação

> **Para trabalhadores agentic:** SUB-SKILL OBRIGATÓRIA: Use superpowers:subagent-driven-development ou superpowers:executing-plans para implementar este plano tarefa por tarefa. Passos usam sintaxe checkbox (`- [ ]`).

**Objetivo:** Expor `GET /api/materials/low-stock` para listar materiais cujo `stock_quantity` seja estritamente menor que `stock_minimum`, filtrando por status, ordenado por ID ascendente.

**Arquitetura:** Este plano é um acréscimo à fatia `Material` já existente. O agregado de domínio `Material`, os DTOs de resposta (`MaterialResponse`) e os Mappers da camada REST (`MaterialRestMapper`) e Persistência (`MaterialPersistenceMapper`) construídos no plano anterior **serão reaproveitados sem modificação**. O foco é criar o novo caso de uso e a nova query JPA.

---

## Restrições Globais

- **Código 100% inglês**; documentação e conversa em português.
- **Spring Boot 3.5.16** e Java 21. Sem Lombok.
- Nenhum arquivo existente será sobrescrito ou apagado. As tarefas consistem em adicionar novos métodos a interfaces/classes existentes ou criar as novas classes de Use Case e Testes.

---

## Estrutura de Arquivos Afetada

```text
src/main/java/com/postech/oficinamecanica
├── application/material
│   ├── MaterialRepository.java          [MODIFICAR] adicionar porta findLowStockByStatus
│   └── ListLowStockMaterialsUseCase.java [CRIAR] novo caso de uso
├── infrastructure/persistence/material
│   ├── MaterialJpaRepository.java       [MODIFICAR] adicionar query @Query com regra de estoque
│   └── MaterialRepositoryImpl.java      [MODIFICAR] implementar nova porta
├── interfaces/rest/material
│   └── MaterialController.java          [MODIFICAR] adicionar @GetMapping("/low-stock")
src/main/resources
└── db/migration/V4__seed_low_stock.sql  [CRIAR] dados específicos para testar a regra
src/test/java/com/postech/oficinamecanica
├── application/material/ListLowStockMaterialsUseCaseTest.java             [CRIAR] 
├── infrastructure/persistence/material/MaterialJpaRepositoryIntegrationTest.java [MODIFICAR]
└── interfaces/rest/material/MaterialControllerIntegrationTest.java        [MODIFICAR]
```

---

### Tarefa 1: Migration de Dados — Cenários de Estoque Baixo

**Justificativa:** Os dados da `V3` não possuem nenhum item `ACTIVE` que esteja com estoque abaixo do mínimo (todos estão acima ou iguais). Precisamos inserir um caso positivo (estoque baixo) e um caso de fronteira (estoque exatamente igual ao mínimo, que não deve ser retornado).

- [ ] **Passo 1: Criar a migration V4**

Criar `src/main/resources/db/migration/V4__seed_low_stock.sql`:

```sql
INSERT INTO material (name, description, price, stock_quantity, stock_minimum, status) VALUES
('Vela de Ignição',             'Precisa de reposição urgente',          45.00,  2,  8, 'ACTIVE'),
('Fluido de Freio DOT 4',       'Estoque exatamente no limite',          28.90, 10, 10, 'ACTIVE');
```
*(Nota de asserção: O item 'Vela de Ignição' terá ID 6 e 'Fluido de Freio' ID 7, assumindo sequenciamento contínuo após a V3).*

- [ ] **Passo 2: Compilar/Rodar localmente**
Executar `mvn -q spring-boot:run` e abortar (Ctrl+C) assim que o log indicar que a migration `V4` foi aplicada com sucesso.

---

### Tarefa 2: Camada Aplicação — Porta e Caso de Uso

- [ ] **Passo 1: Atualizar a interface MaterialRepository**

Em `src/main/java/com/postech/oficinamecanica/application/material/MaterialRepository.java`, adicione:

```java
    List<Material> findLowStockByStatus(EntityStatus status);
```

- [ ] **Passo 2: Criar o teste do novo Use Case**

Criar `src/test/java/com/postech/oficinamecanica/application/material/ListLowStockMaterialsUseCaseTest.java`:

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
class ListLowStockMaterialsUseCaseTest {

    @Mock
    private MaterialRepository repository;

    @InjectMocks
    private ListLowStockMaterialsUseCase useCase;

    @Test
    void shouldReturnActiveLowStockMaterialsWhenStatusIsNull() {
        when(repository.findLowStockByStatus(EntityStatus.ACTIVE))
            .thenReturn(List.of(aMaterial(6L, "Vela de Ignicao")));

        List<Material> result = useCase.execute(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Vela de Ignicao");
    }

    @Test
    void shouldRejectUnknownStatus() {
        assertThatThrownBy(() -> useCase.execute("DELETED"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static Material aMaterial(Long id, String name) {
        return new Material(id, name, null, BigDecimal.TEN, 2, 8, EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }
}
```

- [ ] **Passo 3: Criar ListLowStockMaterialsUseCase**

Criar `src/main/java/com/postech/oficinamecanica/application/material/ListLowStockMaterialsUseCase.java`:

```java
package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListLowStockMaterialsUseCase {
    private final MaterialRepository repository;

    public ListLowStockMaterialsUseCase(MaterialRepository repository) {
        this.repository = repository;
    }

    public List<Material> execute(String statusParam) {
        EntityStatus status = (statusParam == null || statusParam.isBlank())
            ? EntityStatus.ACTIVE
            : EntityStatus.valueOf(statusParam.toUpperCase());

        return repository.findLowStockByStatus(status);
    }
}
```

- [ ] **Passo 4: Rodar o teste e confirmar**
Executar `mvn -q test -Dtest=ListLowStockMaterialsUseCaseTest`

---

### Tarefa 3: Camada Infraestrutura — Atualizar Persistência

- [ ] **Passo 1: Adicionar teste em MaterialJpaRepositoryIntegrationTest**

Em `src/test/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialJpaRepositoryIntegrationTest.java`, adicione os testes:

```java
    @Test
    void shouldReturnOnlyActiveMaterialsWithStockStrictlyBelowMinimum() {
        List<MaterialJpaEntity> result = repository.findLowStockByStatusOrderById(EntityStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(6L);
        assertThat(result.get(0).getName()).isEqualTo("Vela de Ignição");
    }

    @Test
    void shouldReturnInactiveMaterialsWithLowStock() {
        List<MaterialJpaEntity> result = repository.findLowStockByStatusOrderById(EntityStatus.INACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(5L); // Bateria 60Ah da V3 (0 < 2)
    }
```

- [ ] **Passo 2: Atualizar MaterialJpaRepository**

Em `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialJpaRepository.java`, adicione a query:

```java
    @Query("SELECT m FROM MaterialJpaEntity m WHERE m.status = :status AND m.stockQuantity < m.stockMinimum ORDER BY m.id ASC")
    List<MaterialJpaEntity> findLowStockByStatusOrderById(@Param("status") EntityStatus status);
```

- [ ] **Passo 3: Atualizar MaterialRepositoryImpl**

Em `src/main/java/com/postech/oficinamecanica/infrastructure/persistence/material/MaterialRepositoryImpl.java`, implemente o novo método:

```java
    @Override
    public List<Material> findLowStockByStatus(EntityStatus status) {
        return jpaRepository.findLowStockByStatusOrderById(status)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
```

- [ ] **Passo 4: Executar os testes de integração de repositório**
Executar `mvn -q test -Dtest=MaterialJpaRepositoryIntegrationTest`

---

### Tarefa 4: Camada Interfaces — Rota e Teste Ponta a Ponta

- [ ] **Passo 1: Adicionar teste em MaterialControllerIntegrationTest**

Em `src/test/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialControllerIntegrationTest.java`, adicione:

```java
    @Test
    void shouldReturnLowStockMaterials() throws Exception {
        mockMvc.perform(get("/api/materials/low-stock"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(6))
            .andExpect(jsonPath("$[0].name").value("Vela de Ignição"))
            .andExpect(jsonPath("$[0].stockQuantity").value(2))
            .andExpect(jsonPath("$[0].stockMinimum").value(8));
    }
```

- [ ] **Passo 2: Atualizar MaterialController**

Em `src/main/java/com/postech/oficinamecanica/interfaces/rest/material/MaterialController.java`, adicione a dependência do novo caso de uso no construtor e crie o mapeamento:

```java
    private final ListLowStockMaterialsUseCase listLowStockUseCase;

    public MaterialController(ListMaterialsUseCase listMaterialsUseCase, 
                              ListLowStockMaterialsUseCase listLowStockUseCase, 
                              MaterialRestMapper mapper) {
        this.listMaterialsUseCase = listMaterialsUseCase;
        this.listLowStockUseCase = listLowStockUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Listar materiais com estoque crítico", description = "Retorna materiais com saldo atual abaixo do estoque mínimo, filtrados por status.")
    public List<MaterialResponse> listLowStock(
        @Parameter(description = "Filtro de status", example = "ACTIVE")
        @RequestParam(required = false) String status
    ) {
        return listLowStockUseCase.execute(status)
            .stream()
            .map(mapper::toResponse)
            .toList();
    }
```

- [ ] **Passo 3: Rodar a suíte inteira**
Executar `mvn clean test` para garantir o funcionamento integrado da listagem normal e da listagem crítica.
