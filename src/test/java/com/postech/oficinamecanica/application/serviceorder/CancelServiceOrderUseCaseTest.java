package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.material.ReturnStockUseCase;
import com.postech.oficinamecanica.domain.serviceorder.AuthorType;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderHistory;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderMaterial;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelServiceOrderUseCaseTest {

    @Mock private ServiceOrderRepository repository;
    @Mock private ReturnStockUseCase returnStockUseCase;
    @Mock private ActiveEmployeeFinder activeEmployeeFinder;
    @InjectMocks private CancelServiceOrderUseCase useCase;

    /** Ordem em execucao, com a peca 9 ja baixada do estoque. */
    private ServiceOrder executingOrderWithDebitedMaterial() {
        Instant now = Instant.now();
        ServiceOrderMaterial material =
            new ServiceOrderMaterial(10L, 9L, 2, new BigDecimal("40.00"), true, now, now);
        ServiceOrderHistory entry = new ServiceOrderHistory(
            1L, ServiceOrderStatus.IN_EXECUTION, new BigDecimal("80.00"), AuthorType.CUSTOMER, 7L, null, now);

        return new ServiceOrder(1L, 7L, 3L, new BigDecimal("80.00"), ServiceOrderStatus.IN_EXECUTION,
            List.of(), List.of(material), List.of(entry), now, now);
    }

    @Test
    void shouldReturnDebitedMaterialsToStockWhenCancelling() {
        when(repository.findById(1L)).thenReturn(Optional.of(executingOrderWithDebitedMaterial()));
        when(repository.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));

        ServiceOrder result = useCase.execute(1L, 1L, "Cliente desistiu do reparo");

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
        assertThat(result.debitedMaterials()).isEmpty();
        verify(returnStockUseCase).execute(9L, 1L, 2);
        verify(activeEmployeeFinder).findActive(1L);
    }

    @Test
    void shouldKeepTheOrderInExecutionWhenTheSystemCancelsAnAdditionalRepair() {
        ServiceOrder order = executingOrderWithDebitedMaterial();
        order.addMaterial(11L, 1, new BigDecimal("25.00"));
        order.submitForApproval(1L);
        when(repository.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));

        ServiceOrder result = useCase.cancelBySystem(order, "Orcamento sem resposta em 7 dias");

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.IN_EXECUTION);
        assertThat(result.getMaterials()).hasSize(1);
        assertThat(result.getMaterials().get(0).getMaterialId()).isEqualTo(9L);
        verify(returnStockUseCase, never()).execute(any(), any(), any());
    }
}
