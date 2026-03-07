package com.example.backend_sistema_LPE.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "flexsur_detail")
public class FlexsurDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flexsur_week_id", nullable = false)
    private FlexsurWeek flexsurWeek;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "trips", nullable = false)
    private Integer trips = 0;

    @Column(name = "extra_column", nullable = false)
    private Integer extraColumn = 0;

    @Column(name = "total", nullable = false)
    private Integer total = 0;
}
