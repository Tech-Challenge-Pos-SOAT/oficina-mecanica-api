package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCustomersUseCaseTest {
    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private ListCustomersUseCase useCase;

    @Test
    void shouldReturnActiveCustomersWhenStatusParamIsNull() {
        Customer customer = new Customer(
            1L,
            new Document("52998224725"),
            "Maria Souza",
            "11987654321",
            "maria@email.com",
            EntityStatus.ACTIVE,
            Instant.now(),
            Instant.now()
        );

        when(repository.findByStatus(EntityStatus.ACTIVE))
            .thenReturn(List.of(customer));

        List<Customer> result = useCase.execute(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Maria Souza");
    }

    @Test
    void shouldReturnActiveCustomersWhenStatusIsExplicitActive() {
        Customer customer = new Customer(
            1L,
            new Document("52998224725"),
            "Maria Souza",
            "11987654321",
            "maria@email.com",
            EntityStatus.ACTIVE,
            Instant.now(),
            Instant.now()
        );

        when(repository.findByStatus(EntityStatus.ACTIVE))
            .thenReturn(List.of(customer));

        List<Customer> result = useCase.execute("active");

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnInactiveCustomersWhenStatusIsInactive() {
        Customer customer = new Customer(
            2L,
            new Document("98765432109"),
            "João Silva",
            "11912345678",
            "joao@email.com",
            EntityStatus.INACTIVE,
            Instant.now(),
            Instant.now()
        );

        when(repository.findByStatus(EntityStatus.INACTIVE))
            .thenReturn(List.of(customer));

        List<Customer> result = useCase.execute("INACTIVE");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    void shouldReturnEmptyListWhenNoCustomersMatchStatus() {
        when(repository.findByStatus(EntityStatus.INACTIVE))
            .thenReturn(List.of());

        List<Customer> result = useCase.execute("INACTIVE");

        assertThat(result).isEmpty();
    }
}
