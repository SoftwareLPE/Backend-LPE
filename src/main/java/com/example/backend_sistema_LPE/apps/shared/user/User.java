package com.example.backend_sistema_LPE.apps.shared.user;

import com.example.backend_sistema_LPE.apps.shared.role.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    @Column(name = "full_name")
    private String name;
    private String lastName;
    @Column(name = "user_name")
    private String userName;
    private String email;
    @Column(name = "user_password")
    private String password;
    private String phone;
    @Column(name = "user_active")
    private Boolean active;
    private Date createAt;

    @OneToMany(mappedBy = "user")
    private Set<UserCompany> userCompanies = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private Set<UserPlant> userPlants = new HashSet<>();


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;



}
