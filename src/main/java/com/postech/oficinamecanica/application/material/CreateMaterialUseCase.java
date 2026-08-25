package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.shared.exceptions.BusinessRuleViolationException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;

@Service
public class CreateMaterialUseCase {
    private final MaterialRepository repository;

    public CreateMaterialUseCase(MaterialRepository repository) {
        this.repository = repository;
    }

    public Material execute(String name, String description, BigDecimal price, Integer stockQuantity, Integer stockMinimum) {
        if (repository.existsByName(name)) {
            throw new BusinessRuleViolationException("Material name already exists: " + name);
        }

        Material newMaterial = new Material(
            null,
            name,
            description,
            price,
            stockQuantity,
            stockMinimum,
            EntityStatus.ACTIVE,
            Instant.now(),
            Instant.now()
        );

        return repository.save(newMaterial);
    }
}
