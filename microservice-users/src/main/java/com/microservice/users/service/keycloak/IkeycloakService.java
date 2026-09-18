package com.microservice.users.service.keycloak;

import com.microservice.users.dto.user.*;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface IkeycloakService {
    List<UserRepresentation> findAllUser();
    List<UserRepresentation>searchUserByUsername(String username);
    String createUser(CreateUserDto user); //hacer el dto de creccion
    void deleteUser(String userId);
    void updateUser(String userId, UpdateUserDto updateDto); //hacer el dto de actualizar
    void updateUserNoRoles(String userId, UpdateUserWithNoRolesDto updateDto);
    String createUserPacinte(CreateUserWithNoRoleDto user);
    String createUserDoctor(CreateUserWithNoRoleDto user);
    void changeEnabledState(String userId, boolean enabled);

}
