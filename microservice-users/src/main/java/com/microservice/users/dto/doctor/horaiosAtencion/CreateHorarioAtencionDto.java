package com.microservice.users.dto.doctor.horaiosAtencion;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record CreateHorarioAtencionDto(
        @NotNull(message = "El día de la semana es obligatorio")
        DayOfWeek diaDeSemana,

        @NotNull(message = "La hora de inicio es obligatoria")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaInicio,

        @NotNull(message = "La hora de fin es obligatoria")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaFin,

        @NotNull(message = "La duración de la cita es obligatoria")
        @Positive(message = "La duración de la cita debe ser mayor a 0 minutos")
        @Min(value = 20, message = "La duración mínima de una cita debe ser de al menos 20 minutos")
        Integer duracionCitaMinutos
) {

    @AssertTrue(message = "La hora de inicio debe ser anterior a la hora de fin")
    public boolean isHorarioValido() {
        if (horaInicio == null || horaFin == null) {
            return true;
        }
        return horaInicio.isBefore(horaFin);
    }
}
