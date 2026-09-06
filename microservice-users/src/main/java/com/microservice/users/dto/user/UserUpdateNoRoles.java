package com.microservice.users.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateNoRoles(
        @NotBlank(message = "The username can not be empty or null")
        String username,

        @NotBlank(message = "The email can not be empty or null")
        @Email(message = "You have to use the correct structure for emails")
        String email,

        @NotBlank(message = "The firstname can not be empty or null")
        String firstName,
        @NotBlank(message = "The lastname can not be empty or null")
        String lastName
) {
}
