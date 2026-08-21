package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCustomerUseCaseTest {
    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private GetCustomerUseCase useCase;

    @Test
    void shouldReturnCustomerWhenFound() {
        Customer customer = new Customer(
            1L, new Document("529.982.247-25"), "Maria Souza", "11987654321", "maria@email.com",
            EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(repository.findById(1L)).thenReturn(Optional.of(customer));

        Customer result = useCase.execute(1L);

        assertThat(result.getName()).isEqualTo("Maria Souza");
    }

    @Test
    void shouldFailWhenCustomerNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
            .isInstanceOf(CustomerNotFoundException.class);
    }
}
