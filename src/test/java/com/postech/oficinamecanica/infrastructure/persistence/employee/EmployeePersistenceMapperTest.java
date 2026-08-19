package com.postech.oficinamecanica.infrastructure.persistence.employee;

import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeePersistenceMapperTest {
    private final EmployeePersistenceMapper mapper = new EmployeePersistenceMapperImpl();

    @Test
    void shouldMapEntityToDomain() {
        Instant now = Instant.now();
        EmployeeJpaEntity entity = new EmployeeJpaEntity(
            1L, "Carlos Souza", "carlos.souza@oficina.com", "hashed-password",
            EmployeeRole.MECHANIC, EntityStatus.ACTIVE, now, now
        );

        Employee domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getName()).isEqualTo("Carlos Souza");
        assertThat(domain.getEmail()).isEqualTo("carlos.souza@oficina.com");
        assertThat(domain.getPassword()).isEqualTo("hashed-password");
        assertThat(domain.getRole()).isEqualTo(EmployeeRole.MECHANIC);
        assertThat(domain.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void shouldMapDomainToEntity() {
        Instant now = Instant.now();
        Employee domain = new Employee(
            1L, "Carlos Souza", "carlos.souza@oficina.com", "hashed-password",
            EmployeeRole.MECHANIC, EntityStatus.ACTIVE, now, now
        );

        EmployeeJpaEntity entity = mapper.toPersistence(domain);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getName()).isEqualTo("Carlos Souza");
        assertThat(entity.getEmail()).isEqualTo("carlos.souza@oficina.com");
        assertThat(entity.getPassword()).isEqualTo("hashed-password");
        assertThat(entity.getRole()).isEqualTo(EmployeeRole.MECHANIC);
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }
}
