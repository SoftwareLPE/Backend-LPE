package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateFlexsurDriverAssignmentRequestDTO;
import com.example.backend_sistema_LPE.dto.FlexsurDriverAssignmentDTO;
import com.example.backend_sistema_LPE.dto.UpdateFlexsurDriverAssignmentRequestDTO;

import java.util.List;

public interface FlexsurDriverAssignmentService {
    List<FlexsurDriverAssignmentDTO> getAssignments(Long plantId, Boolean active, Long shiftId);

    FlexsurDriverAssignmentDTO createAssignment(CreateFlexsurDriverAssignmentRequestDTO request, Long userId);

    FlexsurDriverAssignmentDTO updateAssignment(Long assignmentId, UpdateFlexsurDriverAssignmentRequestDTO request, Long userId);

    void deleteAssignment(Long assignmentId);
}
