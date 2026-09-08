package com.microservice.users.projections.pacientes;

import java.time.LocalDate;
import java.util.UUID;

public interface DetallesPacienteProjection {
    UUID getIdPaciente();
    String getNombre();
    String getApellido();
    String getEmail();
    String getCedula();
    String getDireccion();
    String getSexo();
    LocalDate getFechaNacimiento();
    String getTelefono();

}
