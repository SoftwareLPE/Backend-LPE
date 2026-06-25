package com.example.backend_sistema_LPE.apps.trip_cascade_backend.route;

import java.util.List;

public interface RouteService {
    List<RouteDTO> getRoutesByPlant(Long plantId);
    RouteDTO createRoute(CreateRouteRequestDTO request);
    RouteDTO updateRoute(Long routeId, UpdateRouteRequestDTO request);
    void deleteRoute(Long routeId);
}
