package com.microservice.citas.exception.custom;

public class HorarioValidationException extends RuntimeException {
    public HorarioValidationException(String message) {
        super(message);
    }
}
