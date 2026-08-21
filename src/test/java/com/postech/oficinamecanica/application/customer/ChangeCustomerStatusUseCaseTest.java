package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerAlreadyActiveException;
import com.postech.oficinamecanica.domain.customer.CustomerAlreadyInactiveException;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeCustomerStatusUseCaseTest {
    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private ChangeCustomerStatusUseCase useCase;

    @Test
    void shouldDeactivateActiveCustomer() {
        Customer customer = aCustomer(EntityStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = useCase.execute(new ChangeCustomerStatusCommand(1L, "INACTIVE"));

        assertThat(result.getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    void shouldActivateInactiveCustomer() {
        Customer customer = aCustomer(EntityStatus.INACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = useCase.execute(new ChangeCustomerStatusCommand(1L, "active"));

        assertThat(result.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void shouldFailWhenReactivatingCustomerThatIsAlreadyActive() {
        Customer customer = aCustomer(EntityStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> useCase.execute(new ChangeCustomerStatusCommand(1L, "ACTIVE")))
            .isInstanceOf(CustomerAlreadyActiveException.class);
    }

    @Test
    void shouldFailWhenDeactivatingCustomerThatIsAlreadyInactive() {
        Customer customer = aCustomer(EntityStatus.INACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> useCase.execute(new ChangeCustomerStatusCommand(1L, "INACTIVE")))
            .isInstanceOf(CustomerAlreadyInactiveException.class);
    }

    @Test
    void shouldFailWhenCustomerNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ChangeCustomerStatusCommand(99L, "ACTIVE")))
            .isInstanceOf(CustomerNotFoundException.class);
    }

    private static Customer aCustomer(EntityStatus status) {
        return new Customer(
            1L, new Document("529.982.247-25"), "Maria Souza", "11987654321", "maria@email.com",
            status, Instant.now(), Instant.now()
        );
    }
}
