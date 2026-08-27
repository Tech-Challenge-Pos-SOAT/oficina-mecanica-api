package com.postech.oficinamecanica.infrastructure.persistence.employee;

import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestPropertySource(properties = "spring.docker.compose.enabled=false")
@Import({EmployeeRepositoryImpl.class, EmployeePersistenceMapperImpl.class})
@Testcontainers
class EmployeeRepositoryImplTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private EmployeeRepositoryImpl repository;

    @Test
    void shouldPersistAndFindEmployeeById() {
        Employee saved = repository.save(anEmployee("Julia Ramos", "julia.ramos@oficina.com"));

        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void shouldFindEmployeeByEmail() {
        repository.save(anEmployee("Maria Lima", "maria.lima@oficina.com"));

        assertThat(repository.findByEmail("maria.lima@oficina.com")).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        assertThat(repository.findByEmail("inexistente@oficina.com")).isEmpty();
    }

    @Test
    void shouldListAllEmployees() {
        repository.save(anEmployee("Pedro Alves", "pedro.alves@oficina.com"));

        assertThat(repository.findAll()).isNotEmpty();
    }

    private static Employee anEmployee(String name, String email) {
        return new Employee(
                null, name, email, "hashed-password", EmployeeRole.MECHANIC,
                EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );
    }
}