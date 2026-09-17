package com.microservice.users.dto.especialidad;

import jakarta.validation.constraints.NotBlank;

public record CreateEspecialidad(

        @NotBlank(message = "El nombre no debe estar en blano")
        String nombreEspecialidad,
        String descripcion
) {
}
