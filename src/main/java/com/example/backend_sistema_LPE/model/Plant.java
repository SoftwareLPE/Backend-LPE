package com.example.backend_sistema_LPE.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "plants")
public class Plant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long plantId;
    private String plantName;
    private String location;

    //Una planta puede tener varios choferes
    @OneToMany(mappedBy = "plant")
    @JsonIgnore
    private List<Driver> drivers;

    @ManyToOne
    @JoinColumn(name = "companyId",nullable = false)
    @JsonIgnore   // 👈 IMPORTANTE
    private Company company;
}
