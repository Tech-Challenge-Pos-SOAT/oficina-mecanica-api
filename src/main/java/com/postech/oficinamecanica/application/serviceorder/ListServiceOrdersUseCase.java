package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Listagem da fila da oficina. Sem filtro, devolve so as ordens em aberto
 * (exclui FINISHED, DELIVERED e CANCELLED), ordenadas por prioridade de
 * atendimento (em execucao, aguardando aprovacao, em diagnostico, recebida) e,
 * dentro da mesma prioridade, da mais antiga para a mais nova. Com
 * {@code status} explicito, devolve exatamente aquele status, inclusive os
 * encerrados, para consulta de historico.
 */
@Service
public class ListServiceOrdersUseCase {
    private static final Comparator<ServiceOrder> QUEUE_ORDER = Comparator
        .comparing(ServiceOrder::getStatus, ServiceOrderStatus.byPriority())
        .thenComparing(ServiceOrder::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(ServiceOrder::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final ServiceOrderRepository repository;

    public ListServiceOrdersUseCase(ServiceOrderRepository repository) {
        this.repository = repository;
    }

    public List<ServiceOrder> execute(String statusParam) {
        if (statusParam != null && !statusParam.isBlank()) {
            return repository.findAll(ServiceOrderStatus.fromValue(statusParam));
        }

        return repository.findByStatusIn(ServiceOrderStatus.activeStatuses())
            .stream()
            .sorted(QUEUE_ORDER)
            .toList();
    }
}
