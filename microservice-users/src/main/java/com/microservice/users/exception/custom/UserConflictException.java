package com.microservice.users.exception.custom;

public class UserConflictException extends RuntimeException {
    //Para cuando Keycloak devuelve 409 porque el username o email ya existe (en lugar del IllegalStateException genérico).
    public UserConflictException(String message) {
        super(message);
    }
}
