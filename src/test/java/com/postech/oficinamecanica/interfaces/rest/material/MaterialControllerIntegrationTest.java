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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
            .andExpect(jsonPath("$", hasSize(6)))
            .andExpect(jsonPath("$[*].status", contains("ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE")));
    }

    @Test
    void shouldReturnMaterialsOrderedByIdAscending() throws Exception {
        mockMvc.perform(get("/api/materials").param("status", "ACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[2].id").value(3))
            .andExpect(jsonPath("$[3].id").value(4))
            .andExpect(jsonPath("$[4].id").value(6))
            .andExpect(jsonPath("$[5].id").value(7));
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
            .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Material com identificador '9999' não foi encontrado."));
    }

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

    @Test
    void shouldSuccessfullyPatchMaterialStatus() throws Exception {
        mockMvc.perform(patch("/api/materials/1/status")
                .contentType("application/json")
                .content("{\"status\": \"INACTIVE\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(get("/api/materials").param("status", "INACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].id", contains(1, 5)));

        mockMvc.perform(patch("/api/materials/1/status")
                .contentType("application/json")
                .content("{\"status\": \"ACTIVE\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsInvalidOnPatch() throws Exception {
        mockMvc.perform(patch("/api/materials/1/status")
                .contentType("application/json")
                .content("{\"status\": \"INVALID_STATUS\"}"))
            .andExpect(status().isBadRequest());
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
                .contentType("application/json")
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("Pneu Aro 15"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingMaterialWithExistingName() throws Exception {
        String payload = """
            {
                "name": "Filtro de Óleo",
                "price": 50.00,
                "stockQuantity": 10,
                "stockMinimum": 2
            }
            """;

        mockMvc.perform(post("/api/materials")
                .contentType("application/json")
                .content(payload))
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
                .contentType("application/json")
                .content(payload))
            .andExpect(status().isBadRequest());
    }
}
