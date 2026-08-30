package com.postech.oficinamecanica.domain.serviceorder;

import java.util.Set;

public enum ServiceOrderStatus {
    RECEIVED,
    IN_DIAGNOSIS,
    AWAITING_APPROVAL,
    IN_EXECUTION,
    FINISHED,
    DELIVERED;

    public boolean canTransitionTo(ServiceOrderStatus target) {
        return allowedTargets().contains(target);
    }

    private Set<ServiceOrderStatus> allowedTargets() {
        return switch (this) {
            case RECEIVED -> Set.of(IN_DIAGNOSIS);
            case IN_DIAGNOSIS -> Set.of(AWAITING_APPROVAL, FINISHED);
            case AWAITING_APPROVAL -> Set.of(IN_EXECUTION, FINISHED);
            case IN_EXECUTION -> Set.of(AWAITING_APPROVAL, FINISHED);
            case FINISHED -> Set.of(DELIVERED);
            case DELIVERED -> Set.of();
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
