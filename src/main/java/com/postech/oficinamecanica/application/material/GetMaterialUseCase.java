package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.material.MaterialNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetMaterialUseCase {
    private final MaterialRepository repository;

    public GetMaterialUseCase(MaterialRepository repository) {
        this.repository = repository;
    }

    public Material execute(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new MaterialNotFoundException(id));
    }
}
