package com.example.backend_sistema_LPE.apps.trip_cascade_backend.route;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<List<RouteDTO>> getRoutesByPlant(@RequestParam Long plantId) {
        return ResponseEntity.ok(routeService.getRoutesByPlant(plantId));
    }

    @PostMapping
    public ResponseEntity<RouteDTO> createRoute(@RequestBody CreateRouteRequestDTO request) {
        return ResponseEntity.ok(routeService.createRoute(request));
    }

    @PutMapping("/{routeId}")
    public ResponseEntity<RouteDTO> updateRoute(
            @PathVariable Long routeId,
            @RequestBody UpdateRouteRequestDTO request
    ) {
        return ResponseEntity.ok(routeService.updateRoute(routeId, request));
    }

    @DeleteMapping("/{routeId}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long routeId) {
        routeService.deleteRoute(routeId);
        return ResponseEntity.noContent().build();
    }
}
