package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.application.materialtransaction.MaterialTransactionRepository;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class StockDebitUseCase {
    private final MaterialRepository materialRepository;
    private final MaterialTransactionRepository transactionRepository;

    public StockDebitUseCase(
            MaterialRepository materialRepository,
            MaterialTransactionRepository transactionRepository
    ) {
        this.materialRepository = materialRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Material execute(Long materialId, Long serviceOrderId, Integer quantity) {
        Material material = materialRepository.findByIdForUpdate(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId));

        material.debitStock(quantity);

        Material updatedMaterial = materialRepository.save(material);

        transactionRepository.save(
                MaterialTransaction.out(materialId, serviceOrderId, quantity, Instant.now()));

        return updatedMaterial;
    }
}
