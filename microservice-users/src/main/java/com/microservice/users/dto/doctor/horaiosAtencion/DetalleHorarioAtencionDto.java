package com.microservice.users.dto.doctor.horaiosAtencion;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DetalleHorarioAtencionDto(

        DayOfWeek diaDeSemana,
        LocalTime horaInicio,
        LocalTime horaFin,
        Integer duracionCitaMinutos
) {
}
