package com.microservice.users.dto.user;

import com.microservice.users.model.Roles_Users;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public record UserUpdateDto(
        @NotBlank(message = "The username can not be empty or null")
        String username,

        @NotBlank(message = "The email can not be empty or null")
        @Email(message = "You have to use the correct structure for emails")
        String email,

        @NotBlank(message = "The firstname can not be empty or null")
        String firstName,
        @NotBlank(message = "The lastname can not be empty or null")
        String lastName,

        Set<Roles_Users> roles
) {

}
