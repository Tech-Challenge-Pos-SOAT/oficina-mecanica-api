package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.GetCustomerByDocumentUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.serviceorder.AuthorType;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderHistory;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RejectServiceOrderBudgetUseCaseTest {
    private static final String DOCUMENT = "529.982.247-25";

    @Mock private ServiceOrderRepository repository;
    @Mock private GetCustomerByDocumentUseCase getCustomerByDocumentUseCase;
    @InjectMocks private RejectServiceOrderBudgetUseCase useCase;

    @Test
    void shouldFinishOrderKeepingTheRejectionReason() {
        ServiceOrder order = ServiceOrder.open(7L, 3L, 1L);
        order.startDiagnosis(1L);
        order.addService(5L, new BigDecimal("120.00"));
        order.submitForApproval(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(order));
        when(getCustomerByDocumentUseCase.execute(DOCUMENT)).thenReturn(
            new Customer(7L, new Document(DOCUMENT), "Maria Oliveira", "(31) 98765-4321",
                "maria@email.com", EntityStatus.ACTIVE, Instant.now(), Instant.now()));
        when(repository.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));

        ServiceOrder result = useCase.execute(1L, DOCUMENT, "Preco alto");

        List<ServiceOrderHistory> history = result.getHistory();
        ServiceOrderHistory last = history.get(history.size() - 1);
        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.FINISHED);
        assertThat(last.getAuthorType()).isEqualTo(AuthorType.CUSTOMER);
        assertThat(last.getObservation()).isEqualTo("Preco alto");
    }
}
