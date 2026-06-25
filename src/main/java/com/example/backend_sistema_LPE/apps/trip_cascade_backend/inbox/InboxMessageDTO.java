package com.example.backend_sistema_LPE.apps.trip_cascade_backend.inbox;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InboxMessageDTO {
    private String id;
    private Long cascadaId;
    private String title;
    private String subtitle;
    private String updatedAt;
    private String receivedTime;
    private String fileName;
    private String sheetTitle;
    private String companyName;
    private String sentBy;
    private Long plantId;
    private String weekStartDate;
    private String weekDate;
    private List<String> shiftIds;
    private List<String> dayKeys;
    private String status;
}
