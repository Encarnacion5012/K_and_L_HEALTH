package com.microservice.users.service;

import com.microservice.users.dto.paciente.CreatePacienteDto;
import com.microservice.users.dto.paciente.DetallePacienteDto;
import com.microservice.users.dto.paciente.UpdatePacienteDto;
import com.microservice.users.dto.user.UpdateUserWithNoRolesDto;
import com.microservice.users.mapper.PacienteMapperService;
import com.microservice.users.model.Paciente;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.*;

import com.microservice.users.projections.pacientes.DetallesPacienteProjection;
import com.microservice.users.repository.PacienteRepository;
import com.microservice.users.service.keycloak.IkeycloakService;
import com.microservice.users.service.pacientes.PacienteServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

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


    UUID pacienteId;
    UUID authId;
    Paciente paciente;


    @BeforeEach
    public void setUp() {
        pacienteId = UUID.randomUUID();
        this.authId = UUID.randomUUID();
        this.paciente = new Paciente();
        this.paciente.setIdPaciente(pacienteId);
        this.paciente.setIdAuthentication(authId);
        this.paciente.setActivo(true);
    }


    //Test para creacion y compensasion en keycloak al ocurrir un error

    @Test
    @DisplayName("Debe crear un paciente exitosamente en el servicio de authenticacion y DB")
    void create_Existoso(){

        CreatePacienteDto createPacienteDto = mock(CreatePacienteDto.class);
        DetallePacienteDto detallePacienteDto = mock(DetallePacienteDto.class);

        when(keycloakService.createUserPacinte(any())).thenReturn(authId.toString());
        when(pacienteMapperService.createDTOtoEntity(createPacienteDto)).thenReturn(paciente);
        when(pacienteRepository.save(paciente)).thenReturn(paciente);
        when(pacienteMapperService.entityToDTO(paciente)).thenReturn(detallePacienteDto);

        DetallePacienteDto resultado = pacienteService.create(createPacienteDto);

        assertThat(resultado).isNotNull().isEqualTo(detallePacienteDto);
        verify(keycloakService).createUserPacinte(any());
        verify(pacienteRepository).save(paciente);
        verify(keycloakService, never()).deleteUser(anyString());
    }



    @Test
    @DisplayName("Debe lanzar una exepcion si el auth sever devuelve un id Null")

    void create_FalloAuthServerRetornaNull(){
        CreatePacienteDto createPacienteDto = mock(CreatePacienteDto.class);

        when(keycloakService.createUserPacinte(any())).thenReturn(null);

        assertThatThrownBy(() -> pacienteService.create(createPacienteDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No se puede crear un Paciente, ha habido un error")
        ;

        verify(pacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe hacer la compensacion si ocurre un error en la db")
    void create_FalloCompensacionEjecucionCompensacion(){
        CreatePacienteDto createPacienteDto = mock(CreatePacienteDto.class);
        when(keycloakService.createUserPacinte(any())).thenReturn(authId.toString());
        when(pacienteMapperService.createDTOtoEntity(createPacienteDto)).thenReturn(paciente);
        doThrow(new RuntimeException("Error en postgres")).when(pacienteRepository).save(paciente);

        assertThatThrownBy(() -> pacienteService.create(createPacienteDto))
                .isInstanceOf(RuntimeException.class)
                        .hasMessage("Hubo un problema al crear el nuevo Paciente: Error en postgres");


        verify(keycloakService).deleteUser(authId.toString());
        verify(pacienteMapperService, never()).entityToDTO(any());
    }

    @Test
    @DisplayName("Bebe lanzar una exepcion al haber un error cuando se hace la compensacion")

    void create_FalloCompensacionServerRetornaError(){
        CreatePacienteDto createPacienteDto = mock(CreatePacienteDto.class);
        when(keycloakService.createUserPacinte(any())).thenReturn(authId.toString());
        when(pacienteMapperService.createDTOtoEntity(createPacienteDto)).thenReturn(paciente);
        doThrow(new RuntimeException("Error en postgres")).when(pacienteRepository).save(paciente);
        doThrow(new RuntimeException(authId.toString())).when(keycloakService).deleteUser(authId.toString());

        assertThatThrownBy(() -> pacienteService.create(createPacienteDto))
        .isInstanceOf(RuntimeException.class)
                .hasMessage("Error al eliminar el Paciente con id_autorization: " +  authId.toString());



    }

    //Tests para update
    @Test
    @DisplayName("Update exitoso")
    void update_UpdateExitoso(){
        UpdatePacienteDto updatePacienteDto = mock(UpdatePacienteDto.class);
        DetallePacienteDto detallePacienteDto = mock(DetallePacienteDto.class);

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

        when(pacienteMapperService.entityToDTO(paciente)).thenReturn(detallePacienteDto);

        DetallePacienteDto resultado = pacienteService.update(pacienteId, updatePacienteDto);

        assertThat(resultado).isNotNull().isEqualTo(detallePacienteDto);

        verify(keycloakService).updateUserNoRoles(paciente.getIdAuthentication().toString(), updatePacienteDto.userUpdate());
        verify(pacienteMapperService).updatePaciente(updatePacienteDto, paciente);
        verify(pacienteMapperService).entityToDTO(paciente);
        verify(pacienteRepository).flush();


    }

    @Test
    @DisplayName("Update fallo al traer snapshot keycloak")
    void update_FalloSnapshotKeycloak(){
        UpdatePacienteDto updatePacienteDto = mock(UpdatePacienteDto.class);

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

        doThrow(new RuntimeException( "Error al obtener el estado previo antes de actulizar con el usuario: "+ paciente.getIdAuthentication().toString()))
                .when(keycloakService).getUserUpdateData(paciente.getIdAuthentication().toString());

        assertThatThrownBy(() -> pacienteService.update(paciente.getIdPaciente(),  updatePacienteDto))
            .isInstanceOf(RuntimeException.class)
                .hasMessage("Error al obtener el estado previo antes de actulizar con el usuario: " + paciente.getIdAuthentication().toString());

        verify(keycloakService).getUserUpdateData(paciente.getIdAuthentication().toString());
        verify(pacienteMapperService, never()).updatePaciente(updatePacienteDto,paciente);
        verify(keycloakService, never()).updateUserNoRoles(paciente.getIdAuthentication().toString(),  updatePacienteDto.userUpdate());
        verify(pacienteRepository, never()).flush();
    }

    @Test
    @DisplayName("Update fallo al actualizar en keycloak")
    void update_FalloActualizarKeycloak(){
        UpdatePacienteDto updatePacienteDto = mock(UpdatePacienteDto.class);
        UpdateUserWithNoRolesDto snapShotUser = mock(UpdateUserWithNoRolesDto.class);

        String authId = paciente.getIdAuthentication().toString();
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(keycloakService.getUserUpdateData(paciente.getIdAuthentication().toString())).thenReturn(snapShotUser);
        doThrow(new RuntimeException("Error al actualizar el Paciente con id_autorization: " + paciente.getIdAuthentication().toString()))
                .when(keycloakService).updateUserNoRoles(eq(authId), any());

        assertThatThrownBy(() -> pacienteService.update(pacienteId,  updatePacienteDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error al actualizar el Paciente con id_autorization: " + paciente.getIdAuthentication().toString());

        verify(keycloakService).getUserUpdateData(paciente.getIdAuthentication().toString());
        verify(pacienteMapperService, never()).updatePaciente(any(),any());
        verify(keycloakService).updateUserNoRoles(eq(authId), any());
        verify(pacienteRepository, never()).flush();
    }

    @Test
    @DisplayName("Update fallo en BD, ejecuta compensación (rollback) del snapshot en Keycloak")
    void update_FalloEnDB_EjecutaCompensacionSnapshot() {
        // 1. Arrange
        UpdatePacienteDto updatePacienteDto = mock(UpdatePacienteDto.class);
        UpdateUserWithNoRolesDto nuevosDatosUser = mock(UpdateUserWithNoRolesDto.class);
        UpdateUserWithNoRolesDto snapShotUser = mock(UpdateUserWithNoRolesDto.class);
        String authIdStr = paciente.getIdAuthentication().toString();

        // Entrenamos el DTO para que devuelva la instancia específica de nuevosDatosUser
        when(updatePacienteDto.userUpdate()).thenReturn(nuevosDatosUser);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(keycloakService.getUserUpdateData(authIdStr)).thenReturn(snapShotUser);

        // Simulamos que la BD falla al hacer el flush
        doThrow(new RuntimeException("Error constraints Postgres")).when(pacienteRepository).flush();

        // 2. Act & Assert
        assertThatThrownBy(() -> pacienteService.update(pacienteId, updatePacienteDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ha ocurrido un error al actulizar la data del paciente");

        // 3. Verify: Ahora cada llamada se verifica con su objeto exacto (1 vez cada una)
        verify(keycloakService).updateUserNoRoles(authIdStr, nuevosDatosUser); // Intento inicial
        verify(keycloakService).updateUserNoRoles(authIdStr, snapShotUser);    // Rollback (compensación)
    }

    @Test
    @DisplayName("Update fallo en BD y también falla la compensación (rollback) en Keycloak")
    void update_FalloEnDB_FalloCompensacionSnapshot(){
        UpdatePacienteDto updatePacienteDto = mock(UpdatePacienteDto.class);
        UpdateUserWithNoRolesDto snapShotUser = mock(UpdateUserWithNoRolesDto.class);
        String authIdStr = paciente.getIdAuthentication().toString();

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(keycloakService.getUserUpdateData(authIdStr)).thenReturn(snapShotUser);

        // 1. Falla la BD
        doThrow(new RuntimeException("Error constraints")).when(pacienteRepository).flush();
        // 2. Falla también el rollback en Keycloak
        doThrow(new RuntimeException("Keycloak Caído")).when(keycloakService).updateUserNoRoles(authIdStr, snapShotUser);

        assertThatThrownBy(() -> pacienteService.update(pacienteId, updatePacienteDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error al actualizar el Paciente con id_autorization: " + paciente.getIdAuthentication());
    }

    // Tests para Consultas (Read Only)

    @Test
    @DisplayName("Debe buscar un paciente por ID y fallar si no existe (EntityNotFoundException)")
    void buscarCualquierMetodo_PacienteNoExiste_LanzaException() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.desactivarPaciente(pacienteId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Paciente no encontrado con el id: " + pacienteId);
    }

    @Test
    @DisplayName("getByIdPaciente retorna la proyección")
    void getByIdPaciente_Exitoso() {
        DetallesPacienteProjection projection = mock(DetallesPacienteProjection.class);
        when(pacienteRepository.findByIdPacienteAndActivoTrue(pacienteId)).thenReturn(Optional.of(projection));

        DetallesPacienteProjection resultado = pacienteService.getByIdPaciente(pacienteId);

        assertThat(resultado).isEqualTo(projection);
    }

    @Test
    @DisplayName("findByEmail retorna la proyección")
    void findByEmail_Exitoso() {
        String email = "test@test.com";
        DetallesPacienteProjection projection = mock(DetallesPacienteProjection.class);
        when(pacienteRepository.findByActivoTrueAndEmail(email)).thenReturn(Optional.of(projection));

        DetallesPacienteProjection resultado = pacienteService.findByEmail(email);

        assertThat(resultado).isEqualTo(projection);
    }

    @Test
    @DisplayName("findByCedula lanza excepción si no existe")
    void findByCedula_NoExiste_LanzaException() {
        String cedula = "40200000001";
        when(pacienteRepository.findByCedulaAndActivoTrue(cedula)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.findByCedula(cedula))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Ningun paciente encntrado con la cedula: " + cedula);
    }


}


