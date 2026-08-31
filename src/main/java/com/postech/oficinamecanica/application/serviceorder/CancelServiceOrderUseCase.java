package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.material.ReturnStockUseCase;
import com.postech.oficinamecanica.domain.serviceorder.AuthorType;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderMaterial;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cancelamento da ordem. Toda peca que ja tinha saido do estoque por essa
 * ordem volta, com movimentacao IN vinculada - por isso o cancelamento passa
 * obrigatoriamente por aqui, e nao direto no agregado.
 */
@Service
public class CancelServiceOrderUseCase {
    private final ServiceOrderRepository repository;
    private final ReturnStockUseCase returnStockUseCase;
    private final ActiveEmployeeFinder activeEmployeeFinder;

    public CancelServiceOrderUseCase(ServiceOrderRepository repository,
                                     ReturnStockUseCase returnStockUseCase,
                                     ActiveEmployeeFinder activeEmployeeFinder) {
        this.repository = repository;
        this.returnStockUseCase = returnStockUseCase;
        this.activeEmployeeFinder = activeEmployeeFinder;
    }

    /** Cancelamento manual pelo funcionario. */
    @Transactional
    public ServiceOrder execute(Long serviceOrderId, Long employeeId, String reason) {
        activeEmployeeFinder.findActive(employeeId);

        ServiceOrder order = repository.findById(serviceOrderId)
            .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        returnDebitedMaterials(order);
        order.cancel(AuthorType.EMPLOYEE, employeeId, reason);
        return repository.save(order);
    }

    /** Cancelamento disparado por regra do sistema (peca, servico ou prazo). */
    @Transactional
    public ServiceOrder cancelBySystem(ServiceOrder order, String reason) {
        if (!order.hasBeenExecuted()) {
            returnDebitedMaterials(order);
        }
        order.cancelBySystem(reason);
        return repository.save(order);
    }

    private void returnDebitedMaterials(ServiceOrder order) {
        for (ServiceOrderMaterial material : order.debitedMaterials()) {
            returnStockUseCase.execute(material.getMaterialId(), order.getId(), material.getQuantity());
        }
        order.markStockReturned();
    }
}
