package com.postech.oficinamecanica.application.materialtransaction;

import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetMaterialTransactionByIdUseCase {
    private final MaterialTransactionRepository repository;

    public GetMaterialTransactionByIdUseCase(MaterialTransactionRepository repository) {
        this.repository = repository;
    }

    public MaterialTransaction execute(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialTransaction", id));
    }
}
