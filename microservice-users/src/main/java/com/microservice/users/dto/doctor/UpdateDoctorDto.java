package com.microservice.users.dto.doctor;

import com.microservice.users.dto.user.UpdateUserWithNoRolesDto;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.Set;

public record UpdateDoctorDto(
        @Valid
        UpdateUserWithNoRolesDto userUpdate,

        String telefono,

        Set<Integer> id_Especialidades,

        BigDecimal precioConsulta

) {
}
