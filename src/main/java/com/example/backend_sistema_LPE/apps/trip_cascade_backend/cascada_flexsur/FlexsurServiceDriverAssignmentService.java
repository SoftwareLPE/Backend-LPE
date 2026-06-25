package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import java.util.List;

public interface FlexsurServiceDriverAssignmentService {
    List<FlexsurServiceDriverAssignmentDTO> getAssignments(Long plantId);

    FlexsurServiceDriverAssignmentDTO createAssignment(CreateFlexsurServiceDriverAssignmentRequestDTO request, Long userId);

    FlexsurServiceDriverAssignmentDTO updateAssignment(
            Long assignmentId,
            UpdateFlexsurServiceDriverAssignmentRequestDTO request,
            Long userId
    );

    void deleteAssignment(Long assignmentId);
}
