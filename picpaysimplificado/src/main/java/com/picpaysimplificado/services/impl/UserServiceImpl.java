package com.picpaysimplificado.services.impl;

import com.picpaysimplificado.domain.user.User;
import com.picpaysimplificado.domain.user.UserType;
import com.picpaysimplificado.dtos.UserDTO;
import com.picpaysimplificado.exceptions.InsufficientBalanceException;
import com.picpaysimplificado.exceptions.UnauthorizedTransactionException;
import com.picpaysimplificado.exceptions.UserNotFoundException;
import com.picpaysimplificado.repositories.UserRepository;
import com.picpaysimplificado.services.interfaces.UserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void validateTransaction(User sender, BigDecimal amount) {
        if (sender.getUserType() == UserType.MERCHANT){
            throw new UnauthorizedTransactionException("Usuário do tipo lojista não está autorizado a realizar a transação!");
        }

        if(sender.getBalance().compareTo(amount) < 0){
            throw new InsufficientBalanceException("Saldo insuficiente");
        }
    }

    @Override
    public User findUserById(Long id) {
        return this.userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Usuário não encontrado!"));
    }

    @Override
    public User createUser(UserDTO data){
        User newUser = new User(data);
        this.saveUser(newUser);

        return newUser;
    }

    @Override
    public List<User> getAllUsers(){
        return this.userRepository.findAll();
    }

    @Override
    public void saveUser(User user){
        this.userRepository.save(user);
    }
}
