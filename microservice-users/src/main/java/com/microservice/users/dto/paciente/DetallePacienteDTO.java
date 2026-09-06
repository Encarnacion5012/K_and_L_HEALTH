package com.microservice.users.dto.paciente;

import java.time.LocalDate;
import java.util.UUID;

public record DetallePacienteDTO(
        UUID idPaciente,
        String nombre,
        String apellido,
        String cedula,
        String direccion,
        String sexo,
        LocalDate fechaNacimiento,
        String telefono
) {
}
