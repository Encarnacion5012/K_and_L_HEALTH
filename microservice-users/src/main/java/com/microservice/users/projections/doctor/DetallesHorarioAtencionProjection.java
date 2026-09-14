package com.microservice.users.projections.doctor;

import com.microservice.users.model.Doctor;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.DayOfWeek;
import java.time.LocalTime;

public interface DetallesHorarioAtencionProjection {

    DayOfWeek getDiaDeSemana();

    LocalTime getHoraInicio();


    LocalTime getHoraFin();

    Integer getDuracionCitaMinutos();
}
