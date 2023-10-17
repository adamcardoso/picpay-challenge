package com.picpaysimplificado.services.interfaces;

import com.picpaysimplificado.domain.transaction.Transaction;
import com.picpaysimplificado.domain.user.User;
import com.picpaysimplificado.dtos.TransactionDTO;

import java.math.BigDecimal;

public interface TransactionService {

    Transaction createTransaction(TransactionDTO transactionDTO);
}
