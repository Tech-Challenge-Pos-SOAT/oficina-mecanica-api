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
class ListLowStockMaterialsUseCaseTest {

    @Mock
    private MaterialRepository repository;

    @InjectMocks
    private ListLowStockMaterialsUseCase useCase;

    @Test
    void shouldReturnActiveLowStockMaterialsWhenStatusIsNull() {
        when(repository.findLowStockByStatus(EntityStatus.ACTIVE))
            .thenReturn(List.of(aMaterial(6L, "Vela de Ignicao")));

        List<Material> result = useCase.execute(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Vela de Ignicao");
    }

    @Test
    void shouldRejectUnknownStatus() {
        assertThatThrownBy(() -> useCase.execute("DELETED"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static Material aMaterial(Long id, String name) {
        return new Material(id, name, null, BigDecimal.TEN, 2, 8, EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }
}
