package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.GetCustomerByDocumentUseCase;
import com.postech.oficinamecanica.application.material.StockDebitUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.material.InsufficientStockException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderAccessDeniedException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApproveServiceOrderBudgetUseCaseTest {
    private static final String DOCUMENT = "529.982.247-25";
    private static final Long ORDER_ID = 1L;

    @Mock private ServiceOrderRepository repository;
    @Mock private GetCustomerByDocumentUseCase getCustomerByDocumentUseCase;
    @Mock private StockDebitUseCase stockDebitUseCase;
    @InjectMocks private ApproveServiceOrderBudgetUseCase useCase;

    private Customer customer(Long id) {
        return new Customer(id, new Document(DOCUMENT), "Maria Oliveira", "(31) 98765-4321",
            "maria@email.com", EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    private ServiceOrder orderAwaitingApproval() {
        ServiceOrder order = ServiceOrder.open(7L, 3L, 1L);
        order.startDiagnosis(1L);
        order.addService(5L, new BigDecimal("120.00"));
        order.addMaterial(9L, 2, new BigDecimal("40.00"));
        order.submitForApproval(1L);
        return order;
    }

    @Test
    void shouldMoveToExecutionAndDebitStockOfEveryPendingMaterial() {
        ServiceOrder order = orderAwaitingApproval();
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(getCustomerByDocumentUseCase.execute(DOCUMENT)).thenReturn(customer(7L));
        when(repository.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));

        ServiceOrder result = useCase.execute(ORDER_ID, DOCUMENT);

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.IN_EXECUTION);
        assertThat(result.pendingStockDebit()).isEmpty();
        verify(stockDebitUseCase).execute(9L, ORDER_ID, 2);
    }

    @Test
    void shouldRejectApprovalFromAnotherCustomerDocument() {
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(orderAwaitingApproval()));
        when(getCustomerByDocumentUseCase.execute(DOCUMENT)).thenReturn(customer(999L));

        assertThatThrownBy(() -> useCase.execute(ORDER_ID, DOCUMENT))
            .isInstanceOf(ServiceOrderAccessDeniedException.class);

        verify(stockDebitUseCase, never()).execute(anyLong(), anyLong(), anyInt());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldNotPersistApprovalWhenStockIsInsufficient() {
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(orderAwaitingApproval()));
        when(getCustomerByDocumentUseCase.execute(DOCUMENT)).thenReturn(customer(7L));
        when(stockDebitUseCase.execute(9L, ORDER_ID, 2))
            .thenThrow(new InsufficientStockException("Insufficient stock for material 9"));

        assertThatThrownBy(() -> useCase.execute(ORDER_ID, DOCUMENT))
            .isInstanceOf(InsufficientStockException.class);

        verify(repository, never()).save(any());
    }
}
