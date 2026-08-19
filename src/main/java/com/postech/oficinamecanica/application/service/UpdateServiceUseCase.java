package com.postech.oficinamecanica.application.service;

import com.postech.oficinamecanica.domain.service.DuplicateServiceNameException;
import com.postech.oficinamecanica.domain.service.Service;
import com.postech.oficinamecanica.domain.service.ServiceNotFoundException;

@org.springframework.stereotype.Service
public class UpdateServiceUseCase {
    private final ServiceRepository repository;

    public UpdateServiceUseCase(ServiceRepository repository) {
        this.repository = repository;
    }

    public Service execute(Long id, UpdateServiceCommand cmd) {
        Service service = repository.findById(id)
            .orElseThrow(() -> new ServiceNotFoundException(id));

        repository.findByName(cmd.name())
            .filter(other -> !other.getId().equals(id))
            .ifPresent(other -> { throw new DuplicateServiceNameException(cmd.name()); });

        Service updated = service.update(cmd.name(), cmd.description(), cmd.price());

        return repository.save(updated);
    }
}
