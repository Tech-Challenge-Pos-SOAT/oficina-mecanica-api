package com.postech.oficinamecanica.application.service;

import com.postech.oficinamecanica.domain.service.Service;
import com.postech.oficinamecanica.domain.service.ServiceNotFoundException;

@org.springframework.stereotype.Service
public class FindServiceByIdUseCase {
    private final ServiceRepository repository;

    public FindServiceByIdUseCase(ServiceRepository repository) {
        this.repository = repository;
    }

    public Service execute(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ServiceNotFoundException(id));
    }
}
