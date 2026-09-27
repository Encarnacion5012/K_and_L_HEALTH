package com.microservice.users.exception.custom;

public class AuthIntegrationException extends RuntimeException {
   //Para cuando Keycloak falla al crear el usuario (devuelve null), al obtener el snapshot, al actualizar datos o al activar/desactivar.

    public AuthIntegrationException(String message) {
        super(message);
    }

    public AuthIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}