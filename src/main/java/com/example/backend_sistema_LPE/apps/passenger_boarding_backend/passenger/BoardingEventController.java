package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger.dto.BoardingEventViewResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class BoardingEventController {

    private final BoardingEventQueryService boardingEventQueryService;

    public BoardingEventController(BoardingEventQueryService boardingEventQueryService) {
        this.boardingEventQueryService = boardingEventQueryService;
    }

    @GetMapping
    public ResponseEntity<List<BoardingEventViewResponse>> findTrips(
            @RequestParam(required = false) Long plantId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long resolvedShiftId,
            @RequestParam(required = false) String windowType,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to
    ) {
        List<BoardingEventViewResponse> response = boardingEventQueryService.findTrips(
                plantId,
                unitId,
                resolvedShiftId,
                windowType,
                from,
                to
        );
        return ResponseEntity.ok(response);
    }
}
