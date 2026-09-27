package com.microservice.users.service;

import com.microservice.users.dto.doctor.CreateDcotorDto;
import com.microservice.users.dto.doctor.DetalleDoctorDto;
import com.microservice.users.dto.doctor.UpdateDoctorDto;
import com.microservice.users.dto.doctor.horaiosAtencion.CreateHorarioAtencionDto;
import com.microservice.users.dto.user.UpdateUserWithNoRolesDto;
import com.microservice.users.exception.custom.AuthIntegrationException;
import com.microservice.users.exception.custom.CriticalRollbackException;
import com.microservice.users.exception.custom.PersistenceCompensatedException;
import com.microservice.users.mapper.DoctorMapperService;
import com.microservice.users.mapper.HorarioATencionMapperService;
import com.microservice.users.model.Doctor;
import com.microservice.users.model.Especialidad;
import com.microservice.users.model.HorarioAtencion;
import com.microservice.users.projections.doctor.DetallesDoctorProjection;
import com.microservice.users.repository.DoctorRepository;
import com.microservice.users.repository.EspecialidadRepository;
import com.microservice.users.repository.HorarioAtencionRepository;
import com.microservice.users.service.doctor.DoctorServiceImpl;
import com.microservice.users.service.keycloak.IkeycloakService;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private DoctorMapperService doctorMapperService;
    @Mock
    private IkeycloakService keycloakService;
    @Mock
    private HorarioAtencionRepository horarioAtencionRepository;
    @Mock
    private HorarioATencionMapperService horarioATencionMapperService;
    @Mock
    private EspecialidadRepository especialidadRepository;

    @InjectMocks
    private DoctorServiceImpl doctorServiceImpl;

    private UUID doctorId;
    private UUID authId;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        authId = UUID.randomUUID();

        doctor = new Doctor();
        doctor.setIdDoctor(doctorId);
        doctor.setIdAuthentication(authId);
        doctor.setActivo(true);
        doctor.setEspecialidades(new HashSet<>());
        doctor.setHorarioAtencion(new ArrayList<>());
    }

    // =========================================================================
    // TESTS PARA CREATE DOCTOR
    // =========================================================================

    @Test
    @DisplayName("Debe crear un doctor exitosamente en Keycloak y BD con horarios y especialidades")
    void createDoctor_Exitoso() {
        CreateDcotorDto createDto = mock(CreateDcotorDto.class);
        DetalleDoctorDto detalleDto = mock(DetalleDoctorDto.class);
        CreateHorarioAtencionDto horarioDto = mock(CreateHorarioAtencionDto.class);
        HorarioAtencion horarioEntity = new HorarioAtencion();

        Integer especialidadId = 2;
        Especialidad especialidad = new Especialidad();
        especialidad.setIdEspecialidad(especialidadId);
        especialidad.setDoctors(new HashSet<>());

        when(createDto.horarioAtencion()).thenReturn(List.of(horarioDto));
        when(keycloakService.createUserDoctor(any())).thenReturn(authId.toString());
        when(doctorMapperService.registerDtoToEntity(createDto)).thenReturn(doctor);
        when(especialidadRepository.findAllById(any())).thenReturn(List.of(especialidad));
        when(horarioATencionMapperService.registerDtoToEntity(horarioDto)).thenReturn(horarioEntity);
        when(doctorMapperService.entityToDto(doctor)).thenReturn(detalleDto);

        DetalleDoctorDto resultado = doctorServiceImpl.createDoctor(createDto);

        assertThat(resultado).isNotNull().isEqualTo(detalleDto);
        verify(keycloakService).createUserDoctor(any());
        verify(doctorMapperService).registerDtoToEntity(createDto);
        verify(doctorRepository).save(doctor);
        verify(especialidadRepository).findAllById(any());
        verify(horarioAtencionRepository).saveAll(anyList());
        verify(doctorMapperService).entityToDto(doctor);
        verify(keycloakService, never()).deleteUser(anyString());
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException si Keycloak retorna null al crear doctor")
    void createDoctor_FalloAuthServerRetornaNull() {
        CreateDcotorDto createDto = mock(CreateDcotorDto.class);
        when(keycloakService.createUserDoctor(any())).thenReturn(null);

        assertThatThrownBy(() -> doctorServiceImpl.createDoctor(createDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Hubo un error al crear el nuevo doctor en el servicio de auntenticacion, el id de autenticacion es nulo");

        verify(keycloakService).createUserDoctor(any());
        verify(doctorMapperService, never()).registerDtoToEntity(any());
        verify(doctorRepository, never()).save(any());
        verify(horarioAtencionRepository, never()).saveAll(anyList());
        verify(keycloakService, never()).deleteUser(anyString());
    }

    @Test
    @DisplayName("Debe ejecutar compensación y lanzar PersistenceCompensatedException si falla la BD al crear doctor")
    void createDoctor_FalloDB_EjecutaCompensacion() {
        CreateDcotorDto createDto = mock(CreateDcotorDto.class);
        when(keycloakService.createUserDoctor(any())).thenReturn(authId.toString());
        when(doctorMapperService.registerDtoToEntity(createDto)).thenReturn(doctor);
        doThrow(new RuntimeException("Error de duplicidad en Postgres")).when(doctorRepository).save(doctor);

        assertThatThrownBy(() -> doctorServiceImpl.createDoctor(createDto))
                .isInstanceOf(PersistenceCompensatedException.class)
                .hasMessage("Hubo un problema al crear el nuevo doctor: Error de duplicidad en Postgres");

        verify(keycloakService).createUserDoctor(any());
        verify(doctorRepository).save(doctor);
        verify(keycloakService).deleteUser(authId.toString());
        verify(horarioAtencionRepository, never()).saveAll(anyList());
        verify(doctorMapperService, never()).entityToDto(any());
    }

    @Test
    @DisplayName("Debe lanzar CriticalRollbackException si falla tanto la BD como el borrado de compensación en Keycloak")
    void createDoctor_FalloDB_FalloCompensacionKeycloak() {
        CreateDcotorDto createDto = mock(CreateDcotorDto.class);
        when(keycloakService.createUserDoctor(any())).thenReturn(authId.toString());
        when(doctorMapperService.registerDtoToEntity(createDto)).thenReturn(doctor);
        doThrow(new RuntimeException("Error en Postgres")).when(doctorRepository).save(doctor);
        doThrow(new RuntimeException("Keycloak caído")).when(keycloakService).deleteUser(authId.toString());

        assertThatThrownBy(() -> doctorServiceImpl.createDoctor(createDto))
                .isInstanceOf(CriticalRollbackException.class)
                .hasMessage("Error al eliminar el doctor con id_autorization: " + authId);

        verify(keycloakService).createUserDoctor(any());
        verify(doctorRepository).save(doctor);
        verify(keycloakService).deleteUser(authId.toString());
        verify(doctorMapperService, never()).entityToDto(any());
    }

    // =========================================================================
    // TESTS PARA UPDATE DOCTOR
    // =========================================================================

    @Test
    @DisplayName("Update exitoso actualizando datos básicos y lista de especialidades")
    void updateDoctor_Exitoso() {
        UpdateDoctorDto updateDto = mock(UpdateDoctorDto.class);
        DetalleDoctorDto detalleDto = mock(DetalleDoctorDto.class);
        UpdateUserWithNoRolesDto snapShot = mock(UpdateUserWithNoRolesDto.class);
        UpdateUserWithNoRolesDto nuevosDatosUser = mock(UpdateUserWithNoRolesDto.class);
        String authIdStr = authId.toString();

        Integer espId = 2;
        Especialidad nuevaEsp = new Especialidad();
        nuevaEsp.setIdEspecialidad(espId);
        nuevaEsp.setDoctors(new HashSet<>());

        when(updateDto.id_Especialidades()).thenReturn(Set.of(espId));
        when(updateDto.userUpdate()).thenReturn(nuevosDatosUser);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(keycloakService.getUserUpdateData(authIdStr)).thenReturn(snapShot);
        when(especialidadRepository.findAllById(any())).thenReturn(List.of(nuevaEsp));
        when(doctorMapperService.entityToDto(doctor)).thenReturn(detalleDto);

        DetalleDoctorDto resultado = doctorServiceImpl.updateDoctor(doctorId, updateDto);

        assertThat(resultado).isNotNull().isEqualTo(detalleDto);
        assertThat(doctor.getEspecialidades()).containsExactly(nuevaEsp);

        verify(doctorRepository).findById(doctorId);
        verify(keycloakService).getUserUpdateData(authIdStr);
        verify(keycloakService).updateUserNoRoles(authIdStr, nuevosDatosUser);
        verify(doctorMapperService).update(updateDto, doctor);
        verify(especialidadRepository).findAllById(Set.of(espId));
        verify(doctorRepository).flush();
        verify(doctorMapperService).entityToDto(doctor);
        verify(keycloakService, never()).updateUserNoRoles(authIdStr, snapShot);
    }

    @Test
    @DisplayName("Update lanza AuthIntegrationException si no puede obtener el snapshot inicial de Keycloak")
    void updateDoctor_FalloSnapshotKeycloak() {
        UpdateDoctorDto updateDto = mock(UpdateDoctorDto.class);
        String authIdStr = authId.toString();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        doThrow(new RuntimeException("Keycloak error"))
                .when(keycloakService).getUserUpdateData(authIdStr);

        assertThatThrownBy(() -> doctorServiceImpl.updateDoctor(doctorId, updateDto))
                .isInstanceOf(AuthIntegrationException.class)
                .hasMessage("Error usuario no encontrado en keycloak, id: " + doctorId);

        verify(doctorRepository).findById(doctorId);
        verify(keycloakService).getUserUpdateData(authIdStr);
        verify(keycloakService, never()).updateUserNoRoles(anyString(), any());
        verify(doctorMapperService, never()).update(any(), any());
        verify(doctorRepository, never()).flush();
    }

    @Test
    @DisplayName("Update lanza AuthIntegrationException si falla la actualización inicial en Keycloak")
    void updateDoctor_FalloActualizarKeycloak() {
        UpdateDoctorDto updateDto = mock(UpdateDoctorDto.class);
        UpdateUserWithNoRolesDto snapShot = mock(UpdateUserWithNoRolesDto.class);
        String authIdStr = authId.toString();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(keycloakService.getUserUpdateData(authIdStr)).thenReturn(snapShot);
        doThrow(new RuntimeException("Error en Keycloak"))
                .when(keycloakService).updateUserNoRoles(eq(authIdStr), any());

        assertThatThrownBy(() -> doctorServiceImpl.updateDoctor(doctorId, updateDto))
                .isInstanceOf(AuthIntegrationException.class)
                .hasMessage("Error al actualizar el Doctor en keycloak con id_auth: " + authId);

        verify(doctorRepository).findById(doctorId);
        verify(keycloakService).getUserUpdateData(authIdStr);
        verify(keycloakService).updateUserNoRoles(eq(authIdStr), any());
        verify(doctorMapperService, never()).update(any(), any());
        verify(especialidadRepository, never()).findAllById(any());
        verify(doctorRepository, never()).flush();
    }

    @Test
    @DisplayName("Update ejecuta rollback y lanza PersistenceCompensatedException si alguna especialidad no existe en BD")
    void updateDoctor_EspecialidadNoEncontrada_EjecutaRollback() {
        UpdateDoctorDto updateDto = mock(UpdateDoctorDto.class);
        UpdateUserWithNoRolesDto snapShot = mock(UpdateUserWithNoRolesDto.class);
        UpdateUserWithNoRolesDto nuevosDatosUser = mock(UpdateUserWithNoRolesDto.class);
        String authIdStr = authId.toString();
        Integer espInexistenteId = 999;

        when(updateDto.userUpdate()).thenReturn(nuevosDatosUser);
        when(updateDto.id_Especialidades()).thenReturn(Set.of(espInexistenteId));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(keycloakService.getUserUpdateData(authIdStr)).thenReturn(snapShot);
        when(especialidadRepository.findAllById(any())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> doctorServiceImpl.updateDoctor(doctorId, updateDto))
                .isInstanceOf(PersistenceCompensatedException.class)
                .hasMessage("Ha ocurrido un errror durante la actulizacion de usuario del usuarios :" + doctorId + "el rolback ha sido exitoso");

        verify(keycloakService).getUserUpdateData(authIdStr);
        verify(keycloakService).updateUserNoRoles(authIdStr, nuevosDatosUser);
        verify(doctorMapperService).update(updateDto, doctor);
        verify(especialidadRepository).findAllById(Set.of(espInexistenteId));
        verify(keycloakService).updateUserNoRoles(authIdStr, snapShot);
        verify(doctorRepository, never()).flush();
        verify(doctorMapperService, never()).entityToDto(any());
    }

    @Test
    @DisplayName("Update ejecuta rollback del snapshot y lanza PersistenceCompensatedException si falla el flush en BD")
    void updateDoctor_FalloEnDB_EjecutaRollbackSnapshot() {
        UpdateDoctorDto updateDto = mock(UpdateDoctorDto.class);
        UpdateUserWithNoRolesDto snapShot = mock(UpdateUserWithNoRolesDto.class);
        UpdateUserWithNoRolesDto nuevosDatosUser = mock(UpdateUserWithNoRolesDto.class);
        String authIdStr = authId.toString();

        when(updateDto.userUpdate()).thenReturn(nuevosDatosUser);
        when(updateDto.id_Especialidades()).thenReturn(null);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(keycloakService.getUserUpdateData(authIdStr)).thenReturn(snapShot);
        doThrow(new RuntimeException("Constraint error")).when(doctorRepository).flush();

        assertThatThrownBy(() -> doctorServiceImpl.updateDoctor(doctorId, updateDto))
                .isInstanceOf(PersistenceCompensatedException.class)
                .hasMessage("Ha ocurrido un errror durante la actulizacion de usuario del usuarios :" + doctorId + "el rolback ha sido exitoso");

        verify(keycloakService).getUserUpdateData(authIdStr);
        verify(keycloakService).updateUserNoRoles(authIdStr, nuevosDatosUser);
        verify(doctorMapperService).update(updateDto, doctor);
        verify(doctorRepository).flush();
        verify(keycloakService).updateUserNoRoles(authIdStr, snapShot);
        verify(doctorMapperService, never()).entityToDto(any());
    }

    @Test
    @DisplayName("Update lanza CriticalRollbackException si falla la BD y también falla el rollback en Keycloak")
    void updateDoctor_FalloEnDB_FalloRollbackKeycloak() {
        UpdateDoctorDto updateDto = mock(UpdateDoctorDto.class);
        UpdateUserWithNoRolesDto snapShot = mock(UpdateUserWithNoRolesDto.class);
        UpdateUserWithNoRolesDto nuevosDatosUser = mock(UpdateUserWithNoRolesDto.class);
        String authIdStr = authId.toString();

        when(updateDto.userUpdate()).thenReturn(nuevosDatosUser);
        when(updateDto.id_Especialidades()).thenReturn(null);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(keycloakService.getUserUpdateData(authIdStr)).thenReturn(snapShot);

        doNothing()
                .doThrow(new RuntimeException("Error en Keycloak"))
                .when(keycloakService).updateUserNoRoles(eq(authIdStr), any());
        doThrow(new RuntimeException("Error en BD")).when(doctorRepository).flush();

        assertThatThrownBy(() -> doctorServiceImpl.updateDoctor(doctorId, updateDto))
                .isInstanceOf(CriticalRollbackException.class)
                .hasMessage("Error critico hacer rolback en keycloak al usuario con id: " + authIdStr);

        verify(keycloakService).getUserUpdateData(authIdStr);
        verify(keycloakService).updateUserNoRoles(authIdStr, nuevosDatosUser);
        verify(doctorRepository).flush();
        verify(keycloakService).updateUserNoRoles(authIdStr, snapShot);
        verify(doctorMapperService, never()).entityToDto(any());
    }

    // =========================================================================
    // TESTS PARA DESACTIVAR Y REACTIVAR DOCTOR
    // =========================================================================

    @Test
    @DisplayName("Debe desactivar el doctor exitosamente en Keycloak y cambiar su estado a inactivo")
    void desactivarDoctor_Exitoso() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        doctorServiceImpl.desactivarDoctor(doctorId);

        assertThat(doctor.getActivo()).isFalse();
        verify(doctorRepository).findById(doctorId);
        verify(keycloakService).changeEnabledState(authId.toString(), false);
    }

    @Test
    @DisplayName("Debe lanzar AuthIntegrationException si Keycloak falla al desactivar doctor")
    void desactivarDoctor_FalloKeycloak() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        doThrow(new RuntimeException("Keycloak timeout"))
                .when(keycloakService).changeEnabledState(authId.toString(), false);

        assertThatThrownBy(() -> doctorServiceImpl.desactivarDoctor(doctorId))
                .isInstanceOf(AuthIntegrationException.class)
                .hasMessageContaining("Error al deactivar user doctor");

        assertThat(doctor.getActivo()).isTrue();
        verify(doctorRepository).findById(doctorId);
        verify(keycloakService).changeEnabledState(authId.toString(), false);
    }

    @Test
    @DisplayName("Debe reactivar el doctor exitosamente en Keycloak y cambiar su estado a activo")
    void reactivarDoctor_Exitoso() {
        doctor.setActivo(false);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        doctorServiceImpl.reactivarDoctor(doctorId);

        assertThat(doctor.getActivo()).isTrue();
        verify(doctorRepository).findById(doctorId);
        verify(keycloakService).changeEnabledState(authId.toString(), true);
    }

    @Test
    @DisplayName("Debe lanzar AuthIntegrationException si Keycloak falla al reactivar doctor")
    void reactivarDoctor_FalloKeycloak() {
        doctor.setActivo(false);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        doThrow(new RuntimeException("Keycloak timeout"))
                .when(keycloakService).changeEnabledState(authId.toString(), true);

        assertThatThrownBy(() -> doctorServiceImpl.reactivarDoctor(doctorId))
                .isInstanceOf(AuthIntegrationException.class)
                .hasMessageContaining("Error activating user doctor");

        assertThat(doctor.getActivo()).isFalse();
        verify(doctorRepository).findById(doctorId);
        verify(keycloakService).changeEnabledState(authId.toString(), true);
    }

    // =========================================================================
    // TESTS PARA CONSULTAS Y PROYECCIONES (READ-ONLY)
    // =========================================================================

    @Test
    @DisplayName("findAllACtivo debe retornar página de proyecciones")
    void findAllACtivo_Exitoso() {
        Pageable pageable = mock(Pageable.class);
        @SuppressWarnings("unchecked")
        Page<DetallesDoctorProjection> pageMock = mock(Page.class);
        when(doctorRepository.findAllByActivoTrue(pageable)).thenReturn(pageMock);

        Page<DetallesDoctorProjection> resultado = doctorServiceImpl.findAllACtivo(pageable);

        assertThat(resultado).isEqualTo(pageMock);
        verify(doctorRepository).findAllByActivoTrue(pageable);
        verifyNoInteractions(keycloakService, doctorMapperService);
    }

    @Test
    @DisplayName("findALlByEspecialidad debe retornar página filtrada por nombre de especialidad")
    void findALlByEspecialidad_Exitoso() {
        String especialidad = "Pediatria";
        Pageable pageable = mock(Pageable.class);
        @SuppressWarnings("unchecked")
        Page<DetallesDoctorProjection> pageMock = mock(Page.class);
        when(doctorRepository.findAllByEspecialidades_NombreEspecialidad(especialidad, pageable)).thenReturn(pageMock);

        Page<DetallesDoctorProjection> resultado = doctorServiceImpl.findALlByEspecialidad(especialidad, pageable);

        assertThat(resultado).isEqualTo(pageMock);
        verify(doctorRepository).findAllByEspecialidades_NombreEspecialidad(especialidad, pageable);
        verifyNoInteractions(keycloakService, doctorMapperService);
    }

    @Test
    @DisplayName("findAllByIdDoctor retorna proyección cuando el doctor existe")
    void findAllByIdDoctor_Exitoso() {
        DetallesDoctorProjection projection = mock(DetallesDoctorProjection.class);
        when(doctorRepository.findAllByIdDoctor(doctorId)).thenReturn(Optional.of(projection));

        DetallesDoctorProjection resultado = doctorServiceImpl.findAllByIdDoctor(doctorId);

        assertThat(resultado).isEqualTo(projection);
        verify(doctorRepository).findAllByIdDoctor(doctorId);
        verifyNoInteractions(keycloakService);
    }

    @Test
    @DisplayName("findAllByIdDoctor lanza EntityNotFoundException cuando no existe")
    void findAllByIdDoctor_NoExiste() {
        when(doctorRepository.findAllByIdDoctor(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorServiceImpl.findAllByIdDoctor(doctorId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Doctor no encontrado con el id: " + doctorId);

        verify(doctorRepository).findAllByIdDoctor(doctorId);
        verifyNoInteractions(keycloakService);
    }

    @Test
    @DisplayName("findByExequatur retorna proyección cuando existe")
    void findByExequatur_Exitoso() {
        String exequatur = "EX-12345";
        DetallesDoctorProjection projection = mock(DetallesDoctorProjection.class);
        when(doctorRepository.findByExequatur(exequatur)).thenReturn(Optional.of(projection));

        DetallesDoctorProjection resultado = doctorServiceImpl.findByExequatur(exequatur);

        assertThat(resultado).isEqualTo(projection);
        verify(doctorRepository).findByExequatur(exequatur);
        verifyNoInteractions(keycloakService);
    }

    @Test
    @DisplayName("findByExequatur lanza EntityNotFoundException cuando no existe")
    void findByExequatur_NoExiste() {
        String exequatur = "EX-999";
        when(doctorRepository.findByExequatur(exequatur)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorServiceImpl.findByExequatur(exequatur))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Doctor no encontrado con el exequatur: " + exequatur);

        verify(doctorRepository).findByExequatur(exequatur);
        verifyNoInteractions(keycloakService);
    }

    @Test
    @DisplayName("findBYIdAuthentication lanza EntityNotFoundException cuando no existe")
    void findBYIdAuthentication_NoExiste() {
        when(doctorRepository.findByIdAuthentication(authId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorServiceImpl.findBYIdAuthentication(authId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Doctor no encontrado con el id_Authentication: " + authId);

        verify(doctorRepository).findByIdAuthentication(authId);
        verifyNoInteractions(keycloakService);
    }

    @Test
    @DisplayName("findByCedula lanza EntityNotFoundException cuando no existe")
    void findByCedula_NoExiste() {
        String cedula = "40212345678";
        when(doctorRepository.findByCedula(cedula)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorServiceImpl.findByCedula(cedula))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Doctor no encontrado con cedula: " + cedula);

        verify(doctorRepository).findByCedula(cedula);
        verifyNoInteractions(keycloakService);
    }
}