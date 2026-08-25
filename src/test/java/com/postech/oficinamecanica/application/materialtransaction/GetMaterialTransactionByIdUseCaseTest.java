package com.postech.oficinamecanica.application.materialtransaction;

import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
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
class GetMaterialTransactionByIdUseCaseTest {
    @Mock
    private MaterialTransactionRepository repository;

    @InjectMocks
    private GetMaterialTransactionByIdUseCase useCase;

    @Test
    void shouldReturnTransactionWhenFound() {
        MaterialTransaction transaction = new MaterialTransaction(
                1L, 2L, null, 10, TransactionType.IN, Instant.now()
        );
        when(repository.findById(1L)).thenReturn(Optional.of(transaction));

        MaterialTransaction result = useCase.execute(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMaterialId()).isEqualTo(2L);
        assertThat(result.getQuantity()).isEqualTo(10);
        assertThat(result.getType()).isEqualTo(TransactionType.IN);
    }

    @Test
    void shouldThrowNotFoundWhenTransactionDoesNotExist() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
