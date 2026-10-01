package com.microservice.citas.http.response.doctor;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DetalleHorarioAtencionDto(
        DayOfWeek diaDeSemana,
        LocalTime horaInicio,
        LocalTime horaFin,
        Integer duracionCitaMinutos
){
}
