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
    @Column(name = "format_catalog_id", nullable = false)
    private Long formatCatalogId;

    @Column(name = "format_type_id")
    private Long formatTypeId;

    //Una planta puede tener varios choferes
    @OneToMany(mappedBy = "plant", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<Driver> drivers;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<Route> routes;


    @OneToMany(mappedBy = "plant", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<FormatWeek> formatWeeks;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<FormatWeekManualRow> formatWeekManualRows;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<RegalWeek> regalWeeks;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<FlexsurWeek> flexsurWeeks;

    @ManyToOne
    @JoinColumn(name = "companyId",nullable = false)
    @JsonIgnore   // 👈 IMPORTANTE
    private Company company;
}
