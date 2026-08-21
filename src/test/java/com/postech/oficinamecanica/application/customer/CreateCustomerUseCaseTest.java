package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.customer.DuplicateDocumentException;
import com.postech.oficinamecanica.domain.customer.DuplicateEmailException;
import com.postech.oficinamecanica.domain.customer.InvalidDocumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCustomerUseCaseTest {
    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private CreateCustomerUseCase useCase;

    @Test
    void shouldCreateCustomerWhenDocumentAndEmailAreUnique() {
        CreateCustomerCommand cmd = new CreateCustomerCommand(
            "529.982.247-25", "Maria Souza", "11987654321", "maria@email.com"
        );
        when(repository.findByDocument(any())).thenReturn(Optional.empty());
        when(repository.findByEmail("maria@email.com")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = useCase.execute(cmd);

        assertThat(result.getName()).isEqualTo("Maria Souza");
        verify(repository).save(any());
    }

    @Test
    void shouldFailWhenDocumentAlreadyExists() {
        CreateCustomerCommand cmd = new CreateCustomerCommand(
            "529.982.247-25", "Maria Souza", "11987654321", "maria@email.com"
        );
        Customer existing = Customer.create(
            new Document("529.982.247-25"), "Outro Nome", "11912345678", "outro@email.com"
        );
        when(repository.findByDocument(any())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(DuplicateDocumentException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldFailWhenSameDocumentSentWithDifferentFormatting() {
        CreateCustomerCommand cmd = new CreateCustomerCommand(
            "12345678909", "Maria Souza", "11987654321", "maria@email.com"
        );
        Customer existing = Customer.create(
            new Document("123.456.789-09"), "Outro Nome", "11912345678", "outro@email.com"
        );
        when(repository.findByDocument(new Document("12345678909"))).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(DuplicateDocumentException.class);
    }

    @Test
    void shouldFailWhenEmailAlreadyExists() {
        CreateCustomerCommand cmd = new CreateCustomerCommand(
            "529.982.247-25", "Maria Souza", "11987654321", "maria@email.com"
        );
        Customer existing = Customer.create(
            new Document("123.456.789-09"), "Outro Nome", "11912345678", "maria@email.com"
        );
        when(repository.findByDocument(any())).thenReturn(Optional.empty());
        when(repository.findByEmail("maria@email.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(DuplicateEmailException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldFailWhenDocumentFormatIsInvalid() {
        CreateCustomerCommand cmd = new CreateCustomerCommand(
            "123", "Maria Souza", "11987654321", "maria@email.com"
        );

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(InvalidDocumentException.class);
        verify(repository, never()).save(any());
    }
}
