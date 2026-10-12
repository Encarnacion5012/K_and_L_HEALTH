package com.microservice.users.dto.doctor;

import com.microservice.users.dto.especialidad.DetalleEspecialidadDto;
import com.microservice.users.dto.doctor.horaiosAtencion.DetalleHorarioAtencionDto;

import java.math.BigDecimal;
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
        BigDecimal precioConsulta,
        String email,
        List<DetalleHorarioAtencionDto> horarioAtencion,
        Set<DetalleEspecialidadDto> especialidades
) {
}
