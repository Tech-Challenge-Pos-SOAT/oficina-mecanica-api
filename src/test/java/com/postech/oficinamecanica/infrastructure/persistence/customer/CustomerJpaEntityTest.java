package com.postech.oficinamecanica.infrastructure.persistence.customer;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerJpaEntityTest {

    @Test
    void shouldExposeFieldsViaGettersAndSetters() {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        Instant now = Instant.now();

        entity.setId(1L);
        entity.setName("Maria Souza");
        entity.setDocument("529.982.247-25");
        entity.setPhone("11987654321");
        entity.setEmail("maria@email.com");
        entity.setStatus(EntityStatus.ACTIVE);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getName()).isEqualTo("Maria Souza");
        assertThat(entity.getDocument()).isEqualTo("529.982.247-25");
        assertThat(entity.getPhone()).isEqualTo("11987654321");
        assertThat(entity.getEmail()).isEqualTo("maria@email.com");
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }
}
