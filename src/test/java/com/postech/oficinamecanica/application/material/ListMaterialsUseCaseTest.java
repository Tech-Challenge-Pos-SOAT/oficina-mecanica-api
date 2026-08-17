package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListMaterialsUseCaseTest {
    @Mock
    private MaterialRepository repository;

    @InjectMocks
    private ListMaterialsUseCase useCase;

    @Test
    void shouldReturnActiveMaterialsWhenStatusParamIsNull() {
        when(repository.findByStatus(EntityStatus.ACTIVE))
            .thenReturn(List.of(aMaterial(1L, "Filtro de Oleo", EntityStatus.ACTIVE)));

        List<Material> result = useCase.execute(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Filtro de Oleo");
    }

    @Test
    void shouldReturnActiveMaterialsWhenStatusParamIsBlank() {
        when(repository.findByStatus(EntityStatus.ACTIVE))
            .thenReturn(List.of(aMaterial(1L, "Filtro de Oleo", EntityStatus.ACTIVE)));

        List<Material> result = useCase.execute("   ");

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnActiveMaterialsWhenStatusIsLowercase() {
        when(repository.findByStatus(EntityStatus.ACTIVE))
            .thenReturn(List.of(aMaterial(1L, "Filtro de Oleo", EntityStatus.ACTIVE)));

        List<Material> result = useCase.execute("active");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void shouldReturnInactiveMaterialsWhenStatusIsInactive() {
        when(repository.findByStatus(EntityStatus.INACTIVE))
            .thenReturn(List.of(aMaterial(5L, "Bateria 60Ah", EntityStatus.INACTIVE)));

        List<Material> result = useCase.execute("INACTIVE");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    void shouldReturnEmptyListWhenNoMaterialMatchesStatus() {
        when(repository.findByStatus(EntityStatus.INACTIVE)).thenReturn(List.of());

        List<Material> result = useCase.execute("INACTIVE");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRejectUnknownStatus() {
        assertThatThrownBy(() -> useCase.execute("ARCHIVED"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static Material aMaterial(Long id, String name, EntityStatus status) {
        return new Material(
            id,
            name,
            "Descricao de catalogo",
            new BigDecimal("32.50"),
            25,
            5,
            status,
            Instant.now(),
            Instant.now()
        );
    }
}
