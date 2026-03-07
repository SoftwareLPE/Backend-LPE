package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateRouteRequestDTO;
import com.example.backend_sistema_LPE.dto.RouteDTO;
import com.example.backend_sistema_LPE.dto.UpdateRouteRequestDTO;

import java.util.List;

public interface RouteService {
    List<RouteDTO> getRoutesByPlant(Long plantId);
    RouteDTO createRoute(CreateRouteRequestDTO request);
    RouteDTO updateRoute(Long routeId, UpdateRouteRequestDTO request);
    void deleteRoute(Long routeId);
}
