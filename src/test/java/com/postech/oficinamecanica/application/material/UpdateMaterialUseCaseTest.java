package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.shared.exceptions.BusinessRuleViolationException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateMaterialUseCaseTest {

    @Mock
    private MaterialRepository repository;

    @InjectMocks
    private UpdateMaterialUseCase useCase;

    @Test
    void shouldUpdateMaterialSuccessfully() {
        Long materialId = 1L;
        Material existing = new Material(materialId, "Old Name", "Old Desc", BigDecimal.TEN, 10, 2, EntityStatus.ACTIVE, Instant.now(), Instant.now());
        when(repository.findById(materialId)).thenReturn(Optional.of(existing));
        when(repository.existsByName("New Name")).thenReturn(false);
        when(repository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Material result = useCase.execute(materialId, "New Name", "New Desc", new BigDecimal("50.00"), 20, 5);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("New Desc");
        assertThat(result.getPrice()).isEqualByComparingTo("50.00");
        assertThat(result.getStockQuantity()).isEqualTo(20);
        assertThat(result.getStockMinimum()).isEqualTo(5);
        verify(repository).save(any(Material.class));
    }

    @Test
    void shouldAllowSameMaterialNameWhenUpdatingOwnName() {
        Long materialId = 1L;
        Material existing = new Material(materialId, "Same Name", "Desc", BigDecimal.TEN, 10, 2, EntityStatus.ACTIVE, Instant.now(), Instant.now());
        when(repository.findById(materialId)).thenReturn(Optional.of(existing));
        when(repository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Material result = useCase.execute(materialId, "Same Name", "New Desc", BigDecimal.TEN, 10, 2);

        assertThat(result.getName()).isEqualTo("Same Name");
        verify(repository).save(any(Material.class));
    }

    @Test
    void shouldThrowExceptionWhenMaterialNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L, "Any", "Desc", BigDecimal.TEN, 10, 2))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Material");
    }

    @Test
    void shouldThrowExceptionWhenNameAlreadyExists() {
        Long materialId = 1L;
        Material existing = new Material(materialId, "Old Name", "Desc", BigDecimal.TEN, 10, 2, EntityStatus.ACTIVE, Instant.now(), Instant.now());
        when(repository.findById(materialId)).thenReturn(Optional.of(existing));
        when(repository.existsByName("Existing Name")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(materialId, "Existing Name", "Desc", BigDecimal.TEN, 10, 2))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Material name already exists");
    }
}
