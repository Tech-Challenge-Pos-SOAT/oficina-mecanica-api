package com.postech.oficinamecanica.infrastructure.persistence.material;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MaterialJpaEntityTest {

    @Test
    void shouldCreateEntityWithValidFields() {
        MaterialJpaEntity entity = new MaterialJpaEntity(
                1L, "Test Material", "Desc", new BigDecimal("10.00"), 5, 1, EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );

        assertThat(entity.getName()).isEqualTo("Test Material");
        assertThat(entity.getPrice()).isEqualByComparingTo("10.00");
    }
}
