package com.example.backend_sistema_LPE.apps.shared.user;


import org.springframework.data.domain.Page;

public interface UserAdminService {

    Page<UserAdminTableRowDTO> getUsersForTable(
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

    UserTableDTO updateUserActive(Long userId, Boolean active, Long currentUserId);

}
