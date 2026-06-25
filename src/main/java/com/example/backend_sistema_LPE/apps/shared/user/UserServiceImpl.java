package com.example.backend_sistema_LPE.apps.shared.user;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository,BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User addUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public boolean authenticate(String username, String password){
        User user = userRepository.findByUserName(username);

        if(!user.getUserName().equals(username)){
            throw new UsernameNotFoundException("El user no exist");
        }
        if(!bCryptPasswordEncoder.matches(password, user.getPassword())){
            throw new BadCredentialsException("El password is incorrect");
        }
        return true;
    }

    @Override
    public java.util.List<UserRecipientDTO> getUsersByRole(
            String roleKey,
            String roleName,
            Boolean active
    ) {
        if (roleKey != null && !roleKey.trim().isEmpty()) {
            return userRepository.findRecipientsByRoleKey(roleKey.trim().toUpperCase(), active);
        }

        if (roleName != null && !roleName.trim().isEmpty()) {
            return userRepository.findRecipientsByRoleName(roleName.trim(), active);
        }

        throw new IllegalArgumentException("roleKey or roleName is required");
    }


}
