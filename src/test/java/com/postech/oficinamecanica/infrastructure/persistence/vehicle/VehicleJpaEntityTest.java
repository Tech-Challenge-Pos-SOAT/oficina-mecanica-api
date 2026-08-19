package com.postech.oficinamecanica.infrastructure.persistence.vehicle;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleJpaEntityTest {

    @Test
    void shouldExposeFieldsViaGettersAndSetters() {
        VehicleJpaEntity entity = new VehicleJpaEntity();
        Instant now = Instant.now();

        entity.setId(1L);
        entity.setCustomerId(2L);
        entity.setBrand("Toyota");
        entity.setModel("Corolla");
        entity.setPlate("ABC-1234");
        entity.setYear(2021);
        entity.setStatus(EntityStatus.ACTIVE);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getCustomerId()).isEqualTo(2L);
        assertThat(entity.getBrand()).isEqualTo("Toyota");
        assertThat(entity.getModel()).isEqualTo("Corolla");
        assertThat(entity.getPlate()).isEqualTo("ABC-1234");
        assertThat(entity.getYear()).isEqualTo(2021);
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }
}
