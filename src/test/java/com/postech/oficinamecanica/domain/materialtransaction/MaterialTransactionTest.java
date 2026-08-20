package com.postech.oficinamecanica.domain.materialtransaction;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import java.time.Instant;

class MaterialTransactionTest {
    @Test
    void shouldCreateTransactionWithValidData() {
        MaterialTransaction tx = new MaterialTransaction(
            1L, 10L, 5L, 100, TransactionType.OUT, Instant.now()
        );
        assertThat(tx.getId()).isEqualTo(1L);
        assertThat(tx.getMaterialId()).isEqualTo(10L);
        assertThat(tx.getQuantity()).isEqualTo(100);
        assertThat(tx.getType()).isEqualTo(TransactionType.OUT);
    }

    @Test
    void shouldRejectNullMaterialId() {
        assertThatThrownBy(() -> new MaterialTransaction(
            1L, null, 5L, 100, TransactionType.IN, Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThatThrownBy(() -> new MaterialTransaction(
            1L, 10L, 5L, -1, TransactionType.OUT, Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullType() {
        assertThatThrownBy(() -> new MaterialTransaction(
            1L, 10L, 5L, 100, null, Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
