package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.customer.InvalidDocumentException;
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
class GetCustomerByDocumentUseCaseTest {
    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private GetCustomerByDocumentUseCase useCase;

    @Test
    void shouldReturnCustomerWhenDocumentFound() {
        Customer customer = new Customer(
            1L, new Document("529.982.247-25"), "Maria Souza", "11987654321", "maria@email.com",
            EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(repository.findByDocument(new Document("529.982.247-25"))).thenReturn(Optional.of(customer));

        Customer result = useCase.execute("529.982.247-25");

        assertThat(result.getName()).isEqualTo("Maria Souza");
    }

    @Test
    void shouldTreatDifferentlyFormattedDocumentAsSameLookup() {
        Customer customer = new Customer(
            1L, new Document("529.982.247-25"), "Maria Souza", "11987654321", "maria@email.com",
            EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(repository.findByDocument(new Document("52998224725"))).thenReturn(Optional.of(customer));

        Customer result = useCase.execute("529.982.247-25");

        assertThat(result.getName()).isEqualTo("Maria Souza");
    }

    @Test
    void shouldFailWhenDocumentNotFound() {
        when(repository.findByDocument(new Document("529.982.247-25"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("529.982.247-25"))
            .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void shouldFailWhenDocumentFormatIsInvalid() {
        assertThatThrownBy(() -> useCase.execute("123"))
            .isInstanceOf(InvalidDocumentException.class);
    }
}
