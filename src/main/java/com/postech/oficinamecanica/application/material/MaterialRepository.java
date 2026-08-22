package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.util.List;
import java.util.Optional;

public interface MaterialRepository {
    List<Material> findByStatus(EntityStatus status);
    Optional<Material> findById(Long id);
    Optional<Material> findByIdForUpdate(Long id);
    List<Material> findLowStockByStatus(EntityStatus status);
    Material save(Material material);
    boolean existsByName(String name);
}
