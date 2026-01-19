package com.example.backend_sistema_LPE.service;


import com.example.backend_sistema_LPE.dto.*;
import com.example.backend_sistema_LPE.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public interface UserAdminService {

    Page<UserTableDTO> getUsersForTable(
            String q,
            Long roleId,
            Boolean active,
            int page,
            int size
    );

    UserTableDTO createUser(CreateUserRequestDTO createUserRequestDTO);

    UserTableDTO updateUser(Long userId, UpdateUserRequestDTO updateUserRequestDTO);

    UserDetailDTO getUserDetail(Long userId);

    void deleteUser(Long userId, Long currentUserId);

}
