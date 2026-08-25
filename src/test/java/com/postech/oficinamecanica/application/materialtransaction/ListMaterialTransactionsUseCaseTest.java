package com.postech.oficinamecanica.application.materialtransaction;

import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ListMaterialTransactionsUseCaseTest {
    @Mock private MaterialTransactionRepository repository;
    @InjectMocks private ListMaterialTransactionsUseCase useCase;

    @Test
    void shouldFetchAllTransactionsWhenTypeIsNull() {
        List<MaterialTransaction> expected = List.of(
            new MaterialTransaction(1L, 10L, null, 100, TransactionType.IN, Instant.now()),
            new MaterialTransaction(2L, 20L, 5L, 50, TransactionType.OUT, Instant.now())
        );
        when(repository.findAll(null)).thenReturn(expected);

        List<MaterialTransaction> result = useCase.execute(null);

        assertThat(result).isEqualTo(expected);
        verify(repository).findAll(null);
    }

    @Test
    void shouldFetchOutTransactionsWhenTypeIsOut() {
        List<MaterialTransaction> expected = List.of(
            new MaterialTransaction(2L, 20L, 5L, 50, TransactionType.OUT, Instant.now())
        );
        when(repository.findAll(TransactionType.OUT)).thenReturn(expected);

        List<MaterialTransaction> result = useCase.execute(TransactionType.OUT);

        assertThat(result).isEqualTo(expected);
        verify(repository).findAll(TransactionType.OUT);
    }
}
