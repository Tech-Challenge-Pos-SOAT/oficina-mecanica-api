package com.postech.oficinamecanica.infrastructure.persistence.service;

import com.postech.oficinamecanica.domain.service.Service;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
@Import({ServiceRepositoryImpl.class, ServicePersistenceMapperImpl.class})
class ServiceRepositoryImplTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ServiceRepositoryImpl repository;

    private Service aService(String name) {
        return new Service(null, name, "desc", new BigDecimal("120.00"),
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    @Test
    void shouldSaveAndFindServiceById() {
        Service saved = repository.save(aService("Troca de óleo"));

        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void shouldFindServiceByName() {
        repository.save(aService("Alinhamento"));

        assertThat(repository.findByName("Alinhamento")).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenServiceNameDoesNotExist() {
        assertThat(repository.findByName("Inexistente")).isEmpty();
    }

    @Test
    void shouldListAllServicesOrderedById() {
        repository.save(aService("Balanceamento"));
        repository.save(aService("Revisão completa"));

        assertThat(repository.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }
}
