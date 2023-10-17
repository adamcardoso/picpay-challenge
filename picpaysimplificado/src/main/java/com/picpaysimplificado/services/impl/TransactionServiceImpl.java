package com.picpaysimplificado.services.impl;

import com.picpaysimplificado.domain.transaction.Transaction;
import com.picpaysimplificado.domain.user.User;
import com.picpaysimplificado.dtos.TransactionDTO;
import com.picpaysimplificado.exceptions.UnauthorizedTransactionException;
import com.picpaysimplificado.repositories.TransactionRepository;
import com.picpaysimplificado.services.interfaces.TransactionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final UserServiceImpl userServiceImpl;
    private final TransactionRepository transactionRepository;

    private final AuthorizationServiceImpl authorizationServiceImpl;

    private final NotificationServiceImpl notificationServiceImpl;

    public TransactionServiceImpl(UserServiceImpl userServiceImpl, TransactionRepository transactionRepository, AuthorizationServiceImpl authorizationServiceImpl, NotificationServiceImpl notificationServiceImpl) {
        this.userServiceImpl = userServiceImpl;
        this.transactionRepository = transactionRepository;
        this.authorizationServiceImpl = authorizationServiceImpl;
        this.notificationServiceImpl = notificationServiceImpl;
    }

    public Transaction createTransaction(TransactionDTO transactionDTO) {
        User sender = this.userServiceImpl.findUserById(transactionDTO.senderId());
        User receiver = this.userServiceImpl.findUserById(transactionDTO.receiverId());

        validateTransaction(sender, transactionDTO.value());

        boolean isAuthorized = authorizationServiceImpl.authorizeTransaction(sender, transactionDTO.value());

        if (!isAuthorized) {
            throw new UnauthorizedTransactionException("Transação não autorizada!");
        }

        Transaction newTransaction = createNewTransaction(sender, receiver, transactionDTO.value());

        updateSenderAndReceiverBalances(sender, receiver, transactionDTO.value());

        saveTransactionAndUsers(newTransaction, sender, receiver);

        sendNotifications(sender, receiver);

        return newTransaction;
    }

    private Transaction createNewTransaction(User sender, User receiver, BigDecimal value) {
        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(value);
        newTransaction.setSender(sender);
        newTransaction.setReceiver(receiver);
        newTransaction.setLocalDateTime(LocalDateTime.now());
        return newTransaction;
    }

    private void validateTransaction(User sender, BigDecimal value) {
        if (sender.getBalance().compareTo(value) < 0) {
            throw new UnauthorizedTransactionException("Saldo insuficiente para a transação.");
        }
    }

    private void updateSenderAndReceiverBalances(User sender, User receiver, BigDecimal value) {
        sender.setBalance(sender.getBalance().subtract(value));
        receiver.setBalance(receiver.getBalance().add(value));
    }

    private void saveTransactionAndUsers(Transaction transaction, User sender, User receiver) {
        transactionRepository.save(transaction);
        userServiceImpl.saveUser(sender);
        userServiceImpl.saveUser(receiver);
    }

    private void sendNotifications(User sender, User receiver) {
        notificationServiceImpl.sendNotification(sender, "Transação realizada com sucesso!");
        notificationServiceImpl.sendNotification(receiver, "Transação recebida com sucesso!");
    }
}
