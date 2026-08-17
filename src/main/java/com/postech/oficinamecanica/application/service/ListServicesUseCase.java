package com.postech.oficinamecanica.application.service;

import com.postech.oficinamecanica.domain.service.Service;
import java.util.List;

@org.springframework.stereotype.Service
public class ListServicesUseCase {
    private final ServiceRepository repository;

    public ListServicesUseCase(ServiceRepository repository) {
        this.repository = repository;
    }

    public List<Service> execute() {
        return repository.findAll();
    }
}
