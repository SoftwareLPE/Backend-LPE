package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report;

public class WialonApiException extends RuntimeException {

    private final String service;
    private final int errorCode;
    private final String reason;

    public WialonApiException(String service, int errorCode, String reason) {
        super("Wialon service " + service + " failed with error " + errorCode + ": " + reason);
        this.service = service;
        this.errorCode = errorCode;
        this.reason = reason;
    }

    public String getService() {
        return service;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getReason() {
        return reason;
    }
}
