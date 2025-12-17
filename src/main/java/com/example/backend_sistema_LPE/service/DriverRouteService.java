package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateDriverWithRouteDTO;
import com.example.backend_sistema_LPE.dto.DriverViewDTO;
import com.example.backend_sistema_LPE.dto.UpdateDriverDTO;
import com.example.backend_sistema_LPE.enums.DriverType;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.DriverRoute;
import lombok.extern.java.Log;

import java.util.List;

public interface DriverRouteService {

    DriverRoute createDriverWithRouteAndAssignment(
            CreateDriverWithRouteDTO createDriverWithRouteDTO
    );

    DriverViewDTO updateDriverWithAssignment(Long driverId, UpdateDriverDTO updateDriverDTO);


}
