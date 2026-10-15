package com.microservice.citas.exception.custom;

public class ReagendarException extends RuntimeException {
    public ReagendarException(String message) {
        super(message);
    }
    public ReagendarException(String message, Throwable cause) {
        super(message, cause);
    }
}
