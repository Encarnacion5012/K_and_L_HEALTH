package com.microservice.users.controller;

import com.microservice.users.dto.paciente.CreatePacienteDto;
import com.microservice.users.dto.paciente.DetallePacienteDto;
import com.microservice.users.dto.paciente.UpdatePacienteDto;
import com.microservice.users.projections.pacientes.DetallesPacienteProjection;
import com.microservice.users.service.pacientes.IPacienteService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PacienteControllerTest {

    @Mock
    private IPacienteService pacienteService;

    @InjectMocks
    private PacienteController pacienteController;

    private UUID pacienteId;

    @BeforeEach
    void setUp() {
        pacienteId = UUID.randomUUID();
    }

    // =========================================================================
    // 1. POST /api/pacientes/create (create)
    // =========================================================================

    @Test
    @DisplayName("create debe retornar 201 CREATED, header Location con el ID y el DTO en el body")
    void create_Exitoso() {
        CreatePacienteDto createDto = mock(CreatePacienteDto.class);
        DetallePacienteDto detalleDto = mock(DetallePacienteDto.class);
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();

        when(detalleDto.idPaciente()).thenReturn(pacienteId);
        when(pacienteService.create(createDto)).thenReturn(detalleDto);

        ResponseEntity<DetallePacienteDto> response = pacienteController.create(createDto, uriBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull().isEqualTo(detalleDto);
        assertThat(response.getHeaders().getLocation())
                .isEqualTo(URI.create("api/pacientes/create/" + pacienteId));

        verify(pacienteService).create(createDto);
        verify(pacienteService, never()).update(any(), any());
        verify(pacienteService, never()).desactivarPaciente(any());
        verifyNoMoreInteractions(pacienteService);
    }

    @Test
    @DisplayName("create debe propagar excepción si el servicio falla")
    void create_FalloServicio_PropagaExcepcion() {
        CreatePacienteDto createDto = mock(CreatePacienteDto.class);
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();

        when(pacienteService.create(createDto))
                .thenThrow(new RuntimeException("No se puede crear un Paciente, ha habido un error"));

        assertThatThrownBy(() -> pacienteController.create(createDto, uriBuilder))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No se puede crear un Paciente, ha habido un error");

        verify(pacienteService).create(createDto);
        verify(pacienteService, never()).update(any(), any());
        verifyNoMoreInteractions(pacienteService);
    }

    // =========================================================================
    // 2. GET /api/pacientes/all (findAll)
    // =========================================================================

    @Test
    @DisplayName("findAll debe retornar 200 OK y la página de pacientes activos")
    void findAll_Exitoso() {
        Pageable pageable = mock(Pageable.class);
        @SuppressWarnings("unchecked")
        Page<DetallesPacienteProjection> pageMock = mock(Page.class);

        when(pacienteService.findAllActivo(pageable)).thenReturn(pageMock);

        ResponseEntity<Page<DetallesPacienteProjection>> response = pacienteController.findAll(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEqualTo(pageMock);

        verify(pacienteService).findAllActivo(pageable);
        verify(pacienteService, never()).create(any());
        verify(pacienteService, never()).getByIdPaciente(any());
        verifyNoMoreInteractions(pacienteService);
    }

    // =========================================================================
    // 3. PUT /api/pacientes/update/{id_paciente} (updatePaciente)
    // =========================================================================

    @Test
    @DisplayName("updatePaciente debe retornar 200 OK y el DetallePacienteDto actualizado")
    void updatePaciente_Exitoso() {
        UpdatePacienteDto updateDto = mock(UpdatePacienteDto.class);
        DetallePacienteDto detalleActualizado = mock(DetallePacienteDto.class);

        when(pacienteService.update(pacienteId, updateDto)).thenReturn(detalleActualizado);

        ResponseEntity<DetallePacienteDto> response = pacienteController.updatePaciente(updateDto, pacienteId);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEqualTo(detalleActualizado);

        verify(pacienteService).update(pacienteId, updateDto);
        verify(pacienteService, never()).create(any());
        verify(pacienteService, never()).desactivarPaciente(any());
        verifyNoMoreInteractions(pacienteService);
    }

    @Test
    @DisplayName("updatePaciente debe propagar RuntimeException cuando falla la actualización")
    void updatePaciente_FalloServicio_LanzaExcepcion() {
        UpdatePacienteDto updateDto = mock(UpdatePacienteDto.class);

        when(pacienteService.update(pacienteId, updateDto))
                .thenThrow(new RuntimeException("Ha ocurrido un error al actulizar la data del paciente"));

        assertThatThrownBy(() -> pacienteController.updatePaciente(updateDto, pacienteId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ha ocurrido un error al actulizar la data del paciente");

        verify(pacienteService).update(pacienteId, updateDto);
        verify(pacienteService, never()).create(any());
        verifyNoMoreInteractions(pacienteService);
    }

    // =========================================================================
    // 4. PUT /api/pacientes/descativate_paciente/{id_paciente} (descativatePaciente)
    // =========================================================================

    @Test
    @DisplayName("descativatePaciente debe retornar 204 NO_CONTENT sin body")
    void descativatePaciente_Exitoso() {
        doNothing().when(pacienteService).desactivarPaciente(pacienteId);

        ResponseEntity<Void> response = pacienteController.descativatePaciente(pacienteId);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(pacienteService).desactivarPaciente(pacienteId);
        verify(pacienteService, never()).reactivarPaciente(any());
        verify(pacienteService, never()).update(any(), any());
        verifyNoMoreInteractions(pacienteService);
    }

    @Test
    @DisplayName("descativatePaciente debe propagar excepción si el paciente no existe")
    void descativatePaciente_NoExiste_LanzaExcepcion() {
        doThrow(new EntityNotFoundException("Paciente no encontrado con el id: " + pacienteId))
                .when(pacienteService).desactivarPaciente(pacienteId);

        assertThatThrownBy(() -> pacienteController.descativatePaciente(pacienteId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Paciente no encontrado con el id: " + pacienteId);

        verify(pacienteService).desactivarPaciente(pacienteId);
        verify(pacienteService, never()).reactivarPaciente(any());
        verifyNoMoreInteractions(pacienteService);
    }

    // =========================================================================
    // 5. PUT /api/pacientes/reactivate_paciente/{id_paciente} (reactivatePaciente)
    // =========================================================================

    @Test
    @DisplayName("reactivatePaciente debe retornar 204 NO_CONTENT sin body")
    void reactivatePaciente_Exitoso() {
        doNothing().when(pacienteService).reactivarPaciente(pacienteId);

        ResponseEntity<Void> response = pacienteController.reactivatePaciente(pacienteId);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(pacienteService).reactivarPaciente(pacienteId);
        verify(pacienteService, never()).desactivarPaciente(any());
        verify(pacienteService, never()).update(any(), any());
        verifyNoMoreInteractions(pacienteService);
    }

    // =========================================================================
    // 6. GET /api/pacientes/find_by_id/{id_paciente} (findByIdPaciente)
    // =========================================================================

    @Test
    @DisplayName("findByIdPaciente debe retornar 200 OK y la proyección del paciente")
    void findByIdPaciente_Exitoso() {
        DetallesPacienteProjection projection = mock(DetallesPacienteProjection.class);
        when(pacienteService.getByIdPaciente(pacienteId)).thenReturn(projection);

        ResponseEntity<DetallesPacienteProjection> response = pacienteController.findByIdPaciente(pacienteId);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEqualTo(projection);

        verify(pacienteService).getByIdPaciente(pacienteId);
        verify(pacienteService, never()).findByEmail(anyString());
        verify(pacienteService, never()).findByCedula(anyString());
        verifyNoMoreInteractions(pacienteService);
    }

    @Test
    @DisplayName("findByIdPaciente debe propagar EntityNotFoundException si no existe")
    void findByIdPaciente_NoExiste_LanzaExcepcion() {
        when(pacienteService.getByIdPaciente(pacienteId))
                .thenThrow(new EntityNotFoundException("Paciente no encontrado"));

        assertThatThrownBy(() -> pacienteController.findByIdPaciente(pacienteId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Paciente no encontrado");

        verify(pacienteService).getByIdPaciente(pacienteId);
        verifyNoMoreInteractions(pacienteService);
    }

    // =========================================================================
    // 7. GET /api/pacientes/find_by_email/{email_paciente} (findByEmailPaciente)
    // =========================================================================

    @Test
    @DisplayName("findByEmailPaciente debe retornar 200 OK y la proyección del paciente")
    void findByEmailPaciente_Exitoso() {
        String email = "paciente@test.com";
        DetallesPacienteProjection projection = mock(DetallesPacienteProjection.class);
        when(pacienteService.findByEmail(email)).thenReturn(projection);

        ResponseEntity<DetallesPacienteProjection> response = pacienteController.findByEmailPaciente(email);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEqualTo(projection);

        verify(pacienteService).findByEmail(email);
        verify(pacienteService, never()).getByIdPaciente(any());
        verify(pacienteService, never()).findByCedula(anyString());
        verifyNoMoreInteractions(pacienteService);
    }

    // =========================================================================
    // 8. GET /api/pacientes/find_by_telefono/{telefono} (findByTelefonoPaciente)
    // =========================================================================

    @Test
    @DisplayName("findByTelefonoPaciente debe retornar 200 OK y la lista de proyecciones")
    void findByTelefonoPaciente_Exitoso() {
        String telefono = "8095551234";
        DetallesPacienteProjection projection = mock(DetallesPacienteProjection.class);
        when(pacienteService.findAllByTelefono(telefono)).thenReturn(List.of(projection));

        ResponseEntity<List<DetallesPacienteProjection>> response = pacienteController.findByTelefonoPaciente(telefono);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(1).containsExactly(projection);

        verify(pacienteService).findAllByTelefono(telefono);
        verify(pacienteService, never()).findByCedula(anyString());
        verify(pacienteService, never()).findByEmail(anyString());
        verifyNoMoreInteractions(pacienteService);
    }

    // =========================================================================
    // 9. GET /api/pacientes/find_by_cedula/{cedula} (findByCedulaPaciente)
    // =========================================================================

    @Test
    @DisplayName("findByCedulaPaciente debe retornar 200 OK y la proyección del paciente")
    void findByCedulaPaciente_Exitoso() {
        String cedula = "40212345678";
        DetallesPacienteProjection projection = mock(DetallesPacienteProjection.class);
        when(pacienteService.findByCedula(cedula)).thenReturn(projection);

        ResponseEntity<DetallesPacienteProjection> response = pacienteController.findByCedulaPaciente(cedula);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEqualTo(projection);

        verify(pacienteService).findByCedula(cedula);
        verify(pacienteService, never()).findAllByTelefono(anyString());
        verify(pacienteService, never()).getByIdPaciente(any());
        verifyNoMoreInteractions(pacienteService);
    }

    @Test
    @DisplayName("findByCedulaPaciente debe propagar EntityNotFoundException si la cédula no existe")
    void findByCedulaPaciente_NoExiste_LanzaExcepcion() {
        String cedula = "00000000000";
        when(pacienteService.findByCedula(cedula))
                .thenThrow(new EntityNotFoundException("Ningun paciente encntrado con la cedula: " + cedula));

        assertThatThrownBy(() -> pacienteController.findByCedulaPaciente(cedula))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Ningun paciente encntrado con la cedula: " + cedula);

        verify(pacienteService).findByCedula(cedula);
        verifyNoMoreInteractions(pacienteService);
    }
}