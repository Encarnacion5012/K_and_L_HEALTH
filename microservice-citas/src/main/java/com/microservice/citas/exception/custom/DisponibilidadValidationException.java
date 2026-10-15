package com.microservice.citas.exception.custom;

public class DisponibilidadValidationException extends RuntimeException {
    public DisponibilidadValidationException(String message) {
        super(message);
    }
}
