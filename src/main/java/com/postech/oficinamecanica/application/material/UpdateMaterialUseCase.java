package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.exceptions.BusinessRuleViolationException;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;

@Service
public class UpdateMaterialUseCase {
    private final MaterialRepository repository;

    public UpdateMaterialUseCase(MaterialRepository repository) {
        this.repository = repository;
    }

    public Material execute(Long id, String name, String description, BigDecimal price, Integer stockQuantity, Integer stockMinimum) {
        Material material = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Material", id));

        if (!material.getName().equals(name) && repository.existsByName(name)) {
            throw new BusinessRuleViolationException("Material name already exists: " + name);
        }

        Material updated = new Material(
            material.getId(),
            name,
            description,
            price,
            stockQuantity,
            stockMinimum,
            material.getStatus(),
            material.getCreatedAt(),
            Instant.now()
        );

        return repository.save(updated);
    }
}
