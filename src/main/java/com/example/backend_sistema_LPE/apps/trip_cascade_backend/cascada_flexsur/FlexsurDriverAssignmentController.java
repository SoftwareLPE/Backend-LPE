package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import com.example.backend_sistema_LPE.apps.shared.security.UserPrincipal;
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
@RequestMapping("/flexsur-driver-assignments")
public class FlexsurDriverAssignmentController {
    private final FlexsurDriverAssignmentService assignmentService;

    public FlexsurDriverAssignmentController(FlexsurDriverAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ResponseEntity<List<FlexsurDriverAssignmentDTO>> getAssignments(
            @RequestParam Long plantId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long shiftId
    ) {
        return ResponseEntity.ok(assignmentService.getAssignments(plantId, active, shiftId));
    }

    @PostMapping
    public ResponseEntity<FlexsurDriverAssignmentDTO> createAssignment(
            @RequestBody CreateFlexsurDriverAssignmentRequestDTO request,
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
    public ResponseEntity<FlexsurDriverAssignmentDTO> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestBody UpdateFlexsurDriverAssignmentRequestDTO request,
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
