package com.postech.oficinamecanica.infrastructure.persistence.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
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
@Import({CustomerRepositoryImpl.class, CustomerPersistenceMapperImpl.class})
@Testcontainers
class CustomerRepositoryImplTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CustomerRepositoryImpl repository;

    @Test
    void shouldPersistAndFindCustomerByDocument() {
        repository.save(aCustomer("998.877.665-93", "maria@email.com"));

        assertThat(repository.findByDocument(new Document("998.877.665-93"))).isPresent();
    }

    @Test
    void shouldFindCustomerByEmail() {
        repository.save(aCustomer("135.792.468-28", "joao@email.com"));

        assertThat(repository.findByEmail("joao@email.com")).isPresent();
    }

    @Test
    void shouldFindCustomerById() {
        Customer saved = repository.save(aCustomer("246.813.579-28", "pedro@email.com"));

        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenDocumentNotFound() {
        assertThat(repository.findByDocument(new Document("112.233.445-17"))).isEmpty();
    }

    @Test
    void shouldFindCustomersByStatus() {
        repository.save(aCustomer("147.258.369-82", "carlos@email.com"));

        assertThat(repository.findByStatus(EntityStatus.ACTIVE)).isNotEmpty();
    }

    private static Customer aCustomer(String document, String email) {
        return new Customer(
            null, new Document(document), "Nome Teste", "11987654321", email,
            EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );
    }
}
