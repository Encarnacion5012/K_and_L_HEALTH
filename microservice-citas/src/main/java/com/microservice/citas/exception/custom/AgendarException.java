package com.microservice.citas.exception.custom;

public class AgendarException extends RuntimeException {
    public AgendarException(String message, Throwable cause) {
        super(message, cause);
    }
    public AgendarException(String message) {}
}
