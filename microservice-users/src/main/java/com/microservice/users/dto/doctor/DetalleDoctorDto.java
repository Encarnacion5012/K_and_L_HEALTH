package com.microservice.users.dto.doctor;

import com.microservice.users.dto.doctor.especialidad.DetalleEspecialidadDto;
import com.microservice.users.dto.doctor.horaiosAtencion.DetalleHorarioAtencionDto;
import com.microservice.users.projections.doctor.DetallesEspecialidadProjection;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record DetalleDoctorDto(
        String nombre,
        String apellido,
        LocalDate fechaNacimiento,
        String sexo,
        String cedula,
        String telefono,
        String exequatur,
        String email,
        List<DetalleHorarioAtencionDto> horarioAtencion,
        Set<DetalleEspecialidadDto> especialidades
) {
}
