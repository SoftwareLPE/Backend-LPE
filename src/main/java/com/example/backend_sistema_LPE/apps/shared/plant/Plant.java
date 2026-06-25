package com.example.backend_sistema_LPE.apps.shared.plant;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.company.Company;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard.CascadaStandardManualRow;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver.Driver;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur.FlexsurManualRow;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur.FlexsurServiceDriverAssignment;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur.FlexsurWeek;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom.FormatWeek;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom.FormatWeekManualRow;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal.RegalManualRow;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal.RegalWeek;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.route.Route;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
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
    private List<CascadaStandardManualRow> cascadaStandardManualRows;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<RegalWeek> regalWeeks;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<RegalManualRow> regalManualRows;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<FlexsurWeek> flexsurWeeks;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<FlexsurManualRow> flexsurManualRows;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<FlexsurServiceDriverAssignment> flexsurServiceDriverAssignments;

    @ManyToOne
    @JoinColumn(name = "companyId",nullable = false)
    @JsonIgnore
    private Company company;

    private Long wialonId;
    private Long templateId;
    private Timestamp lastSyncedAt;

   @Column(name = "wialon_units_group_id")
    private Long wialonUnitsGroupId;
}
