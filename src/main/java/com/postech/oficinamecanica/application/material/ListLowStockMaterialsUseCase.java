package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListLowStockMaterialsUseCase {
    private final MaterialRepository repository;

    public ListLowStockMaterialsUseCase(MaterialRepository repository) {
        this.repository = repository;
    }

    public List<Material> execute(String statusParam) {
        EntityStatus status = (statusParam == null || statusParam.isBlank())
            ? EntityStatus.ACTIVE
            : EntityStatus.valueOf(statusParam.toUpperCase());

        return repository.findLowStockByStatus(status);
    }
}
