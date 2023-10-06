package com.picpaysimplificado.services.interfaces;

import com.picpaysimplificado.domain.user.User;
import com.picpaysimplificado.dtos.UserDTO;

import java.math.BigDecimal;
import java.util.List;

public interface UserService {
    void validateTransaction(User sender, BigDecimal amount);

    User findUserById(Long id);

    User createUser(UserDTO data);

    List<User> getAllUsers();

    void saveUser(User user);
}
