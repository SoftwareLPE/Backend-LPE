package com.example.backend_sistema_LPE.security;

import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username);

        if (user==null){
            throw new UsernameNotFoundException("Este usuario no existe en la base de datos");
        }
        return new UserPrincipal(user);
    }
}
