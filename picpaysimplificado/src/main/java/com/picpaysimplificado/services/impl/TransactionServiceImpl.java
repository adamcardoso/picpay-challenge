package com.picpaysimplificado.services.impl;

import com.picpaysimplificado.domain.transaction.Transaction;
import com.picpaysimplificado.domain.user.User;
import com.picpaysimplificado.dtos.TransactionDTO;
import com.picpaysimplificado.exceptions.UnauthorizedTransactionException;
import com.picpaysimplificado.repositories.TransactionRepository;
import com.picpaysimplificado.services.interfaces.TransactionService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final UserServiceImpl userServiceImpl;
    private final TransactionRepository transactionRepository;

    private final RestTemplate restTemplate;

    private final NotificationServiceImpl notificationServiceImpl;

    public TransactionServiceImpl(UserServiceImpl userServiceImpl, TransactionRepository transactionRepository, RestTemplate restTemplate, NotificationServiceImpl notificationServiceImpl) {
        this.userServiceImpl = userServiceImpl;
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
        this.notificationServiceImpl = notificationServiceImpl;
    }

    public Transaction createTransaction(TransactionDTO transactionDTO) {
        User sender = this.userServiceImpl.findUserById(transactionDTO.senderId());
        User receiver = this.userServiceImpl.findUserById(transactionDTO.receiverId());

        userServiceImpl.validateTransaction(sender, transactionDTO.value());

        boolean isAuthorized = this.authorizeTransaction(sender, transactionDTO.value());

        if (!isAuthorized) {
            throw new UnauthorizedTransactionException("Transação não autorizada!");
        }

        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(transactionDTO.value());
        newTransaction.setSender(sender);
        newTransaction.setReceiver(receiver);
        newTransaction.setLocalDateTime(LocalDateTime.now());

        sender.setBalance(sender.getBalance().subtract(transactionDTO.value()));
        receiver.setBalance(receiver.getBalance().add(transactionDTO.value()));

        transactionRepository.save(newTransaction);
        userServiceImpl.saveUser(sender);
        userServiceImpl.saveUser(receiver);

        this.notificationServiceImpl.sendNotification(sender, "Transação realizada com sucesso!");
        this.notificationServiceImpl.sendNotification(receiver, "Transação recebida com sucesso!");

        return newTransaction;
    }

    public boolean authorizeTransaction(User sender, BigDecimal value) {
        ResponseEntity<Map<String, Object>> authorizationResponse = restTemplate.exchange(
                "https://run.mocky.io/v3/8fafdd68-a090-496f-8c9a-3442cf30dae6",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {

                }
        );

        if (authorizationResponse.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = authorizationResponse.getBody();
            if (Objects.nonNull(responseBody)) {
                String message = (String) responseBody.get("message");
                return "Autorizado".equalsIgnoreCase(message);
            }
        }
        return false;
    }

}
