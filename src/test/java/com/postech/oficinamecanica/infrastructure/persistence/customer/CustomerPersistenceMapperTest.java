package com.postech.oficinamecanica.infrastructure.persistence.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerPersistenceMapperTest {
    private final CustomerPersistenceMapper mapper = new CustomerPersistenceMapperImpl();

    @Test
    void shouldMapEntityToDomain() {
        CustomerJpaEntity entity = new CustomerJpaEntity(
            1L, "Maria Souza", "529.982.247-25", "11987654321", "maria@email.com",
            EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );

        Customer domain = mapper.toDomain(entity);

        assertThat(domain.getDocument()).isEqualTo(new Document("529.982.247-25"));
        assertThat(domain.getName()).isEqualTo("Maria Souza");
    }

    @Test
    void shouldMapDomainToEntity() {
        Customer domain = new Customer(
            1L, new Document("529.982.247-25"), "Maria Souza", "11987654321", "maria@email.com",
            EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );

        CustomerJpaEntity entity = mapper.toPersistence(domain);

        assertThat(entity.getDocument()).isEqualTo("529.982.247-25");
        assertThat(entity.getName()).isEqualTo("Maria Souza");
    }

    @Test
    void shouldReturnNullWhenMappingNullDocumentValue() {
        assertThat(mapper.map((String) null)).isNull();
        assertThat(mapper.map((Document) null)).isNull();
    }
}
