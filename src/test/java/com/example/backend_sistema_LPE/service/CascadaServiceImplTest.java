package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_common.CascadaRecipientRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard.CascadaRowDTO;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard.CascadaSaveRequestDTO;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard.CascadaStandardCellRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard.CascadaStandardManualRowRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard.CascadaStandardServiceImpl;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard.CascadaStandardWeekRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver.DriverPlantAssignmentRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver.DriverRepository;
import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.example.backend_sistema_LPE.apps.shared.plant.PlantRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.route.RouteRepository;
import com.example.backend_sistema_LPE.apps.shared.shift.ShiftService;
import com.example.backend_sistema_LPE.apps.shared.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CascadaServiceImplTest {

    @Mock
    private CascadaStandardWeekRepository weekRepository;
    @Mock
    private CascadaStandardCellRepository cellRepository;
    @Mock
    private CascadaStandardManualRowRepository manualRowRepository;
    @Mock
    private CascadaRecipientRepository cascadaRecipientRepository;
    @Mock
    private PlantRepository plantRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private DriverPlantAssignmentRepository driverPlantAssignmentRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ShiftService shiftService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private CascadaStandardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CascadaStandardServiceImpl(
                weekRepository,
                cellRepository,
                manualRowRepository,
                cascadaRecipientRepository,
                plantRepository,
                driverRepository,
                driverPlantAssignmentRepository,
                routeRepository,
                userRepository,
                shiftService,
                messagingTemplate
        );
    }

    @Test
    void updateCascadaStatus_rejectsInvalidStatus() {
        assertThatThrownBy(() -> service.updateCascadaStatus(
                1L,
                LocalDate.of(2026, 2, 6),
                null,
                null,
                null,
                "2",
                "lun",
                "BAD_STATUS",
                10L,
                List.of(1L)
        )).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid status");
    }

    @Test
    void saveCascada_throwsWhenDriverMissing() {
        CascadaSaveRequestDTO request = new CascadaSaveRequestDTO();
        request.setPlantId(10L);
        request.setWeekDate(LocalDate.of(2026, 2, 6));
        request.setShiftId("2");

        CascadaRowDTO row = new CascadaRowDTO();
        row.setDriverId(99L);
        request.setDays(Map.of("lun", List.of(row)));

        Plant plant = new Plant();
        plant.setPlantId(10L);

        when(plantRepository.findById(10L)).thenReturn(Optional.of(plant));
        when(weekRepository.findByPlantPlantIdAndWeekStartDateAndShiftId(anyLong(), any(LocalDate.class), anyString()))
                .thenReturn(Optional.empty());
        when(driverRepository.findAllById(List.of(99L))).thenReturn(List.of());
        when(routeRepository.findAllById(any())).thenReturn(List.of());

        assertThatThrownBy(() -> service.saveCascada(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Driver not found");
    }
}
