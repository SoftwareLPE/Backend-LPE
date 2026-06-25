package com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/driver-route")
public class DriverRouteController {
    private final DriverRouteService driverRouteService;

    public DriverRouteController(DriverRouteService driverRouteService) {
        this.driverRouteService = driverRouteService;
    }

    @PostMapping("/create-driver")
    public ResponseEntity<DriverRoute> createDriverWithRoute(
            @RequestBody CreateDriverWithRouteDTO createDriverWithRoute) {

        DriverRoute assignment = driverRouteService.createDriverWithRouteAndAssignment(createDriverWithRoute);
        return ResponseEntity.ok(assignment);
    }
}
