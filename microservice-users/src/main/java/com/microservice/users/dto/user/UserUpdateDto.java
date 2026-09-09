package com.microservice.users.dto.user;

import com.microservice.users.model.Roles_Users;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import java.util.Set;

public record UserUpdateDto(
        @Pattern(regexp = "^(?!\\s*$).+", message = "The username cannot be blank")
        String username,

        @Email(message = "You have to use the correct structure for emails")
        @Pattern(regexp = "^(?!\\s*$).+", message = "The email cannot be blank")
        String email,

        @Pattern(regexp = "^(?!\\s*$).+", message = "The firstname cannot be blank")
        String firstName,

        @Pattern(regexp = "^(?!\\s*$).+", message = "The lastname cannot be blank")
        String lastName,

        Set<Roles_Users> roles
) {}