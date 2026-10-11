package com.microservice.citas.dto;

import com.microservice.citas.model.Estado_Cita;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

public record DetalleCitaDto(
        UUID idCita,
        UUID idPaciente,
        String nombre_Paciente,
        String apellido_Paciente,
        String cedula_Paciente,
        UUID idDoctor,
        String nombre_Doctor,
        String apellido_Doctor,
        Set<String> especialidades_Doctor,
        ZonedDateTime fechaHoraCita,
        String motivo,
        Estado_Cita estado

) {
}
