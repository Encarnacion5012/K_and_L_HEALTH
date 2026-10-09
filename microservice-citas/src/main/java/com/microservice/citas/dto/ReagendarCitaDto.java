package com.microservice.citas.dto;

import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;

public record ReagendarCitaDto(

        @Future
        LocalDateTime nuevaFechaHora
) {
}
