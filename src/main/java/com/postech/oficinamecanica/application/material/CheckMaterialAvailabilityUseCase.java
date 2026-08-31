package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Checa, com a linha do material travada, se da' para atender a quantidade
 * pedida. Usado antes de aprovar um orcamento: se faltar qualquer item, a
 * ordem nao chega a debitar nada.
 */
@Service
public class CheckMaterialAvailabilityUseCase {
    private final MaterialRepository repository;

    public CheckMaterialAvailabilityUseCase(MaterialRepository repository) {
        this.repository = repository;
    }

    public boolean isAvailable(Long materialId, Integer quantity) {
        Optional<Material> material = repository.findByIdForUpdate(materialId);
        return material
            .filter(found -> found.getStatus() == EntityStatus.ACTIVE)
            .filter(found -> found.getStockQuantity() >= quantity)
            .isPresent();
    }
}
