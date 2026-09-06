package com.microservice.users.dto.user;

import com.microservice.users.model.Roles_Users;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public record UserRegisterDto(
        @NotBlank(message = "The username can not be empty or null")
        String username,

        @NotBlank(message = "The email can not be empty or null")
        @Email(message = "You have to use the correct structure for emails")
        String email,

        @NotBlank(message = "The firstname can not be empty or null")
        String firstName,
        @NotBlank(message = "The lastname can not be empty or null")
        String lastName,

        @NotBlank(message = "El campo clave no de estar vacio")

        @Pattern(regexp = "^.*\\d.*$", message = "The password must contain at least one number from 1 to 9.")
        @Length(min = 8, max = 255, message = "The password must be at least 8 characters long.")
        @Pattern(regexp = "^.*[a-z].*$", message = "The password must contain at least one lowercase letter.")
        @Pattern(regexp = "^.*[A-Z].*$", message = "The password must contain at least one uppercase letter.")
        @Pattern(regexp = "^.*[\\W_].*$", message = "The password must contain at least one special character: (@!%#?&).")
        String password,
        Set<Roles_Users> roles
) {
}
