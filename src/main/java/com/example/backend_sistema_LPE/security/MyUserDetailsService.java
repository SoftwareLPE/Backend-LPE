package com.example.backend_sistema_LPE.security;

import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username);

        String normalizedUsername = (username == null) ? null : username.trim();

        if (user == null) {
            throw new UsernameNotFoundException("Este usuario no existe en la base de datos");
        }

        if (user.getRole() == null || user.getRole().getRoleName() == null) {
            throw new UsernameNotFoundException("El usuario no tiene rol asignado");
        }

        String roleName = user.getRole().getRoleName().trim().toUpperCase();
        if (roleName.startsWith("ROLE_")) {
            roleName = roleName.substring(5);
        }
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName);




        return new UserPrincipal(
                user.getUserId(),
                user.getUserName(),
                user.getPassword(),
                List.of(authority)
        );

    }
}

