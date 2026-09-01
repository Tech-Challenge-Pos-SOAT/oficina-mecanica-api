package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.GetCustomerByDocumentUseCase;
import com.postech.oficinamecanica.application.material.CheckMaterialAvailabilityUseCase;
import com.postech.oficinamecanica.application.material.StockDebitUseCase;
import com.postech.oficinamecanica.application.service.ServiceRepository;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderAccessDeniedException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderMaterial;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotFoundException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderService;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aprovacao do orcamento pelo cliente.
 *
 * Antes de debitar qualquer coisa, confere se a oficina ainda consegue
 * executar: servico continua no catalogo e peca tem saldo. Se algo impedir, a
 * ordem nao fica pendurada esperando - o sistema encerra. Ordem que nunca foi
 * executada e' cancelada; ordem que ja tinha um ciclo aprovado perde apenas o
 * reparo adicional e volta a executar o que ja estava autorizado.
 */
@Service
public class ApproveServiceOrderBudgetUseCase {
    private final ServiceOrderRepository repository;
    private final GetCustomerByDocumentUseCase getCustomerByDocumentUseCase;
    private final StockDebitUseCase stockDebitUseCase;
    private final CheckMaterialAvailabilityUseCase checkMaterialAvailabilityUseCase;
    private final ServiceRepository serviceRepository;
    private final CancelServiceOrderUseCase cancelServiceOrderUseCase;

    public ApproveServiceOrderBudgetUseCase(ServiceOrderRepository repository,
                                            GetCustomerByDocumentUseCase getCustomerByDocumentUseCase,
                                            StockDebitUseCase stockDebitUseCase,
                                            CheckMaterialAvailabilityUseCase checkMaterialAvailabilityUseCase,
                                            ServiceRepository serviceRepository,
                                            CancelServiceOrderUseCase cancelServiceOrderUseCase) {
        this.repository = repository;
        this.getCustomerByDocumentUseCase = getCustomerByDocumentUseCase;
        this.stockDebitUseCase = stockDebitUseCase;
        this.checkMaterialAvailabilityUseCase = checkMaterialAvailabilityUseCase;
        this.serviceRepository = serviceRepository;
        this.cancelServiceOrderUseCase = cancelServiceOrderUseCase;
    }

    @Transactional
    public ServiceOrder execute(Long serviceOrderId, String customerDocument) {
        ServiceOrder order = repository.findById(serviceOrderId)
            .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        Customer customer = getCustomerByDocumentUseCase.execute(customerDocument);
        if (!order.getCustomerId().equals(customer.getId())) {
            throw new ServiceOrderAccessDeniedException(serviceOrderId);
        }

        String impedimento = firstBlocker(order);
        if (impedimento != null) {
            return cancelServiceOrderUseCase.cancelBySystem(order, impedimento);
        }

        order.approveBudget();

        for (ServiceOrderMaterial material : order.pendingStockDebit()) {
            stockDebitUseCase.execute(material.getMaterialId(), serviceOrderId, material.getQuantity());
        }
        order.markCycleApproved();

        return repository.save(order);
    }

    /** Primeiro motivo que impede a execucao, ou null se esta tudo disponivel. */
    private String firstBlocker(ServiceOrder order) {
        for (ServiceOrderService item : order.getServices()) {
            if (item.isApproved()) {
                continue;
            }
            boolean disponivel = serviceRepository.findById(item.getServiceId())
                .filter(catalogo -> catalogo.getStatus() == EntityStatus.ACTIVE)
                .isPresent();

            if (!disponivel) {
                return "Servico " + item.getServiceId() + " nao esta mais disponivel no catalogo";
            }
        }

        for (ServiceOrderMaterial item : order.pendingStockDebit()) {
            if (!checkMaterialAvailabilityUseCase.isAvailable(item.getMaterialId(), item.getQuantity())) {
                return "Peca " + item.getMaterialId() + " sem saldo para atender a quantidade "
                    + item.getQuantity();
            }
        }
        return null;
    }
}
