package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.model.User;

import java.util.List;

public interface UserService {

    List<User> getUsers();

    User getUserById(Long userId);

    User addUser(User user);

    User updateUser(User user);

    void deleteUser(Long userId);

    boolean authenticate(String username, String password);
}
