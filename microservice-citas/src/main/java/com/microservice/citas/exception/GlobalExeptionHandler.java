package com.microservice.citas.exception;

import com.microservice.citas.exception.custom.AntelacionValidationException;
import com.microservice.citas.exception.custom.CancelarCitaException;
import com.microservice.citas.exception.custom.DisponibilidadValidationException;
import com.microservice.citas.exception.custom.HorarioValidationException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExeptionHandler {


    //Validaciones de reglas de negocio handler
    @ExceptionHandler({
            AntelacionValidationException.class,
            CancelarCitaException.class,
            DisponibilidadValidationException.class,
            HorarioValidationException.class
    })
    public ResponseEntity<ErrorResponseDto> handleBussinesRulesExceptions(RuntimeException e,  HttpServletRequest request) {
        String mensaje = (e.getCause()!= null && e.getCause().getMessage()!=null)
                ? e.getCause().getMessage() : e.getMessage();

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Regla de negocio incumplida",
                mensaje,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    //Validations en dtos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> erroresDeValidacion = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            erroresDeValidacion.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Error en campos enviados",
                "Uno o más campos no cumplen con las validaciones requeridas",
                request.getRequestURI(),
                erroresDeValidacion
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    //JSON MAL FORMADO O FORMATO INVÁLIDO (Ej: UUID roto, fecha inválida, en este caso es cuando se recive un parametro que por alguna razon puede
    // ser serializado a json
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Cuerpo de la petición mal formado",
                "El JSON enviado contiene errores de sintaxis, valores no reconocidos o formatos inválidos.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // TIPO DE DATO INVÁLIDO EN URL (@PathVariable / @RequestParam)
    // Ejemplo: Mandar texto normal en un @PathVariable UUID
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String tipoRequerido = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido";
        String mensaje = String.format("El parámetro '%s' con valor '%s' no tiene el formato esperado (%s).",
                ex.getName(),
                Objects.toString(ex.getValue(), "null"),
                tipoRequerido);

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Parámetro de URL inválido",
                mensaje,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    //VALIDACIONES DIRECTAS EN EL CONTROLLER (@NotNull, @Pattern, etc. en @PathVariable)

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ErrorResponseDto> handleConstraintViolations(
            Exception ex,
            HttpServletRequest request
    ) {
        Map<String, String> errores = new HashMap<>();

        if (ex instanceof ConstraintViolationException cve) {
            cve.getConstraintViolations().forEach(violation -> {
                String propiedad = violation.getPropertyPath().toString();
                // Extraer solo el nombre del parámetro
                String campo = propiedad.substring(propiedad.lastIndexOf('.') + 1);
                errores.put(campo, violation.getMessage());
            });
        } else if (ex instanceof HandlerMethodValidationException hmve) {
            hmve.getValueResults().forEach(result -> {
                String paramName = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().forEach(err ->
                        errores.put(paramName, err.getDefaultMessage())
                );
            });
        }

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Parámetros de ruta o consulta inválidos",
                "Uno o más parámetros en la URL no cumplen con las restricciones",
                request.getRequestURI(),
                errores
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    //Entity Not found exception
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFoundException(EntityNotFoundException e, HttpServletRequest request) {
        String mensaje = (e.getCause()!= null && e.getCause().getMessage()!=null)
                ? e.getCause().getMessage() : e.getMessage();

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                "Regla de negocio incumplida",
                mensaje,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
