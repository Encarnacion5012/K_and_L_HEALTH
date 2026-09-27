package com.microservice.users.service;

import com.microservice.users.dto.user.CreateUserDto;
import com.microservice.users.dto.user.CreateUserWithNoRoleDto;
import com.microservice.users.dto.user.UpdateUserDto;
import com.microservice.users.dto.user.UpdateUserWithNoRolesDto;
import com.microservice.users.model.Roles_Users;
import com.microservice.users.service.keycloak.KeycloakServiceImpl;
import com.microservice.users.util.KeycloakProvider;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KeycloakServiceImplTest {

    @InjectMocks
    private KeycloakServiceImpl keycloakService;

    // Mocks de la jerarquía fluida del cliente de Keycloak
    @Mock
    private RealmResource realmResource;
    @Mock
    private UsersResource usersResource;
    @Mock
    private UserResource userResource;
    @Mock
    private RolesResource rolesResource;
    @Mock
    private RoleResource roleResource;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private RoleScopeResource roleScopeResource;
    @Mock
    private Response response;

    private MockedStatic<KeycloakProvider> keycloakProviderMock;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        // Inicializamos el mock estático de KeycloakProvider antes de cada test
        keycloakProviderMock = mockStatic(KeycloakProvider.class);
    }

    @AfterEach
    void tearDown() {
        // CRÍTICO: Liberar el mock estático después de cada test para no contaminar los demás
        keycloakProviderMock.close();
    }

    // =========================================================================
    // TESTS PARA BÚSQUEDAS (findAllUser y searchUserByUsername)
    // =========================================================================

    @Test
    @DisplayName("findAllUser debe retornar la lista de usuarios del realm")
    void findAllUser_Exitoso() {
        UserRepresentation userRep = new UserRepresentation();
        keycloakProviderMock.when(KeycloakProvider::getRealmResource).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list()).thenReturn(List.of(userRep));

        List<UserRepresentation> resultado = keycloakService.findAllUser();

        assertThat(resultado).hasSize(1).containsExactly(userRep);
        verify(realmResource).users();
        verify(usersResource).list();
        verify(usersResource, never()).search(anyString(), anyBoolean());
    }

    @Test
    @DisplayName("searchUserByUsername debe buscar con coincidencia exacta (true)")
    void searchUserByUsername_Exitoso() {
        String username = "estainlin_dev";
        UserRepresentation userRep = new UserRepresentation();
        keycloakProviderMock.when(KeycloakProvider::getRealmResource).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(username, true)).thenReturn(List.of(userRep));

        List<UserRepresentation> resultado = keycloakService.searchUserByUsername(username);

        assertThat(resultado).hasSize(1).containsExactly(userRep);
        verify(realmResource).users();
        verify(usersResource).search(username, true);
        verify(usersResource, never()).list();
    }

    // =========================================================================
    // TESTS PARA CREACIÓN DE USUARIOS (createUser, createUserPacinte, createUserDoctor)
    // =========================================================================

    @Test
    @DisplayName("createUser con roles específicos debe crear usuario (201) y asignar los roles enviados")
    void createUser_ConRoles_Exitoso() {
        CreateUserDto dto = new CreateUserDto("user1", "email@test.com", "Juan", "Perez", "pass123", Set.of(Roles_Users.ADMIN));
        RoleRepresentation roleRep = new RoleRepresentation();

        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        keycloakProviderMock.when(KeycloakProvider::getRealmResource).thenReturn(realmResource);

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(URI.create("http://keycloak/users/" + userId));

        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("ADMIN")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(roleRep);

        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        String createdId = keycloakService.createUser(dto);

        assertThat(createdId).isEqualTo(userId);
        verify(usersResource).create(any(UserRepresentation.class));
        verify(rolesResource).get("ADMIN");
        verify(rolesResource, never()).get("USER");
        verify(roleScopeResource).add(List.of(roleRep));
        verify(response).close();
    }

    @Test
    @DisplayName("createUser con roles vacíos o nulos debe asignar el rol por defecto USER")
    void createUser_SinRoles_AsignaRolUserPorDefecto() {
        CreateUserDto dto = new CreateUserDto("user1", "email@test.com", "Juan", "Perez", "pass123", Collections.emptySet());
        RoleRepresentation defaultRoleRep = new RoleRepresentation();

        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        keycloakProviderMock.when(KeycloakProvider::getRealmResource).thenReturn(realmResource);

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(URI.create("http://keycloak/users/" + userId));

        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("USER")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(defaultRoleRep);

        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        String createdId = keycloakService.createUser(dto);

        assertThat(createdId).isEqualTo(userId);
        verify(rolesResource).get("USER");
        verify(roleScopeResource).add(List.of(defaultRoleRep));
        verify(response).close();
    }

    @Test
    @DisplayName("createUser debe lanzar IllegalStateException cuando Keycloak devuelve 409 CONFLICT")
    void createUser_Conflicto409_LanzaIllegalStateException() {
        CreateUserDto dto = new CreateUserDto("user1", "email@test.com", "Juan", "Perez", "pass123", Set.of(Roles_Users.PACIENTE));

        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(409);

        assertThatThrownBy(() -> keycloakService.createUser(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User or email already exists");

        verify(usersResource).create(any(UserRepresentation.class));
        verify(response, never()).getLocation();
        verify(realmResource, never()).roles();
        verify(roleScopeResource, never()).add(anyList());
        verify(response).close();
    }

    @Test
    @DisplayName("createUser debe lanzar RuntimeException cuando Keycloak devuelve otro código de error (ej. 500)")
    void createUser_Error500_LanzaRuntimeException() {
        CreateUserDto dto = new CreateUserDto("user1", "email@test.com", "Juan", "Perez", "pass123", Set.of(Roles_Users.PACIENTE));

        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(500);

        assertThatThrownBy(() -> keycloakService.createUser(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error communicating with authentication provider");

        verify(usersResource).create(any(UserRepresentation.class));
        verify(response, never()).getLocation();
        verify(realmResource, never()).roles();
        verify(roleScopeResource, never()).add(anyList());
        verify(response).close();
    }

    @Test
    @DisplayName("createUserPacinte debe crear el usuario asignando el rol PACIENTE")
    void createUserPacinte_Exitoso() {
        CreateUserWithNoRoleDto dto = mock(CreateUserWithNoRoleDto.class);
        when(dto.username()).thenReturn("paciente1");
        when(dto.firstName()).thenReturn("Ana");
        when(dto.lastName()).thenReturn("Gomez");
        when(dto.email()).thenReturn("ana@test.com");
        when(dto.password()).thenReturn("1234");

        RoleRepresentation rolPacienteRep = new RoleRepresentation();

        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        keycloakProviderMock.when(KeycloakProvider::getRealmResource).thenReturn(realmResource);

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(URI.create("http://keycloak/users/" + userId));

        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get(Roles_Users.PACIENTE.name())).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(rolPacienteRep);

        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        String resultadoId = keycloakService.createUserPacinte(dto);

        assertThat(resultadoId).isEqualTo(userId);
        verify(rolesResource).get("PACIENTE");
        verify(rolesResource, never()).get("DOCTOR");
        verify(roleScopeResource).add(List.of(rolPacienteRep));
    }

    @Test
    @DisplayName("createUserDoctor debe crear el usuario asignando el rol DOCTOR")
    void createUserDoctor_Exitoso() {
        CreateUserWithNoRoleDto dto = mock(CreateUserWithNoRoleDto.class);
        when(dto.username()).thenReturn("doctor1");
        when(dto.firstName()).thenReturn("Carlos");
        when(dto.lastName()).thenReturn("Ruiz");
        when(dto.email()).thenReturn("carlos@test.com");
        when(dto.password()).thenReturn("1234");

        RoleRepresentation rolDoctorRep = new RoleRepresentation();

        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        keycloakProviderMock.when(KeycloakProvider::getRealmResource).thenReturn(realmResource);

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(URI.create("http://keycloak/users/" + userId));

        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get(Roles_Users.DOCTOR.name())).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(rolDoctorRep);

        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        String resultadoId = keycloakService.createUserDoctor(dto);

        assertThat(resultadoId).isEqualTo(userId);
        verify(rolesResource).get("DOCTOR");
        verify(rolesResource, never()).get("PACIENTE");
        verify(roleScopeResource).add(List.of(rolDoctorRep));
    }

    // =========================================================================
    // TESTS PARA DELETE USER
    // =========================================================================

    @Test
    @DisplayName("deleteUser debe eliminar el usuario en Keycloak por su ID")
    void deleteUser_Exitoso() {
        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);

        keycloakService.deleteUser(userId);

        verify(usersResource).get(userId);
        verify(userResource).remove();
        verify(userResource, never()).update(any());
    }

    // =========================================================================
    // TESTS PARA UPDATE USER (CON ROLES)
    // =========================================================================

    @Test
    @DisplayName("updateUser con nuevos roles debe remover roles actuales y agregar los nuevos")
    void updateUser_ConRoles_ReemplazaRolesYActualiza() {
        UpdateUserDto updateDto = new UpdateUserDto("nuevoUser", "nuevo@test.com", "NuevoNombre", "NuevoApellido", Set.of(Roles_Users.DOCTOR));
        UserRepresentation userRep = new UserRepresentation();
        RoleRepresentation oldRole = new RoleRepresentation();
        RoleRepresentation newRole = new RoleRepresentation();

        keycloakProviderMock.when(KeycloakProvider::getRealmResource).thenReturn(realmResource);
        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);

        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRep);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        when(roleScopeResource.listAll()).thenReturn(List.of(oldRole));

        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("DOCTOR")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(newRole);

        keycloakService.updateUser(userId, updateDto);

        assertThat(userRep.getFirstName()).isEqualTo("NuevoNombre");
        assertThat(userRep.getEmail()).isEqualTo("nuevo@test.com");

        verify(userResource).update(userRep);
        verify(roleScopeResource).remove(List.of(oldRole));
        verify(roleScopeResource).add(List.of(newRole));
    }

    @Test
    @DisplayName("updateUser con roles null solo debe actualizar datos básicos sin tocar roles")
    void updateUser_ConRolesNull_NoModificaRoles() {
        UpdateUserDto updateDto = new UpdateUserDto("nuevoUser", "nuevo@test.com", "NuevoNombre", "NuevoApellido", null);
        UserRepresentation userRep = new UserRepresentation();

        keycloakProviderMock.when(KeycloakProvider::getRealmResource).thenReturn(realmResource);
        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRep);

        keycloakService.updateUser(userId, updateDto);

        verify(userResource).update(userRep);
        verify(userResource, never()).roles();
        verify(roleScopeResource, never()).remove(anyList());
        verify(roleScopeResource, never()).add(anyList());
    }

    // =========================================================================
    // TESTS PARA UPDATE USER NO ROLES
    // =========================================================================

    @Test
    @DisplayName("updateUserNoRoles con DTO null debe retornar inmediatamente sin llamar a Keycloak")
    void updateUserNoRoles_DtoNull_NoHaceNada() {
        keycloakService.updateUserNoRoles(userId, null);

        keycloakProviderMock.verify(KeycloakProvider::getUserResource, never());
        verifyNoInteractions(usersResource, userResource);
    }

    @Test
    @DisplayName("updateUserNoRoles con datos completos debe actualizar todos los campos en Keycloak")
    void updateUserNoRoles_DatosCompletos_ActualizaTodo() {

        UpdateUserWithNoRolesDto updateDto = new UpdateUserWithNoRolesDto(
                "userNuevo",
                "correo@nuevo.com",
                "NombreNuevo",
                "ApellidoNuevo"
        );
        UserRepresentation userRep = spy(new UserRepresentation());

        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRep);

        keycloakService.updateUserNoRoles(userId, updateDto);

        verify(userRep).setFirstName("NombreNuevo");
        verify(userRep).setLastName("ApellidoNuevo");
        verify(userRep).setEmail("correo@nuevo.com");
        verify(userRep).setEmailVerified(true);
        verify(userRep).setUsername("userNuevo");
        verify(userResource).update(userRep);
        verify(userResource, never()).roles();
    }

    @Test
    @DisplayName("updateUserNoRoles con campos nulos solo debe mutar los campos presentes")
    void updateUserNoRoles_CamposParciales_SoloActualizaNoNulos() {
        UpdateUserWithNoRolesDto updateDto = new UpdateUserWithNoRolesDto(null, null, "SoloNombre", null);
        UserRepresentation userRep = spy(new UserRepresentation());

        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRep);

        keycloakService.updateUserNoRoles(userId, updateDto);

        verify(userRep).setFirstName("SoloNombre");
        verify(userRep, never()).setLastName(anyString());
        verify(userRep, never()).setEmail(anyString());
        verify(userRep, never()).setEmailVerified(anyBoolean());
        verify(userRep, never()).setUsername(anyString());
        verify(userResource).update(userRep);
    }

    // =========================================================================
    // TESTS PARA CHANGE ENABLED STATE (Activar / Desactivar)
    // =========================================================================

    @Test
    @DisplayName("changeEnabledState(false) debe deshabilitar el usuario y cerrar sus sesiones (logout)")
    void changeEnabledState_False_DeshabilitaYHaceLogout() {
        UserRepresentation userRep = spy(new UserRepresentation());
        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRep);

        keycloakService.changeEnabledState(userId, false);

        verify(userRep).setEnabled(false);
        verify(userResource).update(userRep);
        verify(userResource).logout();
    }

    @Test
    @DisplayName("changeEnabledState(true) debe habilitar el usuario y NUNCA llamar a logout")
    void changeEnabledState_True_HabilitaSinLogout() {
        UserRepresentation userRep = spy(new UserRepresentation());
        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRep);

        keycloakService.changeEnabledState(userId, true);

        verify(userRep).setEnabled(true);
        verify(userResource).update(userRep);
        verify(userResource, never()).logout();
    }

    // =========================================================================
    // TESTS PARA GET USER UPDATE DATA (Snapshot)
    // =========================================================================

    @Test
    @DisplayName("getUserUpdateData debe retornar el DTO con el snapshot actual del usuario")
    void getUserUpdateData_Exitoso() {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setUsername("user_snap");
        userRep.setFirstName("NombreSnap");
        userRep.setLastName("ApellidoSnap");
        userRep.setEmail("snap@test.com");

        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRep);

        UpdateUserWithNoRolesDto snapshot = keycloakService.getUserUpdateData(userId);

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.username()).isEqualTo("user_snap");
        assertThat(snapshot.firstName()).isEqualTo("NombreSnap");
        assertThat(snapshot.lastName()).isEqualTo("ApellidoSnap");
        assertThat(snapshot.email()).isEqualTo("snap@test.com");

        verify(usersResource).get(userId);
        verify(userResource).toRepresentation();
        verify(userResource, never()).update(any());
    }

    @Test
    @DisplayName("getUserUpdateData debe lanzar RuntimeException si toRepresentation devuelve null")
    void getUserUpdateData_UsuarioNull_LanzaExcepcion() {
        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(null);

        assertThatThrownBy(() -> keycloakService.getUserUpdateData(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error al consultar datos previos en el servidor de autenticación para ID: " + userId);

        verify(usersResource).get(userId);
        verify(userResource).toRepresentation();
        verify(userResource, never()).update(any());
    }

    @Test
    @DisplayName("getUserUpdateData debe lanzar RuntimeException si falla la conexión con Keycloak")
    void getUserUpdateData_FalloKeycloak_LanzaExcepcion() {
        keycloakProviderMock.when(KeycloakProvider::getUserResource).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> keycloakService.getUserUpdateData(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error al consultar datos previos en el servidor de autenticación para ID: " + userId);

        verify(usersResource).get(userId);
        verify(userResource).toRepresentation();
    }
}