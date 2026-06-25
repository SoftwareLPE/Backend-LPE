package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.session_wialon;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.session_wialon.dto.WialonTokenLoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class WialonAuthClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String token;

    public WialonAuthClient(
            @Value("${wialon.api.base-url}") String baseUrl,
            @Value("${wialon.api.token}") String token
    ) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        this.token = token;
    }

    public String tokenLogin() {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("WIALON_API_TOKEN is empty. Configure wialon.api.token.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("svc", "token/login");
        body.add("params", "{\"token\":\"" + escapeJson(token) + "\"}");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        WialonTokenLoginResponse response = restTemplate.postForObject(
                baseUrl,
                request,
                WialonTokenLoginResponse.class
        );

        if (response == null || response.getEid() == null || response.getEid().isBlank()) {
            throw new IllegalStateException("Wialon token/login did not return eid.");
        }

        return response.getEid();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
