package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.material.MaterialRepository;
import com.postech.oficinamecanica.domain.material.InactiveMaterialException;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddMaterialToServiceOrderUseCaseTest {

    @Mock private ServiceOrderRepository serviceOrderRepository;
    @Mock private MaterialRepository materialRepository;
    @Mock private ActiveEmployeeFinder activeEmployeeFinder;
    @InjectMocks private AddMaterialToServiceOrderUseCase useCase;

    private Material material(EntityStatus status) {
        return new Material(9L, "Filtro de Oleo", "Linha leve", new BigDecimal("32.50"),
            25, 5, status, Instant.now(), Instant.now());
    }

    private ServiceOrder orderInDiagnosis() {
        ServiceOrder order = ServiceOrder.open(7L, 3L, 1L);
        order.startDiagnosis(1L);
        return order;
    }

    @Test
    void shouldCopyCatalogPriceIntoOrderWithoutTouchingStock() {
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(orderInDiagnosis()));
        when(materialRepository.findById(9L)).thenReturn(Optional.of(material(EntityStatus.ACTIVE)));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));

        ServiceOrder result = useCase.execute(1L, 9L, 2, 1L);

        assertThat(result.getMaterials()).hasSize(1);
        assertThat(result.getMaterials().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("32.50"));
        assertThat(result.getMaterials().get(0).isStockDebited()).isFalse();
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("65.00"));
        verify(materialRepository, never()).save(any());
    }

    @Test
    void shouldRejectInactiveMaterial() {
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(orderInDiagnosis()));
        when(materialRepository.findById(9L)).thenReturn(Optional.of(material(EntityStatus.INACTIVE)));

        assertThatThrownBy(() -> useCase.execute(1L, 9L, 2, 1L))
            .isInstanceOf(InactiveMaterialException.class);

        verify(serviceOrderRepository, never()).save(any());
    }
}
