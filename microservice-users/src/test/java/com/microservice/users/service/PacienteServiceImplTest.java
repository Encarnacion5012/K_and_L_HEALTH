package com.microservice.users.service;

import com.microservice.users.dto.paciente.CreatePacienteDto;
import com.microservice.users.dto.paciente.DetallePacienteDto;
import com.microservice.users.dto.paciente.UpdatePacienteDto;
import com.microservice.users.dto.user.UpdateUserWithNoRolesDto;
import com.microservice.users.exception.custom.AuthIntegrationException;
import com.microservice.users.exception.custom.CriticalRollbackException;
import com.microservice.users.exception.custom.PersistenceCompensatedException;
import com.microservice.users.mapper.PacienteMapperService;
import com.microservice.users.model.Paciente;
import com.microservice.users.projections.pacientes.DetallesPacienteProjection;
import com.microservice.users.repository.PacienteRepository;
import com.microservice.users.service.keycloak.IkeycloakService;
import com.microservice.users.service.pacientes.PacienteServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PacienteServiceImplTest {

    @Mock
    private PacienteMapperService pacienteMapperService;
    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private IkeycloakService keycloakService;

    @InjectMocks
    private PacienteServiceImpl pacienteService;

    private UUID pacienteId;
    private UUID authId;
    private Paciente paciente;

    @BeforeEach
    public void setUp() {
        this.pacienteId = UUID.randomUUID();
        this.authId = UUID.randomUUID();
        this.paciente = new Paciente();
        this.paciente.setIdPaciente(pacienteId);
        this.paciente.setIdAuthentication(authId);
        this.paciente.setActivo(true);
    }

    // =========================================================================
    // TESTS PARA CREATE PACIENTE
    // =========================================================================

    @Test
    @DisplayName("Debe crear un paciente exitosamente en el servicio de autenticación y DB")
    void create_Exitoso() {
        CreatePacienteDto createPacienteDto = mock(CreatePacienteDto.class);
        DetallePacienteDto detallePacienteDto = mock(DetallePacienteDto.class);

        when(keycloakService.createUserPacinte(any())).thenReturn(authId.toString());
        when(pacienteMapperService.createDTOtoEntity(createPacienteDto)).thenReturn(paciente);
        when(pacienteRepository.save(paciente)).thenReturn(paciente);
        when(pacienteMapperService.entityToDTO(paciente)).thenReturn(detallePacienteDto);

        DetallePacienteDto resultado = pacienteService.create(createPacienteDto);

        assertThat(resultado).isNotNull().isEqualTo(detallePacienteDto);
        verify(keycloakService).createUserPacinte(any());
        verify(pacienteMapperService).createDTOtoEntity(createPacienteDto);
        verify(pacienteRepository).save(paciente);
        verify(pacienteMapperService).entityToDTO(paciente);
        verify(keycloakService, never()).deleteUser(anyString());
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException si el auth server devuelve un id Null")
    void create_FalloAuthServerRetornaNull() {
        CreatePacienteDto createPacienteDto = mock(CreatePacienteDto.class);

        when(keycloakService.createUserPacinte(any())).thenReturn(null);

        assertThatThrownBy(() -> pacienteService.create(createPacienteDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("No se puede crear un Paciente, ha habido un error");

        verify(keycloakService).createUserPacinte(any());
        verify(pacienteMapperService, never()).createDTOtoEntity(any());
        verify(pacienteRepository, never()).save(any());
        verify(keycloakService, never()).deleteUser(anyString());
    }

    @Test
    @DisplayName("Debe ejecutar compensación y lanzar PersistenceCompensatedException si ocurre un error en la DB")
    void create_FalloCompensacionEjecucionCompensacion() {
        CreatePacienteDto createPacienteDto = mock(CreatePacienteDto.class);
        when(keycloakService.createUserPacinte(any())).thenReturn(authId.toString());
        when(pacienteMapperService.createDTOtoEntity(createPacienteDto)).thenReturn(paciente);
        doThrow(new RuntimeException("Error en postgres")).when(pacienteRepository).save(paciente);

        assertThatThrownBy(() -> pacienteService.create(createPacienteDto))
                .isInstanceOf(PersistenceCompensatedException.class)
                .hasMessage("Hubo un problema al crear el nuevo Paciente: Error en postgres");

        verify(keycloakService).createUserPacinte(any());
        verify(pacienteRepository).save(paciente);
        verify(keycloakService).deleteUser(authId.toString());
        verify(pacienteMapperService, never()).entityToDTO(any());
    }

    @Test
    @DisplayName("Debe lanzar CriticalRollbackException al haber un error cuando se hace la compensación")
    void create_FalloCompensacionServerRetornaError() {
        CreatePacienteDto createPacienteDto = mock(CreatePacienteDto.class);
        when(keycloakService.createUserPacinte(any())).thenReturn(authId.toString());
        when(pacienteMapperService.createDTOtoEntity(createPacienteDto)).thenReturn(paciente);
        doThrow(new RuntimeException("Error en postgres")).when(pacienteRepository).save(paciente);
        doThrow(new RuntimeException(authId.toString())).when(keycloakService).deleteUser(authId.toString());

        assertThatThrownBy(() -> pacienteService.create(createPacienteDto))
                .isInstanceOf(CriticalRollbackException.class)
                .hasMessage("Error al eliminar el Paciente con id_autorization: " + authId);

        verify(keycloakService).createUserPacinte(any());
        verify(pacienteRepository).save(paciente);
        verify(keycloakService).deleteUser(authId.toString());
        verify(pacienteMapperService, never()).entityToDTO(any());
    }

    // =========================================================================
    // TESTS PARA UPDATE PACIENTE
    // =========================================================================

    @Test
    @DisplayName("Update exitoso")
    void update_UpdateExitoso() {
        UpdatePacienteDto updatePacienteDto = mock(UpdatePacienteDto.class);
        DetallePacienteDto detallePacienteDto = mock(DetallePacienteDto.class);
        UpdateUserWithNoRolesDto snapShotUser = mock(UpdateUserWithNoRolesDto.class);
        UpdateUserWithNoRolesDto nuevosDatosUser = mock(UpdateUserWithNoRolesDto.class);
        String authIdStr = authId.toString();

        when(updatePacienteDto.userUpdate()).thenReturn(nuevosDatosUser);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(keycloakService.getUserUpdateData(authIdStr)).thenReturn(snapShotUser);
        when(pacienteMapperService.entityToDTO(paciente)).thenReturn(detallePacienteDto);

        DetallePacienteDto resultado = pacienteService.update(pacienteId, updatePacienteDto);

        assertThat(resultado).isNotNull().isEqualTo(detallePacienteDto);

        verify(keycloakService).getUserUpdateData(authIdStr);
        verify(keycloakService).updateUserNoRoles(authIdStr, nuevosDatosUser);
        verify(pacienteMapperService).updatePaciente(updatePacienteDto, paciente);
        verify(pacienteRepository).flush();
        verify(pacienteMapperService).entityToDTO(paciente);
        verify(keycloakService, never()).updateUserNoRoles(authIdStr, snapShotUser);
    }

    @Test
    @DisplayName("Update lanza AuthIntegrationException al fallar traer snapshot de Keycloak")
    void update_FalloSnapshotKeycloak() {
        UpdatePacienteDto updatePacienteDto = mock(UpdatePacienteDto.class);
        String authIdStr = authId.toString();

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        doThrow(new RuntimeException("Keycloak caído"))
                .when(keycloakService).getUserUpdateData(authIdStr);

        assertThatThrownBy(() -> pacienteService.update(pacienteId, updatePacienteDto))
                .isInstanceOf(AuthIntegrationException.class)
                .hasMessage("Error al obtener el estado previo antes de actulizar con el usuario: " + authIdStr);

        verify(keycloakService).getUserUpdateData(authIdStr);
        verify(keycloakService, never()).updateUserNoRoles(anyString(), any());
        verify(pacienteMapperService, never()).updatePaciente(any(), any());
        verify(pacienteRepository, never()).flush();
    }

    @Test
    @DisplayName("Update lanza AuthIntegrationException al fallar la actualización en Keycloak")
    void update_FalloActualizarKeycloak() {
        UpdatePacienteDto updatePacienteDto = mock(UpdatePacienteDto.class);
        UpdateUserWithNoRolesDto snapShotUser = mock(UpdateUserWithNoRolesDto.class);
        String authIdStr = authId.toString();

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(keycloakService.getUserUpdateData(authIdStr)).thenReturn(snapShotUser);
        doThrow(new RuntimeException("Fallo en Keycloak"))
                .when(keycloakService).updateUserNoRoles(eq(authIdStr), any());

        assertThatThrownBy(() -> pacienteService.update(pacienteId, updatePacienteDto))
                .isInstanceOf(AuthIntegrationException.class)
                .hasMessage("Error al actualizar el Paciente con id_autorization: " + authIdStr);

        verify(keycloakService).getUserUpdateData(authIdStr);
        verify(keycloakService).updateUserNoRoles(eq(authIdStr), any());
        verify(pacienteMapperService, never()).updatePaciente(any(), any());
        verify(pacienteRepository, never()).flush();
    }

    @Test
    @DisplayName("Update fallo en BD ejecuta compensación del snapshot y lanza PersistenceCompensatedException")
    void update_FalloEnDB_EjecutaCompensacionSnapshot() {
        UpdatePacienteDto updatePacienteDto = mock(UpdatePacienteDto.class);
        UpdateUserWithNoRolesDto nuevosDatosUser = mock(UpdateUserWithNoRolesDto.class);
        UpdateUserWithNoRolesDto snapShotUser = mock(UpdateUserWithNoRolesDto.class);
        String authIdStr = authId.toString();

        when(updatePacienteDto.userUpdate()).thenReturn(nuevosDatosUser);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(keycloakService.getUserUpdateData(authIdStr)).thenReturn(snapShotUser);
        doThrow(new RuntimeException("Error constraints Postgres")).when(pacienteRepository).flush();

        assertThatThrownBy(() -> pacienteService.update(pacienteId, updatePacienteDto))
                .isInstanceOf(PersistenceCompensatedException.class)
                .hasMessage("Ha ocurrido un error al actulizar la data del paciente");

        verify(keycloakService).getUserUpdateData(authIdStr);
        verify(keycloakService).updateUserNoRoles(authIdStr, nuevosDatosUser);
        verify(pacienteMapperService).updatePaciente(updatePacienteDto, paciente);
        verify(pacienteRepository).flush();
        verify(keycloakService).updateUserNoRoles(authIdStr, snapShotUser);
        verify(pacienteMapperService, never()).entityToDTO(any());
    }

    @Test
    @DisplayName("Update lanza CriticalRollbackException si falla la BD y también falla el rollback en Keycloak")
    void update_FalloEnDB_FalloCompensacionSnapshot() {
        UpdatePacienteDto updatePacienteDto = mock(UpdatePacienteDto.class);
        UpdateUserWithNoRolesDto nuevosDatosUser = mock(UpdateUserWithNoRolesDto.class);
        UpdateUserWithNoRolesDto snapShotUser = mock(UpdateUserWithNoRolesDto.class);
        String authIdStr = authId.toString();

        when(updatePacienteDto.userUpdate()).thenReturn(nuevosDatosUser);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(keycloakService.getUserUpdateData(authIdStr)).thenReturn(snapShotUser);

        doNothing()
                .doThrow(new RuntimeException("Keycloak Caído"))
                .when(keycloakService).updateUserNoRoles(eq(authIdStr), any());
        doThrow(new RuntimeException("Error constraints")).when(pacienteRepository).flush();

        assertThatThrownBy(() -> pacienteService.update(pacienteId, updatePacienteDto))
                .isInstanceOf(CriticalRollbackException.class)
                .hasMessage("Ha habido un error al hacer rollback del Paciente");

        verify(keycloakService).getUserUpdateData(authIdStr);
        verify(keycloakService).updateUserNoRoles(authIdStr, nuevosDatosUser);
        verify(keycloakService).updateUserNoRoles(authIdStr, snapShotUser);
        verify(pacienteMapperService, never()).entityToDTO(any());
    }

    // =========================================================================
    // TESTS PARA DESACTIVAR Y REACTIVAR PACIENTE
    // =========================================================================

    @Test
    @DisplayName("Debe desactivar el paciente exitosamente en Keycloak y BD")
    void desactivarPaciente_Exitoso() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

        pacienteService.desactivarPaciente(pacienteId);

        assertThat(paciente.isActivo()).isFalse();
        verify(pacienteRepository).findById(pacienteId);
        verify(keycloakService).changeEnabledState(authId.toString(), false);
    }

    @Test
    @DisplayName("Debe lanzar AuthIntegrationException al desactivar si Keycloak falla")
    void desactivarPaciente_FalloKeycloak() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        doThrow(new RuntimeException("Timeout")).when(keycloakService).changeEnabledState(authId.toString(), false);

        assertThatThrownBy(() -> pacienteService.desactivarPaciente(pacienteId))
                .isInstanceOf(AuthIntegrationException.class)
                .hasMessage("Error al eliminar el Paciente");

        assertThat(paciente.isActivo()).isTrue();
        verify(keycloakService).changeEnabledState(authId.toString(), false);
    }

    @Test
    @DisplayName("Debe reactivar el paciente exitosamente en Keycloak y BD")
    void reactivarPaciente_Exitoso() {
        paciente.setActivo(false);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

        pacienteService.reactivarPaciente(pacienteId);

        assertThat(paciente.isActivo()).isTrue();
        verify(pacienteRepository).findById(pacienteId);
        verify(keycloakService).changeEnabledState(authId.toString(), true);
    }

    @Test
    @DisplayName("Debe lanzar AuthIntegrationException al reactivar si Keycloak falla")
    void reactivarPaciente_FalloKeycloak() {
        paciente.setActivo(false);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        doThrow(new RuntimeException("Timeout")).when(keycloakService).changeEnabledState(authId.toString(), true);

        assertThatThrownBy(() -> pacienteService.reactivarPaciente(pacienteId))
                .isInstanceOf(AuthIntegrationException.class)
                .hasMessage("Error al reactivar el Paciente");

        assertThat(paciente.isActivo()).isFalse();
        verify(keycloakService).changeEnabledState(authId.toString(), true);
    }

    // =========================================================================
    // TESTS PARA CONSULTAS (READ-ONLY)
    // =========================================================================

    @Test
    @DisplayName("findAllActivo debe retornar página de proyecciones")
    void findAllActivo_Exitoso() {
        Pageable pageable = mock(Pageable.class);
        @SuppressWarnings("unchecked")
        Page<DetallesPacienteProjection> pageMock = mock(Page.class);
        when(pacienteRepository.findAllByActivoTrue(pageable)).thenReturn(pageMock);

        Page<DetallesPacienteProjection> resultado = pacienteService.findAllActivo(pageable);

        assertThat(resultado).isEqualTo(pageMock);
        verify(pacienteRepository).findAllByActivoTrue(pageable);
        verifyNoInteractions(keycloakService, pacienteMapperService);
    }

    @Test
    @DisplayName("getByIdPaciente retorna la proyección cuando existe")
    void getByIdPaciente_Exitoso() {
        DetallesPacienteProjection projection = mock(DetallesPacienteProjection.class);
        when(pacienteRepository.findByIdPacienteAndActivoTrue(pacienteId)).thenReturn(Optional.of(projection));

        DetallesPacienteProjection resultado = pacienteService.getByIdPaciente(pacienteId);

        assertThat(resultado).isEqualTo(projection);
        verify(pacienteRepository).findByIdPacienteAndActivoTrue(pacienteId);
        verifyNoInteractions(keycloakService);
    }

    @Test
    @DisplayName("findByEmail retorna la proyección cuando existe")
    void findByEmail_Exitoso() {
        String email = "test@test.com";
        DetallesPacienteProjection projection = mock(DetallesPacienteProjection.class);
        when(pacienteRepository.findByActivoTrueAndEmail(email)).thenReturn(Optional.of(projection));

        DetallesPacienteProjection resultado = pacienteService.findByEmail(email);

        assertThat(resultado).isEqualTo(projection);
        verify(pacienteRepository).findByActivoTrueAndEmail(email);
        verifyNoInteractions(keycloakService);
    }

    @Test
    @DisplayName("findAllByTelefono retorna lista de proyecciones cuando existe")
    void findAllByTelefono_Exitoso() {
        String telefono = "8095550000";
        DetallesPacienteProjection projection = mock(DetallesPacienteProjection.class);
        when(pacienteRepository.findAllByActivoTrueAndTelefono(telefono)).thenReturn(Optional.of(List.of(projection)));

        List<DetallesPacienteProjection> resultado = pacienteService.findAllByTelefono(telefono);

        assertThat(resultado).hasSize(1).containsExactly(projection);
        verify(pacienteRepository).findAllByActivoTrueAndTelefono(telefono);
        verifyNoInteractions(keycloakService);
    }

    @Test
    @DisplayName("findByIdAuthentication retorna entidad cuando existe")
    void findByIdAuthentication_Exitoso() {
        when(pacienteRepository.findByIdAuthentication(authId)).thenReturn(Optional.of(paciente));

        Paciente resultado = pacienteService.findByIdAuthentication(authId);

        assertThat(resultado).isEqualTo(paciente);
        verify(pacienteRepository).findByIdAuthentication(authId);
        verifyNoInteractions(keycloakService);
    }

    @Test
    @DisplayName("findByCedula lanza EntityNotFoundException si no existe")
    void findByCedula_NoExiste_LanzaException() {
        String cedula = "40200000001";
        when(pacienteRepository.findByCedulaAndActivoTrue(cedula)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.findByCedula(cedula))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Ningun paciente encntrado con la cedula: " + cedula);

        verify(pacienteRepository).findByCedulaAndActivoTrue(cedula);
        verifyNoInteractions(keycloakService);
    }
}