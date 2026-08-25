package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeMaterialStatusUseCaseTest {

    @Mock
    private MaterialRepository repository;

    @InjectMocks
    private ChangeMaterialStatusUseCase useCase;

    @Test
    void shouldUpdateStatusAndSave() {
        Material material = aMaterial(1L, EntityStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(material));
        when(repository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Material result = useCase.execute(1L, "INACTIVE");

        assertThat(result.getStatus()).isEqualTo(EntityStatus.INACTIVE);
        verify(repository).save(material);
    }

    @Test
    void shouldThrowExceptionWhenMaterialNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, "INACTIVE"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Material com identificador '99' não foi encontrado.");
    }

    @Test
    void shouldThrowExceptionWhenStatusIsInvalid() {
        assertThatThrownBy(() -> useCase.execute(1L, "INVALID"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static Material aMaterial(Long id, EntityStatus status) {
        return new Material(id, "Peca Teste", null, BigDecimal.TEN, 10, 2, status, Instant.now(), Instant.now());
    }
}
