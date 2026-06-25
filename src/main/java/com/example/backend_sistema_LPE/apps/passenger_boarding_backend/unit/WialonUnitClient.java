package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.WialonApiException;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.session_wialon.SessionWialonService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WialonUnitClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final SessionWialonService sessionWialonService;

    public WialonUnitClient(
            ObjectMapper objectMapper,
            SessionWialonService sessionWialonService,
            @Value("${wialon.api.base-url}") String baseUrl
    ) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.sessionWialonService = sessionWialonService;
    }

    public List<WialonUnitItemDTO> searchUnitsByIds(Set<Long> unitIds) {
        if (unitIds == null || unitIds.isEmpty()) {
            return List.of();
        }

        String sid = sessionWialonService.getOrCreateValidSid();
        try {
            return mapUnits(executeSearchUnitsByIds(sid, unitIds));
        } catch (WialonApiException ex) {
            boolean isInvalidSid = "core/search_items".equals(ex.getService()) && ex.getErrorCode() == 1;
            if (!isInvalidSid) {
                throw ex;
            }

            String refreshedSid = sessionWialonService.forceRefreshSid();
            return mapUnits(executeSearchUnitsByIds(refreshedSid, unitIds));
        }
    }

    private JsonNode executeSearchUnitsByIds(String sid, Set<Long> unitIds) {
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("itemsType", "avl_unit");
        spec.put("propName", "sys_id");
        spec.put("propValueMask", unitIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));
        spec.put("sortType", "sys_name");
        spec.put("propType", "");

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("spec", spec);
        params.put("force", 1);
        params.put("flags", 1);
        params.put("from", 0);
        params.put("to", unitIds.size() - 1);

        return executeService("core/search_items", params, sid);
    }

    private List<WialonUnitItemDTO> mapUnits(JsonNode response) {
        JsonNode items = response.path("items");
        if (!items.isArray()) {
            return List.of();
        }

        List<WialonUnitItemDTO> result = new ArrayList<>();
        for (JsonNode item : items) {
            Long wialonId = item.path("id").isIntegralNumber() ? item.path("id").asLong() : null;
            if (wialonId == null) {
                continue;
            }
            result.add(new WialonUnitItemDTO(
                    wialonId,
                    item.path("nm").asText(null)
            ));
        }
        return result;
    }

    private JsonNode executeService(String service, Map<String, Object> params, String sid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("svc", service);
        body.add("params", toJson(params));
        body.add("sid", sid);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        JsonNode response = restTemplate.postForObject(baseUrl, request, JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("Wialon service " + service + " returned empty response.");
        }

        int errorCode = response.path("error").asInt(0);
        if (errorCode != 0) {
            String reason = response.path("reason").asText("");
            String reasonText = reason.isBlank() ? "(no reason provided)" : reason;
            throw new WialonApiException(service, errorCode, reasonText);
        }

        return response;
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize Wialon params.", e);
        }
    }
}
