package com.microservice.users.exception.custom;

public class CriticalRollbackException extends RuntimeException {

    //Para el peor escenario: falló PostgreSQL y también falló la compensación en Keycloak (inconsistencia crítica que requiere revisión en logs).
    public CriticalRollbackException(String message) {
        super(message);
    }

    public CriticalRollbackException(String message, Throwable cause) {
        super(message, cause);
    }
}