package com.postech.oficinamecanica.application.materialtransaction;

import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import com.postech.oficinamecanica.domain.shared.exceptions.InvalidParametersException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListMaterialTransactionsUseCase {
    private final MaterialTransactionRepository repository;

    public ListMaterialTransactionsUseCase(MaterialTransactionRepository repository) {
        this.repository = repository;
    }

    public List<MaterialTransaction> execute(TransactionType typeParam) {
        return repository.findAll(typeParam);
    }
}
