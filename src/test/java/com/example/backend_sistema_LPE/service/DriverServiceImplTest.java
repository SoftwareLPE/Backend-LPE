package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateDriverWithRouteDTO;
import com.example.backend_sistema_LPE.dto.DriverViewDTO;
import com.example.backend_sistema_LPE.enums.DriverType;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.DriverPlantAssignment;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.Route;
import com.example.backend_sistema_LPE.model.Shift;
import com.example.backend_sistema_LPE.repository.DriverPlantAssignmentRepository;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.DriverRouteRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.RouteRepository;
import com.example.backend_sistema_LPE.repository.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {

    @Mock
    private DriverRepository driverRepository;
    @Mock
    private PlantRepository plantRepository;
    @Mock
    private DriverRouteRepository driverRouteRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private ShiftRepository shiftRepository;
    @Mock
    private DriverPlantAssignmentRepository driverPlantAssignmentRepository;

    private DriverServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DriverServiceImpl(
                driverRepository,
                plantRepository,
                driverRouteRepository,
                routeRepository,
                shiftRepository,
                driverPlantAssignmentRepository
        );
    }

    @Test
    void createDriver_requiresLastName() {
        CreateDriverWithRouteDTO request = new CreateDriverWithRouteDTO();
        request.setLastName(" ");

        assertThatThrownBy(() -> service.createDriver(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Last name is required");
    }

    @Test
    void createDriver_failsWhenPlantMissing() {
        CreateDriverWithRouteDTO request = baseRequest();
        request.setPlantId(10L);

        when(plantRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createDriver(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Plant not found");
    }

    @Test
    void createDriver_failsWhenRouteNotFound() {
        CreateDriverWithRouteDTO request = baseRequest();
        request.setPlantId(10L);
        request.setRouteId(99L);

        Plant plant = new Plant();
        plant.setPlantId(10L);

        when(plantRepository.findById(10L)).thenReturn(Optional.of(plant));
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createDriver(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Route not found");
    }

    @Test
    void createDriver_failsWhenRouteNotInPlant() {
        CreateDriverWithRouteDTO request = baseRequest();
        request.setPlantId(10L);
        request.setRouteId(99L);

        Plant plant = new Plant();
        plant.setPlantId(10L);
        Plant otherPlant = new Plant();
        otherPlant.setPlantId(20L);

        Route route = new Route();
        route.setRouteId(99L);
        route.setPlant(otherPlant);

        when(plantRepository.findById(10L)).thenReturn(Optional.of(plant));
        when(routeRepository.findById(99L)).thenReturn(Optional.of(route));

        assertThatThrownBy(() -> service.createDriver(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Route does not belong to plant");
    }

    @Test
    void createDriver_savesAssignmentsAndShifts() {
        CreateDriverWithRouteDTO request = baseRequest();
        request.setPlantId(10L);
        request.setRouteId(99L);
        request.setShiftIds(Set.of(1L, 2L));

        Plant plant = new Plant();
        plant.setPlantId(10L);

        Route route = new Route();
        route.setRouteId(99L);
        route.setPlant(plant);

        Shift shift1 = new Shift();
        shift1.setShiftId(1L);
        Shift shift2 = new Shift();
        shift2.setShiftId(2L);

        when(plantRepository.findById(10L)).thenReturn(Optional.of(plant));
        when(routeRepository.findById(99L)).thenReturn(Optional.of(route));
        when(shiftRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(shift1, shift2));

        service.createDriver(request);

        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());
        Driver savedDriver = driverCaptor.getValue();
        assertThat(savedDriver.getPlant()).isEqualTo(plant);
        assertThat(savedDriver.getLastName()).isEqualTo("Perez");
        assertThat(savedDriver.getShifts()).hasSize(2);

        ArgumentCaptor<DriverPlantAssignment> assignmentCaptor = ArgumentCaptor.forClass(DriverPlantAssignment.class);
        verify(driverPlantAssignmentRepository).save(assignmentCaptor.capture());
        DriverPlantAssignment assignment = assignmentCaptor.getValue();
        assertThat(assignment.getPlant()).isEqualTo(plant);
        assertThat(assignment.getRoute()).isEqualTo(route);
        assertThat(assignment.getDriverType()).isEqualTo(DriverType.TITULAR);
    }

    @Test
    void getDriversByPlant_filtersActiveAndSortsByRouteName() {
        Plant plant = new Plant();
        plant.setPlantId(10L);

        Driver driver1 = new Driver();
        driver1.setDriverId(1L);
        driver1.setDriverName("Ana");
        driver1.setLastName("Lopez");
        driver1.setActive(true);

        Driver driver2 = new Driver();
        driver2.setDriverId(2L);
        driver2.setDriverName("Beto");
        driver2.setLastName("Diaz");
        driver2.setActive(false);

        Route routeA = new Route();
        routeA.setRouteName("R01");
        Route routeB = new Route();
        routeB.setRouteName("R10");

        DriverPlantAssignment assignment1 = new DriverPlantAssignment();
        assignment1.setDriver(driver1);
        assignment1.setPlant(plant);
        assignment1.setRoute(routeB);
        assignment1.setDriverType(DriverType.TITULAR);

        DriverPlantAssignment assignment2 = new DriverPlantAssignment();
        assignment2.setDriver(driver2);
        assignment2.setPlant(plant);
        assignment2.setRoute(routeA);
        assignment2.setDriverType(DriverType.EXTRA);

        when(driverPlantAssignmentRepository.findByPlantPlantId(10L))
                .thenReturn(List.of(assignment1, assignment2));

        List<DriverViewDTO> result = service.getDriversByPlant(10L, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDriverId()).isEqualTo(1L);
        assertThat(result.get(0).getRouteName()).isEqualTo("R10");
    }

    @Test
    void getDriversByShift_requiresShift() {
        when(shiftRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDriversByShift(5L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Shift not found");
    }

    @Test
    void getDriversByShift_returnsRouteWhenAssigned() {
        Shift shift = new Shift();
        shift.setShiftId(5L);
        Plant plant = new Plant();
        plant.setPlantId(10L);
        shift.setPlant(plant);

        Driver driver = new Driver();
        driver.setDriverId(1L);
        driver.setDriverName("Ana");
        driver.setLastName("Lopez");
        driver.setActive(true);

        Route route = new Route();
        route.setRouteName("R02");

        DriverPlantAssignment assignment = new DriverPlantAssignment();
        assignment.setDriver(driver);
        assignment.setPlant(plant);
        assignment.setRoute(route);
        assignment.setDriverType(DriverType.TITULAR);

        when(shiftRepository.findById(5L)).thenReturn(Optional.of(shift));
        when(driverRepository.findByShiftsShiftId(5L)).thenReturn(List.of(driver));
        when(driverPlantAssignmentRepository.findByDriverDriverIdAndPlantPlantId(1L, 10L))
                .thenReturn(Optional.of(assignment));

        List<DriverViewDTO> result = service.getDriversByShift(5L, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRouteName()).isEqualTo("R02");
        assertThat(result.get(0).getDriverType()).isEqualTo(DriverType.TITULAR);
    }

    private CreateDriverWithRouteDTO baseRequest() {
        CreateDriverWithRouteDTO request = new CreateDriverWithRouteDTO();
        request.setDriverName("Juan");
        request.setLastName("Perez");
        request.setActive(true);
        request.setDriverType(DriverType.TITULAR);
        return request;
    }
}
