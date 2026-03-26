package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/by-role")
    public ResponseEntity<java.util.List<com.example.backend_sistema_LPE.dto.UserRecipientDTO>> getUsersByRole(
            @RequestParam(required = false) String roleKey,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Boolean active
    ) {
        return ResponseEntity.ok(userService.getUsersByRole(roleKey, roleName, active));
    }

    @DeleteMapping("/user/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId){
        userService.deleteUser(userId);
    }
}
