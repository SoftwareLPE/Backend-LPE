package com.example.backend_sistema_LPE.model;

import com.example.backend_sistema_LPE.enums.ShiftType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "shifts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"plant_id", "shift_name"})
)
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shiftId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @Column(name = "shift_name", nullable = false)
    private String shiftName;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type")
    private ShiftType shiftType = ShiftType.REGULAR;

    @ElementCollection
    @CollectionTable(
            name = "shift_days",
            joinColumns = @JoinColumn(name = "shift_id")
    )
    @Column(name = "day_key", nullable = false)
    private Set<String> dayKeys = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "shift_long_week_days",
            joinColumns = @JoinColumn(name = "shift_id")
    )
    @Column(name = "day_key", nullable = false)
    private Set<String> longWeekDayKeys = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "shift_short_week_days",
            joinColumns = @JoinColumn(name = "shift_id")
    )
    @Column(name = "day_key", nullable = false)
    private Set<String> shortWeekDayKeys = new HashSet<>();

    @ManyToMany(mappedBy = "shifts")
    private Set<Driver> drivers = new HashSet<>();
}
