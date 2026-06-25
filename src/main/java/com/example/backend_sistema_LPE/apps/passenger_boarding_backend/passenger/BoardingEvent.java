package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger_group.PassengerGroup;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.ReportExecution;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit.Unit;
import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "boarding_events", uniqueConstraints = {
                @UniqueConstraint(name = "uk_boarding_event_wialon_row_key", columnNames = {"wialon_row_key"})
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BoardingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardingEventId;

    @ManyToOne
    @JoinColumn(name = "report_execution_id", nullable = false)
    @JsonIgnore
    private ReportExecution reportExecution;

    @ManyToOne
    @JoinColumn(name = "plant_id", nullable = false)
    @JsonIgnore
    private Plant plant;

    @ManyToOne
    @JoinColumn(name = "passenger_group_id", nullable = false)
    @JsonIgnore
    private PassengerGroup passengerGroup;

    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = false)
    @JsonIgnore
    private Unit unit;

    @ManyToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    @JsonIgnore
    private Passenger passenger;

    @Column(length = 50)
    private String rowNumber;

    @Column(length = 20)
    private String shift;

    @Column(nullable = false)
    private Timestamp boardingTime;

    private Timestamp alightingTime;

    @Column(name = "final_time")
    private Timestamp finalTime;

    @Column(length = 100)
    private String duration;

    @Column(length = 500)
    private String startLocationText;

    private Double startLatitude;
    private Double startLongitude;

    @Column(length = 500)
    private String endLocationText;

    private Double endLatitude;
    private Double endLongitude;

    @Column(length = 100)
    private String wialonTagId;

    @Column(name = "wialon_row_key", nullable = false, length = 200)
    private String wialonRowKey;

    @Column(columnDefinition = "TEXT")
    private String rawRowJson;

    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(nullable = false)
    private Timestamp updatedAt;

    @PrePersist
    void onCreate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
