package com.microservice.users.projections.doctor;



import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface DetallesDoctorProjection {
    String getNombre();

    String getApellido();

    LocalDate getFechaNacimiento();

    String getSexo();

    String getCedula();

    String  getTelefono();

    String getExequatur();

    String getEmail();

   List<DetallesHorarioAtencionProjection> getHorarioAtencion();

    Set<DetallesEspecialidadProjection> getEspecialidades();
}
