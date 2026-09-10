package com.microservice.users.service.keycloak;

import com.microservice.users.dto.user.UserRegisterDto;
import com.microservice.users.dto.user.UserUpdateDto;
import com.microservice.users.dto.user.UserUpdateNoRolesDTO;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface IkeycloakService {
    List<UserRepresentation> findAllUser();
    List<UserRepresentation>searchUserByUsername(String username);
    String createUser(UserRegisterDto user); //hacer el dto de creccion
    void deleteUser(String userId);
    void updateUser(String userId, UserUpdateDto updateDto); //hacer el dto de actualizar
    void updateUserNoRoles(String userId, UserUpdateNoRolesDTO updateDto);
    String createUserPacinte(UserRegisterDto user);

}
