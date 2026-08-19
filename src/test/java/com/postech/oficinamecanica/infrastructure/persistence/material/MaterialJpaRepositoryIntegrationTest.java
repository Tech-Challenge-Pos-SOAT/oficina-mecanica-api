package com.postech.oficinamecanica.infrastructure.persistence.material;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.docker.compose.enabled=false"
})
@Testcontainers
class MaterialJpaRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MaterialJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldReturnOnlyActiveMaterialsOrderedByIdAscending() {
        var m1 = repository.save(aMaterial("Filtro de Óleo", EntityStatus.ACTIVE, 15, 5, "45.00"));
        repository.save(aMaterial("Bateria 60Ah", EntityStatus.INACTIVE, 10, 5, "300.00"));
        var m3 = repository.save(aMaterial("Pneu Aro 15", EntityStatus.ACTIVE, 20, 4, "250.00"));

        List<MaterialJpaEntity> result = repository.findByStatusOrderById(EntityStatus.ACTIVE);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MaterialJpaEntity::getId)
                .containsExactly(m1.getId(), m3.getId()); // Validação pela ordem de inserção
        assertThat(result).extracting(MaterialJpaEntity::getStatus)
                .containsOnly(EntityStatus.ACTIVE);
    }

    @Test
    void shouldReturnOnlyInactiveMaterialsWhenFilteringByInactive() {
        repository.save(aMaterial("Vela", EntityStatus.ACTIVE, 40, 10, "189.90"));
        var inactive = repository.save(aMaterial("Bateria 60Ah", EntityStatus.INACTIVE, 5, 2, "400.00"));

        List<MaterialJpaEntity> result = repository.findByStatusOrderById(EntityStatus.INACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(inactive.getId());
        assertThat(result.get(0).getName()).isEqualTo("Bateria 60Ah");
    }

    @Test
    void shouldMapMonetaryAndStockColumnsWithoutPrecisionLoss() {
        var saved = repository.save(aMaterial("Vela de Ignição", EntityStatus.ACTIVE, 40, 10, "189.90"));

        var result = repository.findById(saved.getId()).orElseThrow();

        assertThat(result.getPrice()).isEqualByComparingTo("189.90");
        assertThat(result.getStockQuantity()).isEqualTo(40);
        assertThat(result.getStockMinimum()).isEqualTo(10);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldAcceptNullDescription() {
        var material = aMaterial("Correia Dentada", EntityStatus.ACTIVE, 10, 5, "120.00");
        material.setDescription(null);
        var saved = repository.save(material);

        var result = repository.findById(saved.getId()).orElseThrow();

        assertThat(result.getName()).isEqualTo("Correia Dentada");
        assertThat(result.getDescription()).isNull();
    }

    @Test
    void shouldReturnOnlyActiveMaterialsWithStockStrictlyBelowMinimum() {
        var lowStockActive = repository.save(aMaterial("Vela de Ignição", EntityStatus.ACTIVE, 5, 10, "189.90"));
        repository.save(aMaterial("Filtro de Ar", EntityStatus.ACTIVE, 20, 10, "40.00")); // Estoque normal
        repository.save(aMaterial("Pastilha de Freio", EntityStatus.INACTIVE, 2, 10, "150.00")); // Estoque baixo, mas inativo

        List<MaterialJpaEntity> result = repository.findLowStockByStatusOrderById(EntityStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(lowStockActive.getId());
    }

    @Test
    void shouldReturnInactiveMaterialsWithLowStock() {
        repository.save(aMaterial("Filtro de Ar", EntityStatus.INACTIVE, 20, 10, "40.00")); // Inativo, estoque normal
        var lowStockInactive = repository.save(aMaterial("Pastilha de Freio", EntityStatus.INACTIVE, 2, 10, "150.00")); // Inativo, estoque baixo
        repository.save(aMaterial("Vela", EntityStatus.ACTIVE, 2, 10, "50.00")); // Ativo, estoque baixo

        List<MaterialJpaEntity> result = repository.findLowStockByStatusOrderById(EntityStatus.INACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(lowStockInactive.getId());
    }

    @Test
    void shouldFindById() {
        var saved = repository.save(aMaterial("Óleo Motor 5W30 Sintético", EntityStatus.ACTIVE, 10, 5, "55.00"));

        var material = repository.findById(saved.getId());

        assertThat(material).isPresent();
        assertThat(material.get().getName()).isEqualTo("Óleo Motor 5W30 Sintético");
    }

    @Test
    void shouldSaveAndMergeUpdatedMaterial() {
        var saved = repository.save(aMaterial("Amortecedor", EntityStatus.ACTIVE, 4, 2, "350.00"));

        saved.setStatus(EntityStatus.INACTIVE);
        repository.save(saved);

        var fetchAgain = repository.findById(saved.getId()).orElseThrow();
        assertThat(fetchAgain.getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    private MaterialJpaEntity aMaterial(String name, EntityStatus status, int stock, int minStock, String price) {
        MaterialJpaEntity entity = new MaterialJpaEntity();
        entity.setName(name);
        entity.setDescription("Descrição padrão gerada no teste");
        entity.setStatus(status);
        entity.setStockQuantity(stock);
        entity.setStockMinimum(minStock);
        entity.setPrice(new BigDecimal(price));
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}