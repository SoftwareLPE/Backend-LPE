package com.example.backend_sistema_LPE.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String name;
    private String lastName;
    private String userName;
    private String email;
    private String password;
    private String phone;
    private Boolean active;
    private Date createAt;
    private String passwordHash;

    @OneToMany(mappedBy = "user")
    private Set<UserCompany> userCompanies = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private Set<UserPlant> userPlants = new HashSet<>();


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;



}
