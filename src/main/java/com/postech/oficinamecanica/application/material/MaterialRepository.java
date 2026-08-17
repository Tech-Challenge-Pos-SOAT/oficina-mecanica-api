package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.util.List;

public interface MaterialRepository {
    List<Material> findByStatus(EntityStatus status);
}
