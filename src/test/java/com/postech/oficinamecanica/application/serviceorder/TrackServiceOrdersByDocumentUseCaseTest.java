package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.GetCustomerByDocumentUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackServiceOrdersByDocumentUseCaseTest {
    private static final String DOCUMENT = "529.982.247-25";

    @Mock private ServiceOrderRepository repository;
    @Mock private GetCustomerByDocumentUseCase getCustomerByDocumentUseCase;
    @InjectMocks private TrackServiceOrdersByDocumentUseCase useCase;

    @Test
    void shouldListOnlyOrdersOfTheCustomerBehindTheDocument() {
        Customer customer = new Customer(7L, new Document(DOCUMENT), "Maria Oliveira", "(31) 98765-4321",
            "maria@email.com", EntityStatus.ACTIVE, Instant.now(), Instant.now());
        ServiceOrder order = ServiceOrder.open(7L, 3L, 1L);

        when(getCustomerByDocumentUseCase.execute(DOCUMENT)).thenReturn(customer);
        when(repository.findByCustomerId(7L)).thenReturn(List.of(order));

        List<ServiceOrder> result = useCase.execute(DOCUMENT);

        assertThat(result).containsExactly(order);
    }
}
