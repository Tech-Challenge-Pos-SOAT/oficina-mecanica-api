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

        assertThat(result).hasSize(6);
        assertThat(result).extracting(MaterialJpaEntity::getId).containsExactly(1L, 2L, 3L, 4L, 6L, 7L);
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
        assertThat(result.get(0).getId()).isEqualTo(5L);
    }
}
