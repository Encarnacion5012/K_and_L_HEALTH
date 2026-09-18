package com.microservice.users.dto.paciente;

import com.microservice.users.dto.user.UpdateUserWithNoRolesDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

public record UpdatePacienteDto(
        @Valid
        UpdateUserWithNoRolesDto userUpdate,

        @Pattern(regexp = "^(?!\\s*$).+", message = "The address cannot be blank")
        String direccion,

        @Pattern(regexp = "^(?!\\s*$).+", message = "The genre cannot be blank")
        String sexo,

        @Pattern(regexp = "^(?!\\s*$).+", message = "The phone number cannot be blank")
        String telefono
) {}