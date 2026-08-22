package com.postech.oficinamecanica.application.materialtransaction;

import com.postech.oficinamecanica.application.material.MaterialRepository;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListMaterialTransactionsByMaterialUseCaseTest {
    @Mock private MaterialRepository materialRepository;
    @Mock private MaterialTransactionRepository transactionRepository;
    @InjectMocks private ListMaterialTransactionsByMaterialUseCase useCase;

    private Material aMaterial(Long id) {
        return new Material(id, "Oleo 5W30", "Desc", BigDecimal.TEN, 10, 5,
                EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    @Test
    void shouldThrowNotFoundWhenMaterialDoesNotExist() {
        when(materialRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L, null))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldReturnAllTransactionsWhenTypeIsNull() {
        when(materialRepository.findById(1L)).thenReturn(Optional.of(aMaterial(1L)));
        List<MaterialTransaction> expected = List.of(
            new MaterialTransaction(1L, 1L, null, 100, TransactionType.IN, Instant.now()),
            new MaterialTransaction(2L, 1L, 5L, 50, TransactionType.OUT, Instant.now())
        );
        when(transactionRepository.findAllByMaterialId(1L, null)).thenReturn(expected);

        List<MaterialTransaction> result = useCase.execute(1L, null);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldFilterByTypeWhenProvided() {
        when(materialRepository.findById(1L)).thenReturn(Optional.of(aMaterial(1L)));
        List<MaterialTransaction> expected = List.of(
            new MaterialTransaction(2L, 1L, 5L, 50, TransactionType.OUT, Instant.now())
        );
        when(transactionRepository.findAllByMaterialId(1L, TransactionType.OUT)).thenReturn(expected);

        List<MaterialTransaction> result = useCase.execute(1L, TransactionType.OUT);

        assertThat(result).isEqualTo(expected);
    }
}
