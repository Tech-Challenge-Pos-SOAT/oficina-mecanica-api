package com.postech.oficinamecanica.application.service;

import com.postech.oficinamecanica.domain.service.DuplicateServiceNameException;
import com.postech.oficinamecanica.domain.service.Service;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.time.Instant;

@org.springframework.stereotype.Service
public class CreateServiceUseCase {
    private final ServiceRepository repository;

    public CreateServiceUseCase(ServiceRepository repository) {
        this.repository = repository;
    }

    public Service execute(CreateServiceCommand cmd) {
        repository.findByName(cmd.name())
            .ifPresent(s -> { throw new DuplicateServiceNameException(cmd.name()); });

        Service service = new Service(
            null,
            cmd.name(),
            cmd.description(),
            cmd.price(),
            EntityStatus.ACTIVE,
            Instant.now(),
            Instant.now()
        );

        return repository.save(service);
    }
}
