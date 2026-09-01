package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Orcamento sem resposta do cliente dentro do prazo. Ordem que nunca foi
 * executada e' cancelada; ordem que ja tinha um ciclo aprovado perde so o
 * reparo adicional e volta para IN_EXECUTION.
 */
@Service
public class ExpireServiceOrderBudgetsUseCase {
    private final ServiceOrderRepository repository;
    private final CancelServiceOrderUseCase cancelServiceOrderUseCase;
    private final long deadlineDays;

    public ExpireServiceOrderBudgetsUseCase(
            ServiceOrderRepository repository,
            CancelServiceOrderUseCase cancelServiceOrderUseCase,
            @Value("${serviceorder.budget-approval-deadline-days:7}") long deadlineDays) {
        this.repository = repository;
        this.cancelServiceOrderUseCase = cancelServiceOrderUseCase;
        this.deadlineDays = deadlineDays;
    }

    @Transactional
    public List<ServiceOrder> execute() {
        Instant limite = Instant.now().minus(deadlineDays, ChronoUnit.DAYS);
        List<ServiceOrder> afetadas = new ArrayList<>();

        for (ServiceOrder order : repository.findAll(ServiceOrderStatus.AWAITING_APPROVAL)) {
            Instant enviadoEm = order.awaitingApprovalSince();
            if (enviadoEm == null || !enviadoEm.isBefore(limite)) {
                continue;
            }
            afetadas.add(cancelServiceOrderUseCase.cancelBySystem(order,
                "Orcamento sem resposta do cliente em " + deadlineDays + " dias"));
        }
        return afetadas;
    }
}
