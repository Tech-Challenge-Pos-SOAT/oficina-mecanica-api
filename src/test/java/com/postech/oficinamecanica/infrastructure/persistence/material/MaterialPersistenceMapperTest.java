package com.postech.oficinamecanica.infrastructure.persistence.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MaterialPersistenceMapperTest {

    private final MaterialPersistenceMapper mapper = Mappers.getMapper(MaterialPersistenceMapper.class);

    @Test
    void shouldMapEntityToDomainCompletely() {
        Instant now = Instant.now();
        MaterialJpaEntity entity = new MaterialJpaEntity(
                1L, "Filtro", "Desc", new BigDecimal("10.00"), 5, 1, EntityStatus.ACTIVE, now, now
        );

        Material domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getName()).isEqualTo("Filtro");
        assertThat(domain.getDescription()).isEqualTo("Desc");
        assertThat(domain.getPrice()).isEqualByComparingTo("10.00");
        assertThat(domain.getStockQuantity()).isEqualTo(5);
        assertThat(domain.getStockMinimum()).isEqualTo(1);
        assertThat(domain.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(domain.getCreatedAt()).isEqualTo(now);
        assertThat(domain.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldMapDomainToEntityCompletely() {
        Instant now = Instant.now();
        Material domain = new Material(
                2L, "Correia", "Desc Correia", new BigDecimal("45.50"), 10, 2, EntityStatus.INACTIVE, now, now
        );

        MaterialJpaEntity entity = mapper.toPersistence(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getName()).isEqualTo("Correia");
        assertThat(entity.getDescription()).isEqualTo("Desc Correia");
        assertThat(entity.getPrice()).isEqualByComparingTo("45.50");
        assertThat(entity.getStockQuantity()).isEqualTo(10);
        assertThat(entity.getStockMinimum()).isEqualTo(2);
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.INACTIVE);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }
}
