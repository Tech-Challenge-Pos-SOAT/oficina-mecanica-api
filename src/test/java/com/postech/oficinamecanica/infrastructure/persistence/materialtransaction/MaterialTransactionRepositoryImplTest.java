package com.postech.oficinamecanica.infrastructure.persistence.materialtransaction;

import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.infrastructure.persistence.material.MaterialJpaEntity;
import com.postech.oficinamecanica.infrastructure.persistence.material.MaterialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import java.time.Instant;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class MaterialTransactionRepositoryImplTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private MaterialTransactionJpaRepository jpaRepository;
    @Autowired private MaterialJpaRepository materialRepository;

    private MaterialTransactionRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        materialRepository.save(new MaterialJpaEntity(
            null, "Material 1", "Desc 1", BigDecimal.TEN, 100, 10, EntityStatus.ACTIVE, Instant.now(), Instant.now()
        ));
        materialRepository.save(new MaterialJpaEntity(
            null, "Material 2", "Desc 2", BigDecimal.TEN, 100, 10, EntityStatus.ACTIVE, Instant.now(), Instant.now()
        ));
        materialRepository.save(new MaterialJpaEntity(
            null, "Material 3", "Desc 3", BigDecimal.TEN, 100, 10, EntityStatus.ACTIVE, Instant.now(), Instant.now()
        ));

        MaterialTransactionPersistenceMapper mapper = new MaterialTransactionPersistenceMapperImpl();
        repository = new MaterialTransactionRepositoryImpl(jpaRepository, mapper);
    }

    @Test
    void shouldPersistAndFindTransactionByType() {
        MaterialTransactionJpaEntity entity = new MaterialTransactionJpaEntity(
            null, 1L, null, 100, TransactionType.OUT, Instant.now()
        );
        jpaRepository.save(entity);

        var result = repository.findAll(TransactionType.OUT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(TransactionType.OUT);
    }

    @Test
    void shouldOrderByIdAscending() {
        jpaRepository.save(new MaterialTransactionJpaEntity(null, 1L, null, 100, TransactionType.IN, Instant.now()));
        jpaRepository.save(new MaterialTransactionJpaEntity(null, 2L, null, 50, TransactionType.OUT, Instant.now()));
        jpaRepository.save(new MaterialTransactionJpaEntity(null, 3L, null, 200, TransactionType.IN, Instant.now()));

        var result = repository.findAll(null);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isLessThan(result.get(1).getId());
        assertThat(result.get(1).getId()).isLessThan(result.get(2).getId());
    }

    @Test
    void shouldFindOnlyTransactionsOfGivenMaterialOrderedByIdAsc() {
        jpaRepository.save(new MaterialTransactionJpaEntity(null, 1L, null, 100, TransactionType.IN, Instant.now()));
        jpaRepository.save(new MaterialTransactionJpaEntity(null, 2L, null, 50, TransactionType.OUT, Instant.now()));
        jpaRepository.save(new MaterialTransactionJpaEntity(null, 1L, null, 20, TransactionType.OUT, Instant.now()));

        var result = repository.findAllByMaterialId(1L, null);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(tx -> assertThat(tx.getMaterialId()).isEqualTo(1L));
        assertThat(result.get(0).getId()).isLessThan(result.get(1).getId());
    }

    @Test
    void shouldFindMaterialTransactionsFilteredByType() {
        jpaRepository.save(new MaterialTransactionJpaEntity(null, 1L, null, 100, TransactionType.IN, Instant.now()));
        jpaRepository.save(new MaterialTransactionJpaEntity(null, 1L, null, 50, TransactionType.OUT, Instant.now()));

        var result = repository.findAllByMaterialId(1L, TransactionType.OUT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(TransactionType.OUT);
        assertThat(result.get(0).getServiceOrderId()).isNull();
    }
}
