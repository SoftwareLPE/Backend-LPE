package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.enums.ReportExecutionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "report_executions")
public class ReportExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportExecutionId;

    @Column(nullable = false)
    private Long reportResourceId;

    @Column(nullable = false)
    private Long reportTemplateId;

    @Column(nullable = false)
    private Long reportObjectId;

    @Column(nullable = false)
    private Long reportObjectSecId;

    @Column(nullable = false)
    private Timestamp intervalFrom;

    @Column(nullable = false)
    private Timestamp intervalTo;

    @Column(nullable = false)
    private Timestamp executedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportExecutionStatus status = ReportExecutionStatus.PENDING;

    private Timestamp finishedAt;
    private int totalRows;
    private int rowCount;

    @Column(length = 255)
    private String requestKey;

    @Column(length = 255)
    private String sidUsed;

    @Column(length = 255)
    private String parserVersion;

    private int durationMs;

    @Column(length = 255)
    private String errorMessage;

}
