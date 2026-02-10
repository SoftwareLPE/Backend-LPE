package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.CreateDriverWithRouteDTO;
import com.example.backend_sistema_LPE.dto.DriverViewDTO;
import com.example.backend_sistema_LPE.dto.UpdateDriverActiveDTO;
import com.example.backend_sistema_LPE.dto.UpdateDriverDTO;
import com.example.backend_sistema_LPE.dto.UpdateDriverShiftsDTO;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.service.DriverRouteService;
import com.example.backend_sistema_LPE.service.DriverService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<List<DriverViewDTO>> getDriverByPlant(
            @PathVariable Long plantId,
            @RequestParam(required = false) Boolean active,
            Authentication authentication
    ){
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()));
        Boolean effectiveActive = isAdmin ? active : Boolean.TRUE;
        List<DriverViewDTO> drivers = driverService.getDriversByPlant(plantId, effectiveActive);
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/by-shift/{shiftId}")
    public ResponseEntity<List<DriverViewDTO>> getDriversByShift(
            @PathVariable Long shiftId,
            @RequestParam(required = false) Boolean active,
            Authentication authentication
    ) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()));
        Boolean effectiveActive = isAdmin ? active : Boolean.TRUE;
        List<DriverViewDTO> drivers = driverService.getDriversByShift(shiftId, effectiveActive);
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

    @PatchMapping("/{driverId}/active")
    public ResponseEntity<Driver> updateDriverActive(
            @PathVariable Long driverId,
            @RequestBody UpdateDriverActiveDTO updateDriverActiveDTO
    ) {
        Driver updated = driverService.updateDriverActive(driverId, updateDriverActiveDTO.getActive());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{driverId}/shifts")
    public ResponseEntity<Driver> updateDriverShifts(
            @PathVariable Long driverId,
            @RequestBody UpdateDriverShiftsDTO updateDriverShiftsDTO
    ) {
        Driver updated = driverService.updateDriverShifts(driverId, updateDriverShiftsDTO.getShiftIds());
        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("{driverId}")
    public void deleteUser(@PathVariable("driverId") Long driverId){
        driverService.deleteDriver(driverId);
    }
}
