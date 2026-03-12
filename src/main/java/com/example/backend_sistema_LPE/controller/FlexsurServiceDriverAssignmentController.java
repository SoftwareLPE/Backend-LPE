package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.CreateFlexsurServiceDriverAssignmentRequestDTO;
import com.example.backend_sistema_LPE.dto.FlexsurServiceDriverAssignmentDTO;
import com.example.backend_sistema_LPE.dto.UpdateFlexsurServiceDriverAssignmentRequestDTO;
import com.example.backend_sistema_LPE.security.UserPrincipal;
import com.example.backend_sistema_LPE.service.FlexsurServiceDriverAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/flexsur-service-assignments")
public class FlexsurServiceDriverAssignmentController {
    private final FlexsurServiceDriverAssignmentService assignmentService;

    public FlexsurServiceDriverAssignmentController(FlexsurServiceDriverAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ResponseEntity<List<FlexsurServiceDriverAssignmentDTO>> getAssignments(@RequestParam Long plantId) {
        return ResponseEntity.ok(assignmentService.getAssignments(plantId));
    }

    @PostMapping
    public ResponseEntity<FlexsurServiceDriverAssignmentDTO> createAssignment(
            @RequestBody CreateFlexsurServiceDriverAssignmentRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignmentService.createAssignment(request, userId));
    }

    @PatchMapping("/{assignmentId}")
    public ResponseEntity<FlexsurServiceDriverAssignmentDTO> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestBody UpdateFlexsurServiceDriverAssignmentRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        return ResponseEntity.ok(assignmentService.updateAssignment(assignmentId, request, userId));
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long assignmentId) {
        assignmentService.deleteAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }
}
