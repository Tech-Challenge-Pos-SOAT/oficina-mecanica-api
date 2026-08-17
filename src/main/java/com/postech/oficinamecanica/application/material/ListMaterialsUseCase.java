package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListMaterialsUseCase {
    private final MaterialRepository repository;

    public ListMaterialsUseCase(MaterialRepository repository) {
        this.repository = repository;
    }

    public List<Material> execute(String statusParam) {
        EntityStatus status = (statusParam == null || statusParam.isBlank())
            ? EntityStatus.ACTIVE
            : EntityStatus.valueOf(statusParam.toUpperCase());

        return repository.findByStatus(status);
    }
}
