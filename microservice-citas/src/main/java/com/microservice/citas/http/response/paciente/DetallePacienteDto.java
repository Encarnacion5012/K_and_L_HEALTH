package com.microservice.citas.http.response.paciente;

import java.time.LocalDate;
import java.util.UUID;

public record DetallePacienteDto(
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
