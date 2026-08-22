package com.postech.oficinamecanica.infrastructure.persistence.vehicle;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
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
@Import({VehicleRepositoryImpl.class, VehiclePersistenceMapperImpl.class})
@Testcontainers
class VehicleRepositoryImplTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private VehicleRepositoryImpl repository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private Long persistCustomer() {
        jdbcTemplate.update(
            "INSERT INTO customer (name, document, phone, email, status) VALUES (?, ?, ?, ?, ?)",
            "Nome Teste", "998.877.665-93" + System.nanoTime() % 1000, "11987654321",
            "teste" + System.nanoTime() + "@email.com", "ACTIVE"
        );
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM customer", Long.class);
    }

    @Test
    void shouldPersistAndFindVehicleByPlate() {
        Long customerId = persistCustomer();
        repository.save(aVehicle(customerId, "ABC-1234"));

        assertThat(repository.findByPlate(new Plate("ABC-1234"))).isPresent();
    }

    @Test
    void shouldFindVehicleById() {
        Long customerId = persistCustomer();
        Vehicle saved = repository.save(aVehicle(customerId, "XYZ-9876"));

        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenPlateNotFound() {
        assertThat(repository.findByPlate(new Plate("QQQ-0000"))).isEmpty();
    }

    @Test
    void shouldFindVehiclesByStatus() {
        Long customerId = persistCustomer();
        repository.save(aVehicle(customerId, "AAA-1111"));

        assertThat(repository.findByStatus(EntityStatus.ACTIVE)).isNotEmpty();
    }

    private static Vehicle aVehicle(Long customerId, String plate) {
        return new Vehicle(null, customerId, new Plate(plate), "Toyota", "Corolla", 2021,
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }
}
