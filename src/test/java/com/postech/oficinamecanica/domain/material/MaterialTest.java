package com.postech.oficinamecanica.domain.material;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaterialTest {

    private Material material(int stockQuantity, EntityStatus status) {
        return new Material(1L, "Spark Plug", "NGK Premium", BigDecimal.TEN,
                stockQuantity, 2, status, Instant.now(), Instant.now());
    }

    @Test
    void shouldDebitStockAndUpdateTimestamp() {
        Material material = material(10, EntityStatus.ACTIVE);
        Instant before = material.getUpdatedAt();

        material.debitStock(4);

        assertThat(material.getStockQuantity()).isEqualTo(6);
        assertThat(material.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void shouldFailWhenStockInsufficient() {
        Material material = material(3, EntityStatus.ACTIVE);

        assertThatThrownBy(() -> material.debitStock(5))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void shouldFailWhenMaterialInactive() {
        Material material = material(100, EntityStatus.INACTIVE);

        assertThatThrownBy(() -> material.debitStock(1))
                .isInstanceOf(InactiveMaterialException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void shouldFailWhenQuantityIsNull() {
        Material material = material(10, EntityStatus.ACTIVE);

        assertThatThrownBy(() -> material.debitStock(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFailWhenQuantityIsZeroOrNegative() {
        Material material = material(10, EntityStatus.ACTIVE);

        assertThatThrownBy(() -> material.debitStock(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> material.debitStock(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
