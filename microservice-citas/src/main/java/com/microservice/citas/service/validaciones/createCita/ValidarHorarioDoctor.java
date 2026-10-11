package com.microservice.citas.service.validaciones.createCita;

import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import com.microservice.citas.http.response.doctor.DetalleHorarioAtencionDto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class ValidarHorarioDoctor implements ValidarCreacionCita{
    @Override
    public void validarCreacionCita(CreateCitaDto createCitaDto, DetalleDoctorDto detalleDoctorDto) {
        DayOfWeek diaCita = createCitaDto.fechaHoraCita().getDayOfWeek();
        LocalTime horaCita = createCitaDto.fechaHoraCita().toLocalTime();

        DetalleHorarioAtencionDto horarioDelDia = detalleDoctorDto.horarioAtencion().stream()
                .filter(h-> h.diaDeSemana().equals(diaCita))
                .findFirst()
                .orElseThrow(()-> new RuntimeException("EL doctor deseado no trabaja los dias: " + diaCita));

        boolean horaValida = !horaCita.isBefore(horarioDelDia.horaInicio()) && !horaCita.isAfter(horarioDelDia.horaFin());

        if (!horaValida) {
            throw new IllegalArgumentException("La hora seleccionada (" + horaCita +
                    ") está fuera del horario de atención del doctor (" +
                    horarioDelDia.horaInicio() + " - " + horarioDelDia.horaFin() + ")");
        }

    }
}
