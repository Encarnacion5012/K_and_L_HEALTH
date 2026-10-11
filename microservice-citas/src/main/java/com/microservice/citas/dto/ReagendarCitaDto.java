package com.microservice.citas.dto;

import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public record ReagendarCitaDto(

        @Future
        ZonedDateTime nuevaFechaHora
) {
}
