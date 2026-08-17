package com.postech.oficinamecanica.application.service;

import com.postech.oficinamecanica.domain.service.Service;
import com.postech.oficinamecanica.domain.service.ServiceNotFoundException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;

@org.springframework.stereotype.Service
public class ChangeServiceStatusUseCase {
    private final ServiceRepository repository;

    public ChangeServiceStatusUseCase(ServiceRepository repository) {
        this.repository = repository;
    }

    public Service execute(Long id, ChangeServiceStatusCommand cmd) {
        Service service = repository.findById(id)
            .orElseThrow(() -> new ServiceNotFoundException(id));

        EntityStatus status = EntityStatus.valueOf(cmd.status().toUpperCase());

        Service updated = status == EntityStatus.ACTIVE
            ? service.activate()
            : service.deactivate();

        return repository.save(updated);
    }
}
