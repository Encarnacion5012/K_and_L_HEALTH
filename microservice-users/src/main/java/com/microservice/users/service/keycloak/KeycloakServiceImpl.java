package com.microservice.users.service.keycloak;

import com.microservice.users.dto.user.*;
import com.microservice.users.model.Roles_Users;
import com.microservice.users.util.KeycloakProvider;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class KeycloakServiceImpl implements IkeycloakService {

    @Override
    public List<UserRepresentation> findAllUser() {
        return KeycloakProvider.getRealmResource()
                .users()
                .list();
    }

    @Override
    public List<UserRepresentation> searchUserByUsername(String username) {
        return KeycloakProvider.getRealmResource()
                .users()
                .search(username, true);
    }

   private UserRepresentation reutilizableUserRepresentation(String username, String password, String firstName, String lastName, String email) {
       // 1. Configurar credenciales iniciales directamente en el UserRepresentation
       CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
       credentialRepresentation.setTemporary(false);
       credentialRepresentation.setType(OAuth2Constants.PASSWORD);
       credentialRepresentation.setValue(password);

       UserRepresentation userRepresentation = new UserRepresentation();
       userRepresentation.setFirstName(firstName);
       userRepresentation.setLastName(lastName);
       userRepresentation.setEmail(email);
       userRepresentation.setUsername(username);
       userRepresentation.setEmailVerified(true);
       userRepresentation.setEnabled(true);
       userRepresentation.setCredentials(List.of(credentialRepresentation));

       return userRepresentation;
   }

    @Override
    public String createUser(CreateUserDto userDTO) {
       return userCreation(userDTO.username(), userDTO.firstName(), userDTO.lastName(), userDTO.email(),userDTO.password(), userDTO.roles());
    }



    private String userCreation(
            String userName, String firstName, String lastName, String email, String password, Set<Roles_Users> roles
    ) {
        UsersResource userResource = KeycloakProvider.getUserResource();

        UserRepresentation userRepresentation = reutilizableUserRepresentation(
                userName, password,firstName, lastName, email
        );

        try (Response response = userResource.create(userRepresentation)) {
            int status = response.getStatus();

            if (status == Response.Status.CREATED.getStatusCode()) {
                // 2. Extraer el ID real asignado por Keycloak desde el header Location
                String path = response.getLocation().getPath();
                String createdUserId = path.substring(path.lastIndexOf('/') + 1);


                // 3. Asignar roles a nivel de Realm
                RealmResource realmResource = KeycloakProvider.getRealmResource();
                List<RoleRepresentation> rolesRepresentation;

                if (roles == null || roles.isEmpty()) {
                    rolesRepresentation = List.of(realmResource.roles().get("USER").toRepresentation());
                } else {
                    rolesRepresentation = roles.stream()
                            .map(roleEnum -> realmResource.roles().get(roleEnum.name()).toRepresentation())
                            .toList();
                }

                realmResource.users()
                        .get(createdUserId)
                        .roles()
                        .realmLevel()
                        .add(rolesRepresentation);

                log.info("User {} created successfully with ID {}", userName, UUID.fromString(createdUserId));
                return createdUserId; // Retorna el UUID para persistirlo en PostgreSQL

            } else if (status == Response.Status.CONFLICT.getStatusCode()) {
                log.error("User {} or email already exists in Keycloak", userName);
                throw new IllegalStateException("User or email already exists");
            } else {
                log.error("Unable to create user {}. Keycloak HTTP Status: {}", userName, status);
                throw new RuntimeException("Error communicating with authentication provider");
            }
        }
    }





    @Override
    public String createUserPacinte(CreateUserWithNoRoleDto userDTO) {
        Set<Roles_Users> rolPaciente = Set.of(Roles_Users.PACIENTE);

        return userCreation(userDTO.username(), userDTO.firstName(), userDTO.lastName(), userDTO.email(),userDTO.password(), rolPaciente );
    }

    @Override
    public void deleteUser(String userId) {
        KeycloakProvider.getUserResource().get(userId).remove();
        log.info("User with ID {} deleted from Keycloak", userId);
    }

    @Override
    public void updateUser(String userId, UpdateUserDto updateDto) {
        // Implementación de actualización
        RealmResource realmResource = KeycloakProvider.getRealmResource();

        UserResource userResource = KeycloakProvider.getUserResource().get(userId);

        UserRepresentation userRepresentation = userResource.toRepresentation();

        userRepresentation.setFirstName(updateDto.firstName());
        userRepresentation.setLastName(updateDto.lastName());
        userRepresentation.setEmail(updateDto.email());
        userRepresentation.setUsername(updateDto.username());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setEnabled(true);

        userResource.update(userRepresentation);


        //actualizaion de roles

        if (updateDto.roles() != null) {
            List<RoleRepresentation> currentRoles = userResource.roles().realmLevel().listAll();

            if (!currentRoles.isEmpty()) {
                userResource.roles().realmLevel().remove(currentRoles);
            }

            List<RoleRepresentation> newRoles;


            if(updateDto.roles().isEmpty()) {
                newRoles = List.of(realmResource.roles().get("USER").toRepresentation());
            }
            else {
                newRoles = updateDto.roles().stream()
                        .map(rolEnum -> realmResource.roles().get(rolEnum.name()).toRepresentation())
                        .toList();
            }

            userResource.roles().realmLevel().add(newRoles);

        }

        log.info("User {} updated successfully with ID {}", updateDto.username(), userId);
    }

    @Override
    public void updateUserNoRoles(String userId, UpdateUserWithNoRolesDto updateDto) {

        if (updateDto == null) {
            return;
        }

        UserResource userResource = KeycloakProvider.getUserResource().get(userId);

        UserRepresentation userRepresentation = userResource.toRepresentation();

        if (updateDto.firstName() != null) {
            userRepresentation.setFirstName(updateDto.firstName());
        }

        if (updateDto.lastName() != null){
            userRepresentation.setLastName(updateDto.lastName());
        }
        if (updateDto.email() != null){
            userRepresentation.setEmail(updateDto.email());
            userRepresentation.setEmailVerified(true);
        }

        if (updateDto.username() != null) {
            userRepresentation.setUsername(updateDto.username());
        }


        userResource.update(userRepresentation);

    }

    @Override
    public String createUserDoctor(CreateUserWithNoRoleDto user) {
        Set<Roles_Users>  rolDoctor = Set.of(Roles_Users.DOCTOR);
        return userCreation(user.username(), user.firstName(), user.lastName(), user.email(),user.password(), rolDoctor);


    }
}