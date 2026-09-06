package com.microservice.users.service.keycloak;

import com.microservice.users.dto.user.UserRegisterDto;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface IkeycloakService {
    List<UserRepresentation> findAllUser();
    List<UserRepresentation>searchUserByUsername(String username);
    String createUser(UserRegisterDto user); //hacer el dto de creccion
    void deleteUser(String userId);
    void updateUser(String userId ); //hacer el dto de actualizar
}
