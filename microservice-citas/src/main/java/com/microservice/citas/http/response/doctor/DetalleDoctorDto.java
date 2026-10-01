package com.microservice.citas.http.response.doctor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record DetalleDoctorDto (
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
