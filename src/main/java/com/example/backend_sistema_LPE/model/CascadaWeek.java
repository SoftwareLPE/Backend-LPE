package com.example.backend_sistema_LPE.model;

import com.example.backend_sistema_LPE.enums.CascadaStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "cascada_week",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"plant_id", "week_date", "shift_id"}
        )
)
public class CascadaWeek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cascadaWeekId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @Column(name = "week_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "shift_id", nullable = false)
    private String shiftId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CascadaStatus status;

    @Lob
    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedByUserId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "sent_by_user_id")
    private Long sentByUserId;
}
