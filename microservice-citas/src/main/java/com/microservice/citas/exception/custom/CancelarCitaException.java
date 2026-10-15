package com.microservice.citas.exception.custom;

public class CancelarCitaException extends RuntimeException {
    public CancelarCitaException(String message) {
        super(message);
    }
    public CancelarCitaException(String message, Throwable cause) {}
}
