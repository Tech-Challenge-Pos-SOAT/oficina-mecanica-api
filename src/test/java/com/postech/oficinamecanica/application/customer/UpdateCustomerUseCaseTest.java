package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.customer.DuplicateEmailException;
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
class UpdateCustomerUseCaseTest {
    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private UpdateCustomerUseCase useCase;

    @Test
    void shouldUpdateCustomerDetailsWhenFound() {
        Customer customer = aCustomer(1L, "529.982.247-25", "Maria Souza", "maria@email.com");
        UpdateCustomerCommand cmd = new UpdateCustomerCommand(1L, "Maria Lima", "11999998888", "maria.lima@email.com");

        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.findByEmail("maria.lima@email.com")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = useCase.execute(cmd);

        assertThat(result.getName()).isEqualTo("Maria Lima");
        assertThat(result.getEmail()).isEqualTo("maria.lima@email.com");
    }

    @Test
    void shouldFailWhenCustomerNotFound() {
        UpdateCustomerCommand cmd = new UpdateCustomerCommand(99L, "Maria", "11999998888", "maria@email.com");
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void shouldFailWhenEmailBelongsToAnotherCustomer() {
        Customer customer = aCustomer(1L, "529.982.247-25", "Maria Souza", "maria@email.com");
        Customer other = aCustomer(2L, "123.456.789-09", "Outra Pessoa", "outra@email.com");
        UpdateCustomerCommand cmd = new UpdateCustomerCommand(1L, "Maria Souza", "11987654321", "outra@email.com");

        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.findByEmail("outra@email.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void shouldAllowKeepingOwnEmailUnchanged() {
        Customer customer = aCustomer(1L, "529.982.247-25", "Maria Souza", "maria@email.com");
        UpdateCustomerCommand cmd = new UpdateCustomerCommand(1L, "Maria Souza", "11987654321", "maria@email.com");

        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.findByEmail("maria@email.com")).thenReturn(Optional.of(customer));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = useCase.execute(cmd);

        assertThat(result.getEmail()).isEqualTo("maria@email.com");
    }

    private static Customer aCustomer(Long id, String document, String name, String email) {
        return new Customer(
            id, new Document(document), name, "11987654321", email,
            EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );
    }
}
