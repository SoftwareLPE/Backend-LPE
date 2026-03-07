package com.example.backend_sistema_LPE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.backend_sistema_LPE.enums.CascadaType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cascada_recipients")
public class CascadaRecipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cascadaRecipientId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @Column(name = "week_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "shift_id", nullable = false)
    private String shiftId;

    @Column(name = "day_key")
    private String dayKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "cascada_type", nullable = false)
    private CascadaType cascadaType;

    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "sent_by_user_id")
    private Long sentByUserId;
}
