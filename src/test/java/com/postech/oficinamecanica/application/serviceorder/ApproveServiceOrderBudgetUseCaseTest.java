package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.GetCustomerByDocumentUseCase;
import com.postech.oficinamecanica.application.material.CheckMaterialAvailabilityUseCase;
import com.postech.oficinamecanica.application.material.StockDebitUseCase;
import com.postech.oficinamecanica.application.service.ServiceRepository;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderAccessDeniedException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.anyString;
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
    @Mock private CheckMaterialAvailabilityUseCase checkMaterialAvailabilityUseCase;
    @Mock private ServiceRepository serviceRepository;
    @Mock private CancelServiceOrderUseCase cancelServiceOrderUseCase;
    @InjectMocks private ApproveServiceOrderBudgetUseCase useCase;

    private Customer customer(Long id) {
        return new Customer(id, new Document(DOCUMENT), "Maria Oliveira", "(31) 98765-4321",
            "maria@email.com", EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    private com.postech.oficinamecanica.domain.service.Service catalogService(EntityStatus status) {
        return new com.postech.oficinamecanica.domain.service.Service(5L, "Troca de oleo", "Motor flex",
            new BigDecimal("120.00"), status, Instant.now(), Instant.now());
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
        when(serviceRepository.findById(5L)).thenReturn(Optional.of(catalogService(EntityStatus.ACTIVE)));
        when(checkMaterialAvailabilityUseCase.isAvailable(9L, 2)).thenReturn(true);
        when(repository.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));

        ServiceOrder result = useCase.execute(ORDER_ID, DOCUMENT);

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.IN_EXECUTION);
        assertThat(result.pendingStockDebit()).isEmpty();
        assertThat(result.getServices().get(0).isApproved()).isTrue();
        verify(stockDebitUseCase).execute(9L, ORDER_ID, 2);
    }

    @Test
    void shouldCancelWhenAPartHasNoStock() {
        ServiceOrder order = orderAwaitingApproval();
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(getCustomerByDocumentUseCase.execute(DOCUMENT)).thenReturn(customer(7L));
        when(serviceRepository.findById(5L)).thenReturn(Optional.of(catalogService(EntityStatus.ACTIVE)));
        when(checkMaterialAvailabilityUseCase.isAvailable(9L, 2)).thenReturn(false);

        useCase.execute(ORDER_ID, DOCUMENT);

        ArgumentCaptor<String> motivo = ArgumentCaptor.forClass(String.class);
        verify(cancelServiceOrderUseCase).cancelBySystem(any(ServiceOrder.class), motivo.capture());
        assertThat(motivo.getValue()).contains("Peca 9");
        verify(stockDebitUseCase, never()).execute(anyLong(), anyLong(), anyInt());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldCancelWhenAServiceLeftTheCatalog() {
        ServiceOrder order = orderAwaitingApproval();
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(getCustomerByDocumentUseCase.execute(DOCUMENT)).thenReturn(customer(7L));
        when(serviceRepository.findById(5L)).thenReturn(Optional.of(catalogService(EntityStatus.INACTIVE)));

        useCase.execute(ORDER_ID, DOCUMENT);

        ArgumentCaptor<String> motivo = ArgumentCaptor.forClass(String.class);
        verify(cancelServiceOrderUseCase).cancelBySystem(any(ServiceOrder.class), motivo.capture());
        assertThat(motivo.getValue()).contains("Servico 5");
        verify(stockDebitUseCase, never()).execute(anyLong(), anyLong(), anyInt());
    }

    @Test
    void shouldCancelWhenTheServiceWasRemovedFromTheCatalog() {
        ServiceOrder order = orderAwaitingApproval();
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(getCustomerByDocumentUseCase.execute(DOCUMENT)).thenReturn(customer(7L));
        when(serviceRepository.findById(5L)).thenReturn(Optional.empty());

        useCase.execute(ORDER_ID, DOCUMENT);

        verify(cancelServiceOrderUseCase).cancelBySystem(any(ServiceOrder.class), anyString());
    }

    @Test
    void shouldRejectApprovalFromAnotherCustomerDocument() {
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(orderAwaitingApproval()));
        when(getCustomerByDocumentUseCase.execute(DOCUMENT)).thenReturn(customer(999L));

        assertThatThrownBy(() -> useCase.execute(ORDER_ID, DOCUMENT))
            .isInstanceOf(ServiceOrderAccessDeniedException.class);

        verify(stockDebitUseCase, never()).execute(anyLong(), anyLong(), anyInt());
        verify(cancelServiceOrderUseCase, never()).cancelBySystem(any(), anyString());
    }
}
