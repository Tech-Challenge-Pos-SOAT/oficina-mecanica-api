package com.postech.oficinamecanica.interfaces.rest.material;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.infrastructure.persistence.material.MaterialJpaEntity;
import com.postech.oficinamecanica.infrastructure.persistence.material.MaterialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.docker.compose.enabled=false"
})
class MaterialControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaterialJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldReturnActiveMaterialsWhenNoStatusFilterProvided() throws Exception {
        repository.save(aMaterial("Óleo 5W30", EntityStatus.ACTIVE));
        repository.save(aMaterial("Bateria", EntityStatus.INACTIVE));
        repository.save(aMaterial("Filtro", EntityStatus.ACTIVE));

        mockMvc.perform(get("/api/materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status", containsInAnyOrder("ACTIVE", "ACTIVE")));
    }

    @Test
    void shouldReturnMaterialWhenFetchedById() throws Exception {
        var saved = repository.save(aMaterial("Óleo Motor 5W30 Sintético", EntityStatus.ACTIVE));

        mockMvc.perform(get("/api/materials/{id}", saved.getId())) // Uso dinâmico do ID
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().intValue()))
                .andExpect(jsonPath("$.name").value("Óleo Motor 5W30 Sintético"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnNotFoundWhenFetchedWithNonExistentId() throws Exception {
        mockMvc.perform(get("/api/materials/{id}", 9999L)) // Seguro, pois o banco foi limpo no setup
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldSuccessfullyPatchMaterialStatus() throws Exception {
        var saved = repository.save(aMaterial("Correia", EntityStatus.ACTIVE));

        mockMvc.perform(patch("/api/materials/{id}/status", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"INACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().intValue()))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void shouldSuccessfullyCreateNewMaterial() throws Exception {
        String payload = """
            {
                "name": "Pneu Aro 15",
                "description": "Pneu para carros de passeio",
                "price": 380.00,
                "stockQuantity": 40,
                "stockMinimum": 4
            }
            """;

        mockMvc.perform(post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Pneu Aro 15"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingMaterialWithExistingName() throws Exception {
        repository.save(aMaterial("Filtro de Óleo", EntityStatus.ACTIVE));

        String payload = """
            {
                "name": "Filtro de Óleo",
                "price": 50.00,
                "stockQuantity": 10,
                "stockMinimum": 2
            }
            """;

        mockMvc.perform(post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnMaterialsOrderedByIdAscending() throws Exception {
        var m1 = repository.save(aMaterial("Pneu", EntityStatus.ACTIVE));
        var m2 = repository.save(aMaterial("Amortecedor", EntityStatus.ACTIVE));
        var m3 = repository.save(aMaterial("Bateria", EntityStatus.ACTIVE));

        mockMvc.perform(get("/api/materials").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(m1.getId().intValue()))
                .andExpect(jsonPath("$[1].id").value(m2.getId().intValue()))
                .andExpect(jsonPath("$[2].id").value(m3.getId().intValue()));
    }

    @Test
    void shouldReturnEveryContractFieldForFirstMaterial() throws Exception {
        var saved = repository.save(aMaterial("Óleo Motor 5W30 Sintético", EntityStatus.ACTIVE));

        mockMvc.perform(get("/api/materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(saved.getId().intValue()))
                .andExpect(jsonPath("$[0].name").value("Óleo Motor 5W30 Sintético"))
                .andExpect(jsonPath("$[0].description").value("Descrição padrão"))
                .andExpect(jsonPath("$[0].price").value(100.00)) // Baseado no Factory
                .andExpect(jsonPath("$[0].stockQuantity").value(10))
                .andExpect(jsonPath("$[0].stockMinimum").value(5))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].updatedAt").exists());
    }

    @Test
    void shouldReturnNullDescriptionWhenMaterialHasNone() throws Exception {
        var material = aMaterial("Correia Dentada", EntityStatus.ACTIVE);
        material.setDescription(null);
        repository.save(material);

        mockMvc.perform(get("/api/materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Correia Dentada"))
                .andExpect(jsonPath("$[0].description").isEmpty());
    }

    @Test
    void shouldReturnInactiveMaterialsWhenStatusIsInactive() throws Exception {
        repository.save(aMaterial("Óleo", EntityStatus.ACTIVE));
        var inactive = repository.save(aMaterial("Bateria 60Ah", EntityStatus.INACTIVE));

        mockMvc.perform(get("/api/materials").param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(inactive.getId().intValue()))
                .andExpect(jsonPath("$[0].status").value("INACTIVE"));
    }

    @Test
    void shouldAcceptLowercaseStatusFilter() throws Exception {
        var inactive = repository.save(aMaterial("Bateria 60Ah", EntityStatus.INACTIVE));

        mockMvc.perform(get("/api/materials").param("status", "inactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(inactive.getId().intValue()));
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsUnknown() throws Exception {
        mockMvc.perform(get("/api/materials").param("status", "ARCHIVED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS"));
    }

    @Test
    void shouldReturnLowStockMaterials() throws Exception {
        var lowStock = aMaterial("Vela de Ignição", EntityStatus.ACTIVE);
        lowStock.setStockQuantity(2);
        lowStock.setStockMinimum(8);
        var savedLow = repository.save(lowStock);

        var normalStock = aMaterial("Amortecedor", EntityStatus.ACTIVE);
        normalStock.setStockQuantity(20);
        normalStock.setStockMinimum(5);
        repository.save(normalStock); // Não deve retornar na query

        mockMvc.perform(get("/api/materials/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(savedLow.getId().intValue()))
                .andExpect(jsonPath("$[0].stockQuantity").value(2));
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsInvalidOnPatch() throws Exception {
        var saved = repository.save(aMaterial("Pneu", EntityStatus.ACTIVE));

        mockMvc.perform(patch("/api/materials/{id}/status", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"INVALID_STATUS\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenNegativeValuesAreProvided() throws Exception {
        String payload = """
            {
                "name": "Peça Negativa",
                "price": -10.00,
                "stockQuantity": -5,
                "stockMinimum": -1
            }
            """;

        mockMvc.perform(post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSuccessfullyUpdateExistingMaterial() throws Exception {
        var saved = repository.save(aMaterial("Óleo Antigo", EntityStatus.ACTIVE));

        String payload = """
            {
                "name": "Óleo Atualizado",
                "description": "Óleo premium com melhorias",
                "price": 199.90,
                "stockQuantity": 50,
                "stockMinimum": 12
            }
            """;

        mockMvc.perform(put("/api/materials/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().intValue()))
                .andExpect(jsonPath("$.name").value("Óleo Atualizado"))
                .andExpect(jsonPath("$.price").value(199.90))
                .andExpect(jsonPath("$.stockQuantity").value(50));
    }

    @Test
    void shouldAllowSameMaterialNameWhenUpdating() throws Exception {
        var saved = repository.save(aMaterial("Óleo Motor 5W30 Sintético", EntityStatus.ACTIVE));

        String payload = """
            {
                "name": "Óleo Motor 5W30 Sintético",
                "description": "Descrição atualizada",
                "price": 189.90,
                "stockQuantity": 40,
                "stockMinimum": 10
            }
            """;

        mockMvc.perform(put("/api/materials/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Óleo Motor 5W30 Sintético"))
                .andExpect(jsonPath("$.description").value("Descrição atualizada"));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithDuplicateName() throws Exception {
        repository.save(aMaterial("Filtro de Ar", EntityStatus.ACTIVE));
        var targetToUpdate = repository.save(aMaterial("Vela", EntityStatus.ACTIVE));

        String payload = """
            {
                "name": "Filtro de Ar",
                "price": 100.00,
                "stockQuantity": 10,
                "stockMinimum": 2
            }
            """;

        mockMvc.perform(put("/api/materials/{id}", targetToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithNegativeValues() throws Exception {
        var saved = repository.save(aMaterial("Pneu", EntityStatus.ACTIVE));

        String payload = """
            {
                "name": "Peça Negativa",
                "price": -50.00,
                "stockQuantity": -10,
                "stockMinimum": -2
            }
            """;

        mockMvc.perform(put("/api/materials/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentMaterial() throws Exception {
        String payload = """
            {
                "name": "Novo Nome",
                "price": 100.00,
                "stockQuantity": 10,
                "stockMinimum": 2
            }
            """;

        mockMvc.perform(put("/api/materials/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    private MaterialJpaEntity aMaterial(String name, EntityStatus status) {
        MaterialJpaEntity entity = new MaterialJpaEntity();
        entity.setName(name);
        entity.setDescription("Descrição padrão");
        entity.setStatus(status);
        entity.setStockQuantity(10);
        entity.setStockMinimum(5);
        entity.setPrice(new BigDecimal("100.00"));
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}
