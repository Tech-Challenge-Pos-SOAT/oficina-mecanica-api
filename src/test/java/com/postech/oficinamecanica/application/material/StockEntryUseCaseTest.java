package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.application.materialtransaction.MaterialTransactionRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockEntryUseCaseTest {
    @Mock private MaterialRepository materialRepository;
    @Mock private MaterialTransactionRepository transactionRepository;
    @InjectMocks private StockEntryUseCase useCase;

    @Test
    void shouldSuccessfullyAddStockAndCreateTransaction() {
        Material existing = new Material(
                1L, "Spark Plug", "NGK Premium", BigDecimal.TEN,
                5, 2, EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(materialRepository.findById(1L)).thenReturn(Optional.of(existing));

        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(MaterialTransaction.class))).thenAnswer(i -> i.getArgument(0));

        Material result = useCase.execute(1L, 10);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStockQuantity()).isEqualTo(15);

        verify(materialRepository).findById(1L);
        verify(materialRepository).save(any(Material.class));
        verify(transactionRepository).save(any(MaterialTransaction.class));
    }

    @Test
    void shouldFailWhenMaterialNotFound() {
        when(materialRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(1L, 10))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(materialRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldInterruptFlowWhenTransactionFails() {
        Material existing = new Material(
                1L, "Spark Plug", "NGK Premium", BigDecimal.TEN,
                5, 2, EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );

        when(materialRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));

        when(transactionRepository.save(any()))
                .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> useCase.execute(1L, 10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(materialRepository).findById(1L);
        verify(materialRepository).save(any(Material.class));
        verify(transactionRepository).save(any());
    }
}
