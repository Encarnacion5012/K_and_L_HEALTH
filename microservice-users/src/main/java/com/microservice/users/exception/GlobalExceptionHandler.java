package com.microservice.users.exception;

import com.microservice.users.exception.custom.AuthIntegrationException;
import com.microservice.users.exception.custom.CriticalRollbackException;
import com.microservice.users.exception.custom.PersistenceCompensatedException;
import com.microservice.users.exception.custom.UserConflictException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Entidades no encontradas (404 NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                "Recurso no encontrado",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 2. Conflicto de usuario existente en Keycloak (409 CONFLICT)
    @ExceptionHandler({UserConflictException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponseDto> handleUserConflict(
            RuntimeException ex, HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                "Conflicto de datos del usuario",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 3. Fallo en BD pero con Rollback exitoso en Keycloak (409 CONFLICT)
    @ExceptionHandler(PersistenceCompensatedException.class)
    public ResponseEntity<ErrorResponseDto> handlePersistenceCompensated(
            PersistenceCompensatedException ex, HttpServletRequest request) {

        log.warn("Operación abortada en BD pero compensada con éxito en Keycloak: {}", ex.getMessage());
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                "Error al procesar datos en base de datos (Operación revertida)",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 4. Error de comunicación o fallo directo en Keycloak (502 BAD_GATEWAY)
    @ExceptionHandler(AuthIntegrationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthIntegration(
            AuthIntegrationException ex, HttpServletRequest request) {

        log.error("Fallo de comunicación con Keycloak: {}", ex.getMessage(), ex);
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_GATEWAY.value(),
                "Error en el proveedor de autenticación",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    // 5. Error CRÍTICO: Falló la BD y falló el Rollback en Keycloak (500 INTERNAL_SERVER_ERROR)
    @ExceptionHandler(CriticalRollbackException.class)
    public ResponseEntity<ErrorResponseDto> handleCriticalRollback(
            CriticalRollbackException ex, HttpServletRequest request) {

        log.error("ALERTA CRÍTICA DE CONSISTENCIA: {}", ex.getMessage(), ex);
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error crítico de sincronización",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // 6. Errores de validación de DTOs con @Valid (400 BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en los campos enviados",
                "Verifique los datos de entrada",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 7. Validación en @PathVariable como @Email en PacienteController (400 BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Parámetro de consulta inválido",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 8. Acceso denegado por @PreAuthorize (403 FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.FORBIDDEN.value(),
                "Acceso denegado",
                "No tienes los permisos necesarios para realizar esta acción",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // 9. Fallback general para cualquier otra excepción no controlada (500 INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Error inesperado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en los campos enviados",
                "Uno o más campos no cumplen con el formato o restricciones requeridas",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}