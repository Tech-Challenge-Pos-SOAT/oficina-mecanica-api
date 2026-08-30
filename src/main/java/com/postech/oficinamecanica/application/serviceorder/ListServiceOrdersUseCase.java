package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListServiceOrdersUseCase {
    private final ServiceOrderRepository repository;

    public ListServiceOrdersUseCase(ServiceOrderRepository repository) {
        this.repository = repository;
    }

    public List<ServiceOrder> execute(String statusParam) {
        ServiceOrderStatus status = (statusParam == null || statusParam.isBlank())
            ? null
            : ServiceOrderStatus.fromValue(statusParam);

        return repository.findAll(status);
    }
}
