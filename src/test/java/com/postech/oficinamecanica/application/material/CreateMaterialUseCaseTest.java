package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.shared.exceptions.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateMaterialUseCaseTest {

    @Mock
    private MaterialRepository repository;

    @InjectMocks
    private CreateMaterialUseCase useCase;

    @Test
    void shouldCreateMaterialSuccessfully() {
        when(repository.existsByName("Pneu 17")).thenReturn(false);
        when(repository.save(any(Material.class))).thenAnswer(invocation -> {
            Material saved = invocation.getArgument(0);
            return new Material(100L, saved.getName(), saved.getDescription(), saved.getPrice(),
                                saved.getStockQuantity(), saved.getStockMinimum(), saved.getStatus(),
                                saved.getCreatedAt(), saved.getUpdatedAt());
        });

        Material result = useCase.execute("Pneu 17", "Descricao", new BigDecimal("350.00"), 10, 2);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getName()).isEqualTo("Pneu 17");
        assertThat(result.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        verify(repository).save(any(Material.class));
    }

    @Test
    void shouldThrowExceptionWhenNameAlreadyExists() {
        when(repository.existsByName("Vela de Ignição")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("Vela de Ignição", null, BigDecimal.TEN, 0, 0))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Material name already exists");
    }
}
