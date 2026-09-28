package com.microservice.users.service.authentication;

import com.microservice.users.dto.authentication.LoginResponsePaciente;
import com.microservice.users.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j

public class AuthenticationServiceImpl  implements  IAuthenticationService {
    private final PacienteRepository pacienteRepository;

    @Override
    public LoginResponsePaciente loginResponsePaciente(Jwt jwt) {
        

        return null;
    }
}
