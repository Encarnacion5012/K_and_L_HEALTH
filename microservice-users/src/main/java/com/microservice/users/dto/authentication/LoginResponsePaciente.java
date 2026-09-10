package com.microservice.users.dto.authentication;

import com.microservice.users.model.Roles_Users;

import java.util.Set;

public record LoginResponsePaciente(

        String userName,
        String firtsName,
        String lastName,
        String email,
        Set<Roles_Users> roles,
        String cedula,
        String telefono

) {
}
