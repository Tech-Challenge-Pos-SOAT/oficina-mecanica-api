package com.postech.oficinamecanica.application.materialtransaction;

import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;

import java.util.List;

public interface MaterialTransactionRepository {
    List<MaterialTransaction> findAll(TransactionType type);
    MaterialTransaction save(MaterialTransaction transaction);
}
