package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.model.LoginRequest;
import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<User> getUsers(){
        return userService.getUsers();
    }
    @GetMapping("/users{userId}")
    public User getUserById(@PathVariable("userId") Long userId){
        return userService.getUserById(userId);
    }
    @PutMapping("/user{userId}")
    public User updateUser(@RequestBody() User user,@PathVariable("userId")Integer userId){
        return userService.updateUser(user);
    }
    @PostMapping("/register")
    public ResponseEntity<User> newUser(@RequestBody() User user){
        User newUser = userService.addUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }
    @DeleteMapping("/user/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId){
        userService.deleteUser(userId);
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try{
            boolean isAuthenticated = userService.authenticate(loginRequest.getUserName(),loginRequest.getPassword());

            if (isAuthenticated){
                session.setAttribute("user",loginRequest.getUserName());
                return ResponseEntity.ok("Login exitoso");
            }else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username o password invalido");
            }
        } catch (Exception e) {
            e.printStackTrace(); // muestra el error completo en consola
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

}
