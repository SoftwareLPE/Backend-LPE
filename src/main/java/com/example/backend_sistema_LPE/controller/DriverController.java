package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.CreateDriverWithRouteDTO;
import com.example.backend_sistema_LPE.dto.DriverViewDTO;
import com.example.backend_sistema_LPE.dto.UpdateDriverDTO;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.service.DriverRouteService;
import com.example.backend_sistema_LPE.service.DriverService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/drivers")
@CrossOrigin(origins ="http://localhost:8081/")
public class DriverController {
    private final DriverService driverService;
    private final DriverRouteService driverRouteService;

    public DriverController(DriverService driverService, DriverRouteService driverRouteService) {
        this.driverService = driverService;
        this.driverRouteService = driverRouteService;
    }

    @GetMapping
    public List<Driver> getAllDrivers(){
        return  driverService.getAllDrivers();
    }

    @GetMapping("/{plantId}")
    public ResponseEntity<List<DriverViewDTO>> getDriverByPlant(@PathVariable Long plantId){
        List<DriverViewDTO> drivers = driverService.getDriversByPlant(plantId);
        return ResponseEntity.ok(drivers);
    }

    @PostMapping("create-driver")
    public ResponseEntity<Void> createDriver(@RequestBody CreateDriverWithRouteDTO driverCreateDTO) {
        driverService.createDriver(driverCreateDTO);
        return ResponseEntity.ok().build();
    }



    @PutMapping("/{driverId}")
    public ResponseEntity<Driver> updateDriver(@PathVariable Long driverId,@RequestBody  Driver driver){
        Driver updatedDriver= driverService.updateDriver(driverId,driver);
        return ResponseEntity.ok(updatedDriver);
    }

    @PatchMapping("/{driverId}")
    public ResponseEntity<DriverViewDTO> updateDriver(
            @PathVariable Long driverId,
            @RequestBody UpdateDriverDTO updateDriverDTO
    ) {
        DriverViewDTO updated = driverRouteService.updateDriverWithAssignment(driverId, updateDriverDTO);
        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("{driverId}")
    public void deleteUser(@PathVariable("driverId") Long driverId){
        driverService.deleteDriver(driverId);
    }
}
