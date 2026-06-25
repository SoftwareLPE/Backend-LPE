package com.example.backend_sistema_LPE.apps.trip_cascade_backend.route;

import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.example.backend_sistema_LPE.apps.shared.plant.PlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteServiceImpl implements RouteService {
    private final RouteRepository routeRepository;
    private final PlantRepository plantRepository;

    public RouteServiceImpl(RouteRepository routeRepository, PlantRepository plantRepository) {
        this.routeRepository = routeRepository;
        this.plantRepository = plantRepository;
    }

    @Override
    public List<RouteDTO> getRoutesByPlant(Long plantId) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        return routeRepository.findByPlantPlantId(plantId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public RouteDTO createRoute(CreateRouteRequestDTO request) {
        if (request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getRouteName() == null || request.getRouteName().trim().isBlank()) {
            throw new RuntimeException("routeName is required");
        }
        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));

        Route route = new Route();
        route.setPlant(plant);
        route.setRouteName(request.getRouteName().trim());
        route.setLocation(trimToNull(request.getLocation()));

        return toDTO(routeRepository.save(route));
    }

    @Override
    @Transactional
    public RouteDTO updateRoute(Long routeId, UpdateRouteRequestDTO request) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (request.getRouteName() != null && !request.getRouteName().trim().isBlank()) {
            route.setRouteName(request.getRouteName().trim());
        }
        if (request.getLocation() != null) {
            route.setLocation(trimToNull(request.getLocation()));
        }

        return toDTO(routeRepository.save(route));
    }

    @Override
    @Transactional
    public void deleteRoute(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        routeRepository.delete(route);
    }

    private RouteDTO toDTO(Route route) {
        return new RouteDTO(
                route.getRouteId(),
                route.getRouteName(),
                route.getLocation()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
