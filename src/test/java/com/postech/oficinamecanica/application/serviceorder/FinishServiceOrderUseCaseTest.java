package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.GetCustomerUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FinishServiceOrderUseCaseTest {
    private static final Long ORDER_ID = 1L;

    @Mock private ServiceOrderRepository repository;
    @Mock private ActiveEmployeeFinder activeEmployeeFinder;
    @Mock private GetCustomerUseCase getCustomerUseCase;
    @Mock private ServiceOrderNotifier notifier;
    @InjectMocks private FinishServiceOrderUseCase useCase;

    private Customer customer() {
        return new Customer(7L, new Document("529.982.247-25"), "Maria Oliveira", "(31) 98765-4321",
            "maria@email.com", EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    private ServiceOrder orderInDiagnosis() {
        ServiceOrder order = ServiceOrder.open(7L, 3L, 1L);
        order.startDiagnosis(1L);
        return order;
    }

    @Test
    void shouldFinishOrderAndNotifyCustomerWhenServiceIsDone() {
        Customer customer = customer();
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(orderInDiagnosis()));
        when(repository.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));
        when(getCustomerUseCase.execute(7L)).thenReturn(customer);

        ServiceOrder result = useCase.execute(ORDER_ID, 1L, "Sem pecas a substituir");

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.FINISHED);
        verify(notifier).notifyFinished(result, customer);
    }
}
