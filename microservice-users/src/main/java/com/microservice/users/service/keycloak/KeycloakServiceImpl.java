package com.microservice.users.service.keycloak;

import com.microservice.users.dto.user.UserRegisterDto;
import com.microservice.users.dto.user.UserUpdateDto;
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

    @Override
    public UUID createUser(UserRegisterDto userDTO) {
        UsersResource userResource = KeycloakProvider.getUserResource();

        UUID userId = UUID.randomUUID();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(userId.toString());
        userRepresentation.setFirstName(userDTO.firstName());
        userRepresentation.setLastName(userDTO.lastName());
        userRepresentation.setEmail(userDTO.email());
        userRepresentation.setUsername(userDTO.username());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setEnabled(true);

        // try-with-resources cierra la conexión HTTP automáticamente
        try (Response response = userResource.create(userRepresentation)) {
            int status = response.getStatus();

            if (status == Response.Status.CREATED.getStatusCode()) {
                // 1. Asignar contraseña
                CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
                credentialRepresentation.setTemporary(false);
                credentialRepresentation.setType(OAuth2Constants.PASSWORD);
                credentialRepresentation.setValue(userDTO.password());

                userResource.get(userId.toString()).resetPassword(credentialRepresentation);

                // 2. Resolver roles usando la llamada estática limpia
                RealmResource realmResource = KeycloakProvider.getRealmResource();
                List<RoleRepresentation> rolesRepresentation;

                if (userDTO.roles() == null || userDTO.roles().isEmpty()) {
                    rolesRepresentation = List.of(realmResource.roles().get("USER").toRepresentation());
                } else {
                    rolesRepresentation = userDTO.roles().stream()
                            .map(roleEnum -> realmResource.roles().get(roleEnum.name()).toRepresentation())
                            .toList();
                }

                realmResource.users()
                        .get(userId.toString())
                        .roles()
                        .realmLevel()
                        .add(rolesRepresentation);

                log.info("User {} created successfully with ID {}", userDTO.username(), userId);
                return userId; // Retorna el UUID para persistirlo en PostgreSQL

            } else if (status == Response.Status.CONFLICT.getStatusCode()) {
                log.error("User {} or email already exists in Keycloak", userDTO.username());
                throw new IllegalStateException("User or email already exists");
            } else {
                log.error("Unable to create user {}. Keycloak HTTP Status: {}", userDTO.username(), status);
                throw new RuntimeException("Error communicating with authentication provider");
            }
        }
    }

    @Override
    public void deleteUser(String userId) {
        KeycloakProvider.getUserResource().get(userId).remove();
        log.info("User with ID {} deleted from Keycloak", userId);
    }

    @Override
    public void updateUser(String userId, UserUpdateDto updateDto) {
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
}