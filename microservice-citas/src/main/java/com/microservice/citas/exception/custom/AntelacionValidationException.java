package com.microservice.citas.exception.custom;

public class AntelacionValidationException extends RuntimeException {
    public AntelacionValidationException(String message) {
        super(message);
    }
    public AntelacionValidationException(String message, Throwable cause) {super(message, cause);}
}
