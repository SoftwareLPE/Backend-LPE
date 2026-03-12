package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateFlexsurServiceDriverAssignmentRequestDTO;
import com.example.backend_sistema_LPE.dto.FlexsurServiceDriverAssignmentDTO;
import com.example.backend_sistema_LPE.dto.UpdateFlexsurServiceDriverAssignmentRequestDTO;

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
