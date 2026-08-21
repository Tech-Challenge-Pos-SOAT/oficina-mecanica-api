package com.postech.oficinamecanica.application.materialtransaction;

import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;

import java.util.List;
import java.util.Optional;

public interface MaterialTransactionRepository {
    List<MaterialTransaction> findAll(TransactionType type);
    MaterialTransaction save(MaterialTransaction transaction);
    Optional<MaterialTransaction> findById(Long id);
}
