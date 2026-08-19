package com.postech.oficinamecanica.infrastructure.persistence.employee;

import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeJpaEntityTest {

    @Test
    void shouldExposeFieldsViaGettersAndSetters() {
        EmployeeJpaEntity entity = new EmployeeJpaEntity();
        Instant now = Instant.now();

        entity.setId(1L);
        entity.setName("Carlos Souza");
        entity.setEmail("carlos.souza@oficina.com");
        entity.setPassword("hashed-password");
        entity.setRole(EmployeeRole.MECHANIC);
        entity.setStatus(EntityStatus.ACTIVE);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getName()).isEqualTo("Carlos Souza");
        assertThat(entity.getEmail()).isEqualTo("carlos.souza@oficina.com");
        assertThat(entity.getPassword()).isEqualTo("hashed-password");
        assertThat(entity.getRole()).isEqualTo(EmployeeRole.MECHANIC);
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldBuildViaAllArgsConstructor() {
        Instant now = Instant.now();
        EmployeeJpaEntity entity = new EmployeeJpaEntity(
            1L, "Carlos Souza", "carlos.souza@oficina.com", "hashed-password",
            EmployeeRole.MECHANIC, EntityStatus.ACTIVE, now, now
        );

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getName()).isEqualTo("Carlos Souza");
        assertThat(entity.getEmail()).isEqualTo("carlos.souza@oficina.com");
    }
}
