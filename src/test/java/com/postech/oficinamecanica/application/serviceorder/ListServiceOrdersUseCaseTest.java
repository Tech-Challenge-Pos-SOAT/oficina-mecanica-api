package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.InvalidServiceOrderStatusException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListServiceOrdersUseCaseTest {

    @Mock private ServiceOrderRepository repository;
    @InjectMocks private ListServiceOrdersUseCase useCase;

    private ServiceOrder order(Long id, ServiceOrderStatus status, String createdAt) {
        Instant at = Instant.parse(createdAt);
        return new ServiceOrder(id, 7L, 3L, null, status,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), at, at);
    }

    @Test
    void shouldListOnlyActiveStatusesWhenNoFilterIsGiven() {
        when(repository.findByStatusIn(ServiceOrderStatus.activeStatuses())).thenReturn(List.of());

        useCase.execute(null);

        verify(repository).findByStatusIn(ServiceOrderStatus.activeStatuses());
        verify(repository, never()).findAll(any());
        assertThat(ServiceOrderStatus.activeStatuses()).containsExactlyInAnyOrder(
            ServiceOrderStatus.RECEIVED, ServiceOrderStatus.IN_DIAGNOSIS,
            ServiceOrderStatus.AWAITING_APPROVAL, ServiceOrderStatus.IN_EXECUTION);
    }

    @Test
    void shouldTreatBlankFilterAsNoFilter() {
        when(repository.findByStatusIn(ServiceOrderStatus.activeStatuses())).thenReturn(List.of());

        useCase.execute("  ");

        verify(repository).findByStatusIn(ServiceOrderStatus.activeStatuses());
    }

    @Test
    void shouldSortByPriorityThenByOldestFirst() {
        ServiceOrder receivedOld = order(1L, ServiceOrderStatus.RECEIVED, "2026-10-01T10:00:00Z");
        ServiceOrder diagnosis = order(2L, ServiceOrderStatus.IN_DIAGNOSIS, "2026-10-02T10:00:00Z");
        ServiceOrder executionNew = order(3L, ServiceOrderStatus.IN_EXECUTION, "2026-10-05T10:00:00Z");
        ServiceOrder awaiting = order(4L, ServiceOrderStatus.AWAITING_APPROVAL, "2026-10-03T10:00:00Z");
        ServiceOrder executionOld = order(5L, ServiceOrderStatus.IN_EXECUTION, "2026-10-04T10:00:00Z");
        ServiceOrder receivedNew = order(6L, ServiceOrderStatus.RECEIVED, "2026-10-06T10:00:00Z");
        when(repository.findByStatusIn(ServiceOrderStatus.activeStatuses()))
            .thenReturn(List.of(receivedOld, diagnosis, executionNew, awaiting, executionOld, receivedNew));

        List<ServiceOrder> result = useCase.execute(null);

        assertThat(result).extracting(ServiceOrder::getId).containsExactly(5L, 3L, 4L, 2L, 1L, 6L);
    }

    @Test
    void shouldReturnExactlyTheRequestedStatusIncludingClosedOnes() {
        ServiceOrder finished = order(1L, ServiceOrderStatus.FINISHED, "2026-10-01T10:00:00Z");
        when(repository.findAll(ServiceOrderStatus.FINISHED)).thenReturn(List.of(finished));

        List<ServiceOrder> result = useCase.execute("finished");

        assertThat(result).containsExactly(finished);
        verify(repository, never()).findByStatusIn(any());
    }

    @Test
    void shouldRejectUnknownStatus() {
        assertThatThrownBy(() -> useCase.execute("NOPE"))
            .isInstanceOf(InvalidServiceOrderStatusException.class);

        verify(repository, never()).findAll(any());
        verify(repository, never()).findByStatusIn(any());
    }
}
