package com.microservice.users.service.keycloak;

import com.microservice.users.dto.user.CreateUserDto;
import com.microservice.users.dto.user.CreateUserWithNoRoleDTO;
import com.microservice.users.dto.user.UpdateUserDto;
import com.microservice.users.dto.user.UpdateUserWithNoRolesDTO;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface IkeycloakService {
    List<UserRepresentation> findAllUser();
    List<UserRepresentation>searchUserByUsername(String username);
    String createUser(CreateUserDto user); //hacer el dto de creccion
    void deleteUser(String userId);
    void updateUser(String userId, UpdateUserDto updateDto); //hacer el dto de actualizar
    void updateUserNoRoles(String userId, UpdateUserWithNoRolesDTO updateDto);
    String createUserPacinte(CreateUserWithNoRoleDTO user);

}
