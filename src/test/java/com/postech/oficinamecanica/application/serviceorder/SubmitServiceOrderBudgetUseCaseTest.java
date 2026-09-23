package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.GetCustomerUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.serviceorder.EmptyServiceOrderException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitServiceOrderBudgetUseCaseTest {
    private static final Long ORDER_ID = 1L;

    @Mock private ServiceOrderRepository repository;
    @Mock private ActiveEmployeeFinder activeEmployeeFinder;
    @Mock private GetCustomerUseCase getCustomerUseCase;
    @Mock private ServiceOrderNotifier notifier;
    @InjectMocks private SubmitServiceOrderBudgetUseCase useCase;

    private Customer customer() {
        return new Customer(7L, new Document("529.982.247-25"), "Maria Oliveira", "(31) 98765-4321",
            "maria@email.com", EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    private ServiceOrder orderWithService() {
        ServiceOrder order = ServiceOrder.open(7L, 3L, 1L);
        order.startDiagnosis(1L);
        order.addService(5L, new BigDecimal("120.00"));
        return order;
    }

    @Test
    void shouldMoveToAwaitingApprovalAndNotifyCustomerWhenBudgetIsSubmitted() {
        Customer customer = customer();
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(orderWithService()));
        when(repository.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));
        when(getCustomerUseCase.execute(7L)).thenReturn(customer);

        ServiceOrder result = useCase.execute(ORDER_ID, 1L);

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
        verify(notifier).notifyBudgetSubmitted(result, customer);
    }

    @Test
    void shouldNotNotifyWhenOrderHasNoServiceOrMaterial() {
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(ServiceOrder.open(7L, 3L, 1L)));

        assertThatThrownBy(() -> useCase.execute(ORDER_ID, 1L))
            .isInstanceOf(EmptyServiceOrderException.class);

        verify(notifier, never()).notifyBudgetSubmitted(any(), any());
        verify(repository, never()).save(any());
    }
}
