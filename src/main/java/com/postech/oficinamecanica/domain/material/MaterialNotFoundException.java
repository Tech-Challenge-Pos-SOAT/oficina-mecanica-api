package com.postech.oficinamecanica.domain.material;

public class MaterialNotFoundException extends RuntimeException {
    private final Long id;

    public MaterialNotFoundException(Long id) {
        super("Material não encontrado com o ID: " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
