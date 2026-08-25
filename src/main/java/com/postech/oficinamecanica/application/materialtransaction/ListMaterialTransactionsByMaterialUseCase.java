package com.postech.oficinamecanica.application.materialtransaction;

import com.postech.oficinamecanica.application.material.MaterialRepository;
import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListMaterialTransactionsByMaterialUseCase {
    private final MaterialRepository materialRepository;
    private final MaterialTransactionRepository transactionRepository;

    public ListMaterialTransactionsByMaterialUseCase(
        MaterialRepository materialRepository,
        MaterialTransactionRepository transactionRepository
    ) {
        this.materialRepository = materialRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<MaterialTransaction> execute(Long materialId, TransactionType type) {
        materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId));

        return transactionRepository.findAllByMaterialId(materialId, type);
    }
}
