package com.postech.oficinamecanica.interfaces.rest.materialtransaction;

import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.infrastructure.persistence.material.MaterialJpaEntity;
import com.postech.oficinamecanica.infrastructure.persistence.material.MaterialJpaRepository;
import com.postech.oficinamecanica.infrastructure.persistence.materialtransaction.MaterialTransactionJpaEntity;
import com.postech.oficinamecanica.infrastructure.persistence.materialtransaction.MaterialTransactionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.docker.compose.enabled=false"
})
class MaterialTransactionsByMaterialControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private MockMvc mockMvc;
    @Autowired private MaterialJpaRepository materialRepository;
    @Autowired private MaterialTransactionJpaRepository transactionRepository;

    private Long materialIdA;
    private Long materialIdB;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        materialRepository.deleteAll();
        materialIdA = materialRepository.save(aMaterial("Oleo 5W30")).getId();
        materialIdB = materialRepository.save(aMaterial("Bateria 60Ah")).getId();
    }

    @Test
    void shouldListOnlyTransactionsOfTheMaterialOrderedByIdAsc() throws Exception {
        saveTransaction(materialIdA, null, 100, TransactionType.IN);
        saveTransaction(materialIdB, null, 40, TransactionType.OUT);
        saveTransaction(materialIdA, null, 30, TransactionType.OUT);

        mockMvc.perform(get("/api/materials/{id}/transactions", materialIdA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].materialId").value(materialIdA.intValue()))
                .andExpect(jsonPath("$[0].quantity").value(100))
                .andExpect(jsonPath("$[0].type").value("IN"))
                .andExpect(jsonPath("$[1].quantity").value(30))
                .andExpect(jsonPath("$[1].type").value("OUT"));
    }

    @Test
    void shouldFilterByTypeOut() throws Exception {
        saveTransaction(materialIdA, null, 100, TransactionType.IN);
        saveTransaction(materialIdA, null, 40, TransactionType.OUT);

        mockMvc.perform(get("/api/materials/{id}/transactions", materialIdA).param("type", "OUT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("OUT"))
                .andExpect(jsonPath("$[0].quantity").value(40));
    }

    @Test
    void shouldReturnEmptyListWhenMaterialHasNoTransactions() throws Exception {
        mockMvc.perform(get("/api/materials/{id}/transactions", materialIdB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnFullContractFields() throws Exception {
        saveTransaction(materialIdA, null, 100, TransactionType.IN);

        mockMvc.perform(get("/api/materials/{id}/transactions", materialIdA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].materialId").value(materialIdA.intValue()))
                .andExpect(jsonPath("$[0].serviceOrderId").value(nullValue()))
                .andExpect(jsonPath("$[0].quantity").value(100))
                .andExpect(jsonPath("$[0].type").value("IN"))
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    void shouldReturn404WhenMaterialDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/materials/{id}/transactions", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturn400WhenTypeIsInvalid() throws Exception {
        mockMvc.perform(get("/api/materials/{id}/transactions", materialIdA).param("type", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER_TYPE"));
    }

    private MaterialJpaEntity aMaterial(String name) {
        return new MaterialJpaEntity(null, name, "Descricao padrao", new BigDecimal("100.00"),
                10, 5, EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    private void saveTransaction(Long materialId, Long serviceOrderId, int quantity, TransactionType type) {
        transactionRepository.save(new MaterialTransactionJpaEntity(
                null, materialId, serviceOrderId, quantity, type, Instant.now()
        ));
    }
}
