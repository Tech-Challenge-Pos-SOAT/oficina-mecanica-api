package com.postech.oficinamecanica.interfaces.rest.materialtransaction;

import com.postech.oficinamecanica.application.auth.TokenProvider;
import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeRole;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.math.BigDecimal;
import java.time.Instant;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MaterialTransactionControllerIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private MockMvc mockMvc;
    @Autowired private MaterialTransactionJpaRepository jpaRepository;
    @Autowired private MaterialJpaRepository materialRepository;
    @Autowired private TokenProvider tokenProvider;

    private Long materialId1;
    private Long materialId2;
    private Long materialId3;
    private String authHeader;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
        materialRepository.deleteAll();
        materialId1 = materialRepository.save(new MaterialJpaEntity(
            null, "Material 1", "Desc 1", BigDecimal.TEN, 100, 10, EntityStatus.ACTIVE, Instant.now(), Instant.now()
        )).getId();
        materialId2 = materialRepository.save(new MaterialJpaEntity(
            null, "Material 2", "Desc 2", BigDecimal.TEN, 100, 10, EntityStatus.ACTIVE, Instant.now(), Instant.now()
        )).getId();
        materialId3 = materialRepository.save(new MaterialJpaEntity(
            null, "Material 3", "Desc 3", BigDecimal.TEN, 100, 10, EntityStatus.ACTIVE, Instant.now(), Instant.now()
        )).getId();
        authHeader = "Bearer " + tokenProvider.generateToken(anAuthenticatedEmployee());
    }

    private Employee anAuthenticatedEmployee() {
        return new Employee(
                1L, "Test User", "test.user@oficina.com", "hashed-password",
                EmployeeRole.ATTENDANT, EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );
    }

    @Test
    void shouldListAllTransactionsFromEndpoint() throws Exception {
        jpaRepository.save(new MaterialTransactionJpaEntity(
            null, materialId1, null, 100, TransactionType.IN, Instant.now()
        ));
        jpaRepository.save(new MaterialTransactionJpaEntity(
            null, materialId2, null, 50, TransactionType.OUT, Instant.now()
        ));

        mockMvc.perform(get("/api/material-transactions").header("Authorization", authHeader))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].materialId").value(materialId1))
            .andExpect(jsonPath("$[1].materialId").value(materialId2));
    }

    @Test
    void shouldFilterTransactionsByType() throws Exception {
        jpaRepository.save(new MaterialTransactionJpaEntity(
            null, materialId1, null, 100, TransactionType.IN, Instant.now()
        ));
        jpaRepository.save(new MaterialTransactionJpaEntity(
            null, materialId2, null, 50, TransactionType.OUT, Instant.now()
        ));

        mockMvc.perform(get("/api/material-transactions?type=OUT").header("Authorization", authHeader))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].type").value("OUT"));
    }

    @Test
    void shouldOrderByIdAscending() throws Exception {
        jpaRepository.save(new MaterialTransactionJpaEntity(
            null, materialId3, null, 200, TransactionType.IN, Instant.now()
        ));
        jpaRepository.save(new MaterialTransactionJpaEntity(
            null, materialId1, null, 100, TransactionType.IN, Instant.now()
        ));
        jpaRepository.save(new MaterialTransactionJpaEntity(
            null, materialId2, null, 50, TransactionType.OUT, Instant.now()
        ));

        mockMvc.perform(get("/api/material-transactions").header("Authorization", authHeader))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[2].id").value(3));
    }

    @Test
    void shouldReturnBadRequestOnInvalidType() throws Exception {
        mockMvc.perform(get("/api/material-transactions?type=INVALID").header("Authorization", authHeader))
            .andExpect(status().isBadRequest());
    }
}
