package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.AuthorType;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderHistory;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderService;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpireServiceOrderBudgetsUseCaseTest {

    @Mock private ServiceOrderRepository repository;
    @Mock private CancelServiceOrderUseCase cancelServiceOrderUseCase;

    private ExpireServiceOrderBudgetsUseCase useCase() {
        return new ExpireServiceOrderBudgetsUseCase(repository, cancelServiceOrderUseCase, 7);
    }

    private ServiceOrder awaitingApprovalSince(Instant enviadoEm) {
        Instant now = Instant.now();
        ServiceOrderService service =
            new ServiceOrderService(10L, 5L, new BigDecimal("120.00"), false, enviadoEm, enviadoEm);
        ServiceOrderHistory entry = new ServiceOrderHistory(1L, ServiceOrderStatus.AWAITING_APPROVAL,
            new BigDecimal("120.00"), AuthorType.EMPLOYEE, 1L, null, enviadoEm);

        return new ServiceOrder(1L, 7L, 3L, new BigDecimal("120.00"), ServiceOrderStatus.AWAITING_APPROVAL,
            List.of(service), List.of(), List.of(entry), enviadoEm, now);
    }

    @Test
    void shouldExpireOrdersWaitingLongerThanTheDeadline() {
        ServiceOrder vencida = awaitingApprovalSince(Instant.now().minus(8, ChronoUnit.DAYS));
        when(repository.findAll(ServiceOrderStatus.AWAITING_APPROVAL)).thenReturn(List.of(vencida));
        when(cancelServiceOrderUseCase.cancelBySystem(any(ServiceOrder.class), anyString())).thenReturn(vencida);

        assertThat(useCase().execute()).hasSize(1);

        verify(cancelServiceOrderUseCase).cancelBySystem(any(ServiceOrder.class), anyString());
    }

    @Test
    void shouldLeaveOrdersStillWithinTheDeadlineAlone() {
        ServiceOrder recente = awaitingApprovalSince(Instant.now().minus(2, ChronoUnit.DAYS));
        when(repository.findAll(ServiceOrderStatus.AWAITING_APPROVAL)).thenReturn(List.of(recente));

        assertThat(useCase().execute()).isEmpty();

        verify(cancelServiceOrderUseCase, never()).cancelBySystem(any(), anyString());
    }
}
