package com.microservice.users.dto.authentication;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
    @NotBlank(message = "The email/username can not be empty")
    String userName_email,

    @NotBlank(message = "The password can not be empty")
    String password
) {
}
