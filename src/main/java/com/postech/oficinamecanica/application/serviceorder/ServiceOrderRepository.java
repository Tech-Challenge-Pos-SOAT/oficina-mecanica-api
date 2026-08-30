package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;

import java.util.List;
import java.util.Optional;

public interface ServiceOrderRepository {
    ServiceOrder save(ServiceOrder serviceOrder);

    Optional<ServiceOrder> findById(Long id);

    List<ServiceOrder> findByCustomerId(Long customerId);

    /** status nulo lista todas as ordens. */
    List<ServiceOrder> findAll(ServiceOrderStatus status);
}
