package com.postech.oficinamecanica.domain.serviceorder;

import java.util.Set;

public enum ServiceOrderStatus {
    RECEIVED,
    IN_DIAGNOSIS,
    AWAITING_APPROVAL,
    IN_EXECUTION,
    FINISHED,
    DELIVERED,
    /**
     * Encerramento sem execucao: falta de peca, servico saido do catalogo ou
     * orcamento vencido sem resposta. Diferente de FINISHED, que e' conclusao
     * (ou recusa) de um atendimento que chegou ao fim normalmente.
     */
    CANCELLED;

    public boolean canTransitionTo(ServiceOrderStatus target) {
        return allowedTargets().contains(target);
    }

    private Set<ServiceOrderStatus> allowedTargets() {
        return switch (this) {
            case RECEIVED -> Set.of(IN_DIAGNOSIS, CANCELLED);
            case IN_DIAGNOSIS -> Set.of(AWAITING_APPROVAL, FINISHED, CANCELLED);
            case AWAITING_APPROVAL -> Set.of(IN_EXECUTION, FINISHED, CANCELLED);
            // Cancelamento a partir da execucao so pelo caminho manual (o
            // funcionario desiste do reparo e devolve as pecas ao estoque).
            // As regras automaticas nunca cancelam ordem ja em execucao: elas
            // descartam o reparo adicional e voltam para IN_EXECUTION.
            case IN_EXECUTION -> Set.of(AWAITING_APPROVAL, FINISHED, CANCELLED);
            case FINISHED -> Set.of(DELIVERED);
            case DELIVERED, CANCELLED -> Set.of();
        };
    }

    public static ServiceOrderStatus fromValue(String value) {
        try {
            return ServiceOrderStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidServiceOrderStatusException(value);
        }
    }
}
