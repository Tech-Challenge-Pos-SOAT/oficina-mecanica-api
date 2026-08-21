package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.application.materialtransaction.MaterialTransactionRepository;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class StockEntryUseCase {
    private final MaterialRepository materialRepository;
    private final MaterialTransactionRepository transactionRepository;

    public StockEntryUseCase(
            MaterialRepository materialRepository,
            MaterialTransactionRepository transactionRepository
    ) {
        this.materialRepository = materialRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Material execute(long materialId, Integer quantity) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId));

        material.addStock(quantity);

        Material updatedMaterial = materialRepository.save(material);

        MaterialTransaction transaction = new MaterialTransaction(
                null,
                materialId,
                null,
                quantity,
                TransactionType.IN,
                Instant.now()
        );
        transactionRepository.save(transaction);

        return updatedMaterial;
    }
}
