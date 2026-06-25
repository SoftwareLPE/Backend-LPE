package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import java.util.List;

public interface FlexsurDriverAssignmentService {
    List<FlexsurDriverAssignmentDTO> getAssignments(Long plantId, Boolean active, Long shiftId);

    FlexsurDriverAssignmentDTO createAssignment(CreateFlexsurDriverAssignmentRequestDTO request, Long userId);

    FlexsurDriverAssignmentDTO updateAssignment(Long assignmentId, UpdateFlexsurDriverAssignmentRequestDTO request, Long userId);

    void deleteAssignment(Long assignmentId);
}
