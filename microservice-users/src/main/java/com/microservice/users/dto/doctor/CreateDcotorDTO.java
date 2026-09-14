package com.microservice.users.dto.doctor;

import com.microservice.users.dto.doctor.especialidad.CreateEspecialidad;
import com.microservice.users.dto.doctor.horaiosAtencion.CreateHorarioAtencion;
import com.microservice.users.dto.user.CreateUserWithNoRoleDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record CreateDcotorDTO(

        @Valid
        CreateUserWithNoRoleDTO userRegister,

        String cedula,
        @NotBlank(message = "EL campo sexo no puede ir en blanco")
        String sexo,

        @PastOrPresent(message = "invalid birth date")
        LocalDate fechaNacimiento,

        String telefono,
        String direccion,
        String exequatur,

        @Valid
        List<CreateHorarioAtencion> horarioAtencion,

        @Valid
        Set<CreateEspecialidad> especialidades
) {
}
