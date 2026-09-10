package com.microservice.users.service.authentication;

import com.microservice.users.dto.authentication.LoginResponsePaciente;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface IAuthenticationService {

    LoginResponsePaciente loginResponsePaciente(OidcUser oidcUser);

}
