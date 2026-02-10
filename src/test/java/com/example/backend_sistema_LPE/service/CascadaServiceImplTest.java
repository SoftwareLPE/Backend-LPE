package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaRowDTO;
import com.example.backend_sistema_LPE.dto.CascadaSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaWeekResponseDTO;
import com.example.backend_sistema_LPE.enums.CascadaStatus;
import com.example.backend_sistema_LPE.model.CascadaWeek;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.repository.CascadaRecipientRepository;
import com.example.backend_sistema_LPE.repository.CascadaWeekRepository;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CascadaServiceImplTest {

    @Mock
    private CascadaWeekRepository cascadaWeekRepository;
    @Mock
    private CascadaRecipientRepository cascadaRecipientRepository;
    @Mock
    private PlantRepository plantRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private ObjectMapper objectMapper;
    private CascadaServiceImpl service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new CascadaServiceImpl(
                cascadaWeekRepository,
                cascadaRecipientRepository,
                plantRepository,
                driverRepository,
                userRepository,
                messagingTemplate,
                objectMapper
        );
    }

    @Test
    void saveCascada_persistsNormalizedPayload() throws Exception {
        CascadaSaveRequestDTO request = new CascadaSaveRequestDTO();
        request.setPlantId(10L);
        request.setWeekDate(LocalDate.of(2026, 2, 6));
        request.setShiftId("2");

        CascadaRowDTO row = new CascadaRowDTO();
        row.setDriverId(5L);
        row.setE(null);
        row.setS("R01");
        row.setEte(null);
        row.setSte("R02");

        request.setDays(Map.of("lun", List.of(row)));

        Plant plant = new Plant();
        plant.setPlantId(10L);

        Driver driver = new Driver();
        driver.setDriverId(5L);

        when(plantRepository.findById(10L)).thenReturn(Optional.of(plant));
        when(driverRepository.findAllById(List.of(5L))).thenReturn(List.of(driver));
        when(cascadaWeekRepository.findByPlantPlantIdAndWeekStartDateAndShiftId(10L, request.getWeekDate(), "2"))
                .thenReturn(Optional.empty());

        service.saveCascada(request);

        ArgumentCaptor<CascadaWeek> captor = ArgumentCaptor.forClass(CascadaWeek.class);
        verify(cascadaWeekRepository).save(captor.capture());
        CascadaWeek saved = captor.getValue();

        assertThat(saved.getPlant()).isEqualTo(plant);
        assertThat(saved.getShiftId()).isEqualTo("2");
        assertThat(saved.getWeekStartDate()).isEqualTo(request.getWeekDate());
        assertThat(saved.getStatus()).isEqualTo(CascadaStatus.DRAFT);

        JsonNode root = objectMapper.readTree(saved.getPayloadJson());
        JsonNode days = root.get("days");
        assertThat(days).isNotNull();
        JsonNode dayRows = days.get("lun");
        assertThat(dayRows).hasSize(1);
        JsonNode rowNode = dayRows.get(0);
        assertThat(rowNode.get("E").asText()).isEmpty();
        assertThat(rowNode.get("S").asText()).isEqualTo("R01");
        assertThat(rowNode.get("ETE").asText()).isEmpty();
        assertThat(rowNode.get("STE").asText()).isEqualTo("R02");
    }

    @Test
    void getWeekCascadas_returnsItemsForAllDays() throws Exception {
        CascadaRowDTO row = new CascadaRowDTO();
        row.setDriverId(1L);
        row.setE("R01");
        row.setS("R01");
        row.setEte("");
        row.setSte("");

        String payload = objectMapper.writeValueAsString(
                Map.of("days", Map.of(
                        "lun", List.of(row),
                        "mar", List.of(row)
                ))
        );

        CascadaWeek week = new CascadaWeek();
        week.setShiftId("3");
        week.setWeekStartDate(LocalDate.of(2026, 2, 6));
        week.setStatus(CascadaStatus.SENT);
        week.setPayloadJson(payload);

        when(cascadaWeekRepository.findByPlantPlantIdAndWeekStartDateAndStatus(
                12L,
                week.getWeekStartDate(),
                CascadaStatus.SENT
        )).thenReturn(List.of(week));

        CascadaWeekResponseDTO response = service.getWeekCascadas(
                12L,
                week.getWeekStartDate(),
                CascadaStatus.SENT.name()
        );

        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getItems()).anyMatch(item ->
                item.getShiftId().equals(3L) && item.getDayKey().equals("lun")
        );
        assertThat(response.getItems()).anyMatch(item ->
                item.getShiftId().equals(3L) && item.getDayKey().equals("mar")
        );
    }

    @Test
    void updateCascadaStatus_rejectsInvalidStatus() {
        assertThatThrownBy(() -> service.updateCascadaStatus(
                1L,
                LocalDate.of(2026, 2, 6),
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
        when(driverRepository.findAllById(List.of(99L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.saveCascada(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Driver not found");
    }

    @Test
    void sendCascada_requiresRecipients() {
        CascadaWeek week = new CascadaWeek();
        week.setShiftId("2");
        Plant plant = new Plant();
        plant.setPlantId(10L);
        week.setPlant(plant);

        when(cascadaWeekRepository.findByPlantPlantIdAndWeekStartDateAndShiftId(
                10L,
                LocalDate.of(2026, 2, 6),
                "2"
        )).thenReturn(Optional.of(week));

        assertThatThrownBy(() -> service.sendCascada(
                10L,
                LocalDate.of(2026, 2, 6),
                "2",
                "lun",
                5L,
                List.of()
        )).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("recipientUserIds is required");
    }

    @Test
    void updateCascadaStatus_sentRequiresRecipients() {
        when(cascadaWeekRepository.updateStatusByPlantAndWeekAndShift(
                eq(10L),
                eq(LocalDate.of(2026, 2, 6)),
                eq("2"),
                eq(CascadaStatus.SENT),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(1);

        assertThatThrownBy(() -> service.updateCascadaStatus(
                10L,
                LocalDate.of(2026, 2, 6),
                "2",
                "lun",
                CascadaStatus.SENT.name(),
                5L,
                List.of()
        )).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("recipientUserIds is required");
    }

    @Test
    void getCascada_returnsEmptyWhenStatusMismatch() throws Exception {
        CascadaRowDTO row = new CascadaRowDTO();
        row.setDriverId(1L);
        row.setE("R01");
        row.setS("R01");
        row.setEte("");
        row.setSte("");

        String payload = objectMapper.writeValueAsString(
                Map.of("days", Map.of("lun", List.of(row)))
        );

        CascadaWeek week = new CascadaWeek();
        week.setShiftId("2");
        week.setWeekStartDate(LocalDate.of(2026, 2, 6));
        week.setStatus(CascadaStatus.DRAFT);
        week.setPayloadJson(payload);

        when(cascadaWeekRepository.findByPlantPlantIdAndWeekStartDateAndShiftId(
                10L,
                week.getWeekStartDate(),
                "2"
        )).thenReturn(Optional.of(week));

        var response = service.getCascada(
                10L,
                week.getWeekStartDate(),
                "2",
                "lun",
                CascadaStatus.SENT.name()
        );

        assertThat(response.getRows()).isEmpty();
        assertThat(response.getStatus()).isEqualTo(CascadaStatus.DRAFT.name());
    }
}
