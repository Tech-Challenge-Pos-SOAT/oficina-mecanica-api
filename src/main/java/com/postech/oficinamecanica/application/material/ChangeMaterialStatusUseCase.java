package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Service;

@Service
public class ChangeMaterialStatusUseCase {
    private final MaterialRepository repository;

    public ChangeMaterialStatusUseCase(MaterialRepository repository) {
        this.repository = repository;
    }

    public Material execute(Long id, String statusParam) {
        EntityStatus newStatus = EntityStatus.valueOf(statusParam.toUpperCase());

        Material material = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Material not found with id: " + id));

        material.changeStatus(newStatus);

        return repository.save(material);
    }
}
