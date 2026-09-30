package com.microservice.citas.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;


import java.time.LocalDateTime;
import java.util.UUID;

public record CreateCitaDto(

        @NotBlank(message = "El id del paciente no puede estar en blaco")
        UUID idPaciente,

        @NotBlank(message = "El id del doctor no puede estar en blaco")
        UUID idDoctor,

        @Future(message = "La cita debe agendarse en una fecha futura")

        LocalDateTime fechaHoaraCita,

        @NotBlank(message = "EL motivo de la cita debe estar especificado")
        String motivo
) {
}
