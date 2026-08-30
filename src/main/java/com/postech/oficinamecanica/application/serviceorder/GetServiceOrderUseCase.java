package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetServiceOrderUseCase {
    private final ServiceOrderRepository repository;

    public GetServiceOrderUseCase(ServiceOrderRepository repository) {
        this.repository = repository;
    }

    public ServiceOrder execute(Long serviceOrderId) {
        return repository.findById(serviceOrderId)
            .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));
    }
}
