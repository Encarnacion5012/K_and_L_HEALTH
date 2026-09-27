package com.microservice.users.exception.custom;

public class PersistenceCompensatedException extends RuntimeException {
    //Para cuando falla el guardado o actualización en PostgreSQL (ej. cédula o exequátur duplicado), pero el rollback/compensación en Keycloak se ejecutó con éxito.
    public PersistenceCompensatedException(String message) {
        super(message);
    }

    public PersistenceCompensatedException(String message, Throwable cause) {
        super(message, cause);
    }
}