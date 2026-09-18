package com.microservice.users.dto.paciente;

import com.microservice.users.dto.user.CreateUserWithNoRoleDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record CreatePacienteDto(
    @Valid
    CreateUserWithNoRoleDto userRegisterDto,


    String cedula,
    @NotBlank(message = "EL campo sexo no puede ir en blanco")
    String sexo,

    @PastOrPresent(message = "invalid birth date")
    LocalDate fechaNacimiento,

    String telefono,
    String direccion



) {
}
