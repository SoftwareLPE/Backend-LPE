package com.example.backend_sistema_LPE.model;

import com.example.backend_sistema_LPE.enums.CascadaStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "cascada_standard_week",
        uniqueConstraints = @UniqueConstraint(columnNames = {"plant_id", "week_start_date", "shift_id"})
)
public class CascadaStandardWeek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cascadaStandardWeekId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "shift_id", nullable = false)
    private String shiftId;

    @Enumerated(EnumType.STRING)
    private CascadaStatus status;

    private LocalDateTime sentAt;
    private Long sentByUserId;
    private LocalDateTime updatedAt;
    private Long updatedByUserId;
}
