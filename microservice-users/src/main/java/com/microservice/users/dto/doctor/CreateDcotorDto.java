package com.microservice.users.dto.doctor;

import com.microservice.users.dto.doctor.horaiosAtencion.CreateHorarioAtencionDto;
import com.microservice.users.dto.user.CreateUserWithNoRoleDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateDcotorDto(

        @Valid
        CreateUserWithNoRoleDto userRegister,

        String cedula,
        @NotBlank(message = "EL campo sexo no puede ir en blanco")
        String sexo,

        @PastOrPresent(message = "invalid birth date")
        LocalDate fechaNacimiento,

        String telefono,
        String direccion,
        String exequatur,

        @NotNull(message = "El campo precio Consulta no puede ser Null")
        BigDecimal precioConsulta,

        @Valid
        List<CreateHorarioAtencionDto> horarioAtencion,

        List<Integer> idEspecialidades
) {
}
