package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.material.MaterialNotFoundException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMaterialUseCaseTest {

    @Mock
    private MaterialRepository repository;

    @InjectMocks
    private GetMaterialUseCase useCase;

    @Test
    void shouldReturnMaterialWhenFoundById() {
        Long id = 1L;
        Material expectedMaterial = new Material(
            id, "Filtro de Óleo", "Filtro Bosch", new BigDecimal("32.50"),
            25, 5, EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(repository.findById(id)).thenReturn(Optional.of(expectedMaterial));

        Material actualMaterial = useCase.execute(id);

        assertThat(actualMaterial).isEqualTo(expectedMaterial);
        verify(repository).findById(id);
    }

    @Test
    void shouldThrowExceptionWhenMaterialNotFound() {
        Long id = 999L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(id))
            .isInstanceOf(MaterialNotFoundException.class)
            .hasMessageContaining("Material não encontrado com o ID: 999");
        verify(repository).findById(id);
    }
}
