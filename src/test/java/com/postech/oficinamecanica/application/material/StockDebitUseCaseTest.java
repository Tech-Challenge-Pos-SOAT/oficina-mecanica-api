package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.application.materialtransaction.MaterialTransactionRepository;
import com.postech.oficinamecanica.domain.material.InactiveMaterialException;
import com.postech.oficinamecanica.domain.material.InsufficientStockException;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class StockDebitUseCaseTest {
    @Mock private MaterialRepository materialRepository;
    @Mock private MaterialTransactionRepository transactionRepository;
    @InjectMocks private StockDebitUseCase useCase;

    private Material activeMaterial(int stockQuantity) {
        return new Material(1L, "Spark Plug", "NGK Premium", BigDecimal.TEN,
                stockQuantity, 2, EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    @Test
    void shouldDebitStockAndCreateOutTransaction() {
        when(materialRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(activeMaterial(10)));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(MaterialTransaction.class)))
                .thenAnswer(i -> i.getArgument(0));

        Material result = useCase.execute(1L, 55L, 3);

        assertThat(result.getStockQuantity()).isEqualTo(7);

        ArgumentCaptor<MaterialTransaction> captor = ArgumentCaptor.forClass(MaterialTransaction.class);
        verify(transactionRepository).save(captor.capture());
        MaterialTransaction saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(TransactionType.OUT);
        assertThat(saved.getMaterialId()).isEqualTo(1L);
        assertThat(saved.getServiceOrderId()).isEqualTo(55L);
        assertThat(saved.getQuantity()).isEqualTo(3);
    }

    @Test
    void shouldFailWhenMaterialNotFound() {
        when(materialRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(1L, 55L, 3))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(materialRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenStockInsufficient() {
        when(materialRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(activeMaterial(2)));

        assertThatThrownBy(() -> useCase.execute(1L, 55L, 3))
                .isInstanceOf(InsufficientStockException.class);

        verify(materialRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenMaterialInactive() {
        Material inactive = new Material(1L, "Spark Plug", "NGK Premium", BigDecimal.TEN,
                50, 2, EntityStatus.INACTIVE, Instant.now(), Instant.now());
        when(materialRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> useCase.execute(1L, 55L, 3))
                .isInstanceOf(InactiveMaterialException.class);

        verify(materialRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenQuantityInvalid() {
        when(materialRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(activeMaterial(10)));

        assertThatThrownBy(() -> useCase.execute(1L, 55L, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.execute(1L, 55L, null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenServiceOrderIdIsNull() {
        when(materialRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(activeMaterial(10)));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> useCase.execute(1L, null, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("serviceOrderId");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldPropagateErrorWhenTransactionSaveFails() {
        when(materialRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(activeMaterial(10)));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> useCase.execute(1L, 55L, 3))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(transactionRepository).save(any());
    }
}
