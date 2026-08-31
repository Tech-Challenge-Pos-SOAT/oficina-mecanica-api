package com.postech.oficinamecanica.application.material;

import com.postech.oficinamecanica.application.materialtransaction.MaterialTransactionRepository;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Estorno de baixa: devolve ao saldo a peca que tinha saido para uma ordem e
 * registra a movimentacao IN vinculada a essa mesma ordem, para o extrato
 * explicar de onde veio a devolucao.
 */
@Service
public class ReturnStockUseCase {
    private final MaterialRepository materialRepository;
    private final MaterialTransactionRepository transactionRepository;

    public ReturnStockUseCase(MaterialRepository materialRepository,
                              MaterialTransactionRepository transactionRepository) {
        this.materialRepository = materialRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Material execute(Long materialId, Long serviceOrderId, Integer quantity) {
        Material material = materialRepository.findByIdForUpdate(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId));

        material.addStock(quantity);
        Material updated = materialRepository.save(material);

        transactionRepository.save(
                MaterialTransaction.returned(materialId, serviceOrderId, quantity, Instant.now()));

        return updated;
    }
}
