package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report;

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

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WialonReportClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public WialonReportClient(
            ObjectMapper objectMapper,
            @Value("${wialon.api.base-url}") String baseUrl
    ) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
    }

    public JsonNode cleanupResult(String sid) {
        Map<String, Object> params = new LinkedHashMap<>();
        return executeService("report/cleanup_result", params, sid);
    }

    public JsonNode execReport(
            String sid,
            Long resourceId,
            Long templateId,
            Long objectId,
            Long objectSecId,
            long intervalFrom,
            long intervalTo
    ) {
        Map<String, Object> interval = new LinkedHashMap<>();
        interval.put("from", intervalFrom);
        interval.put("to", intervalTo);
        interval.put("flags", 0);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("reportResourceId", resourceId);
        params.put("reportTemplateId", templateId);
        params.put("reportObjectId", objectId);
        params.put("reportObjectSecId", objectSecId);
        params.put("interval", interval);
        params.put("remoteExec", 0);

        return executeService("report/exec_report", params, sid);
    }

    public JsonNode selectResultRows(
            String sid,
            int tableIndex,
            int from,
            int to
    ) {
        Map<String, Object> configData = new LinkedHashMap<>();
        configData.put("from", from);
        configData.put("to", to);
        configData.put("level", 2);
        configData.put("flat", 0);
        configData.put("rawValues", 1);
        configData.put("unitInfo", 1);

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("type", "range");
        config.put("data", configData);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("tableIndex", tableIndex);
        params.put("config", config);

        return executeService("report/select_result_rows", params, sid);
    }

    public JsonNode getResultRows(
            String sid,
            int tableIndex,
            int from,
            int to
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("tableIndex", tableIndex);
        params.put("indexFrom", from);
        params.put("indexTo", to);

        return executeService("report/get_result_rows", params, sid);
    }

    private JsonNode executeService(String service, Map<String, Object> params, String sid) {
        if (sid == null || sid.isBlank()) {
            throw new IllegalArgumentException("sid is required");
        }

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
