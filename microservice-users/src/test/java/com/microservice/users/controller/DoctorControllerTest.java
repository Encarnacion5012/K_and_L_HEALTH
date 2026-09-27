package com.microservice.users.controller;

import com.microservice.users.dto.doctor.CreateDcotorDto;
import com.microservice.users.dto.doctor.DetalleDoctorDto;
import com.microservice.users.dto.doctor.UpdateDoctorDto;
import com.microservice.users.projections.doctor.DetallesDoctorProjection;
import com.microservice.users.service.doctor.IDoctorService;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorControllerTest {

    @Mock
    private IDoctorService doctorService;

    @InjectMocks
    private DoctorController doctorController;

    private UUID doctorId;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
    }

    // =========================================================================
    // 1. POST /api/doctor/create (cretaeDoctor)
    // =========================================================================

    @Test
    @DisplayName("cretaeDoctor debe retornar 201 CREATED y el DetalleDoctorDto creado")
    void cretaeDoctor_Exitoso() {
        CreateDcotorDto createDto = mock(CreateDcotorDto.class);
        DetalleDoctorDto detalleDto = mock(DetalleDoctorDto.class);

        when(doctorService.createDoctor(createDto)).thenReturn(detalleDto);

        ResponseEntity<DetalleDoctorDto> response = doctorController.cretaeDoctor(createDto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull().isEqualTo(detalleDto);

        verify(doctorService).createDoctor(createDto);
        verify(doctorService, never()).updateDoctor(any(), any());
        verify(doctorService, never()).desactivarDoctor(any());
        verifyNoMoreInteractions(doctorService);
    }

    @Test
    @DisplayName("cretaeDoctor debe propagar excepción si el servicio falla al crear")
    void cretaeDoctor_FalloServicio_PropagaExcepcion() {
        CreateDcotorDto createDto = mock(CreateDcotorDto.class);
        when(doctorService.createDoctor(createDto)).thenThrow(new RuntimeException("Error al crear doctor"));

        assertThatThrownBy(() -> doctorController.cretaeDoctor(createDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error al crear doctor");

        verify(doctorService).createDoctor(createDto);
        verify(doctorService, never()).updateDoctor(any(), any());
        verifyNoMoreInteractions(doctorService);
    }

    // =========================================================================
    // 2. GET /api/doctor/all (findAll)
    // =========================================================================

    @Test
    @DisplayName("findAll debe retornar 200 OK y la página de doctores activos")
    void findAll_Exitoso() {
        Pageable pageable = mock(Pageable.class);
        @SuppressWarnings("unchecked")
        Page<DetallesDoctorProjection> pageMock = mock(Page.class);

        when(doctorService.findAllACtivo(pageable)).thenReturn(pageMock);

        ResponseEntity<Page<DetallesDoctorProjection>> response = doctorController.findAll(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEqualTo(pageMock);

        verify(doctorService).findAllACtivo(pageable);
        verify(doctorService, never()).findALlByEspecialidad(anyString(), any());
        verify(doctorService, never()).createDoctor(any());
        verifyNoMoreInteractions(doctorService);
    }

    // =========================================================================
    // 3. GET /api/doctor/especiality/{especiality_Name} (findByEspeciality)
    // =========================================================================

    @Test
    @DisplayName("findByEspeciality debe retornar 200 OK y la página filtrada por especialidad")
    void findByEspeciality_Exitoso() {
        String especialidad = "Cardiologia";
        Pageable pageable = mock(Pageable.class);
        @SuppressWarnings("unchecked")
        Page<DetallesDoctorProjection> pageMock = mock(Page.class);

        when(doctorService.findALlByEspecialidad(especialidad, pageable)).thenReturn(pageMock);

        ResponseEntity<Page<DetallesDoctorProjection>> response = doctorController.findByEspeciality(especialidad, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEqualTo(pageMock);

        verify(doctorService).findALlByEspecialidad(especialidad, pageable);
        verify(doctorService, never()).findAllACtivo(any());
        verifyNoMoreInteractions(doctorService);
    }

    // =========================================================================
    // 4. GET /api/doctor/exequatur/{exequatur} (findByExequatur)
    // =========================================================================

    @Test
    @DisplayName("findByExequatur debe retornar 200 OK y la proyección del doctor")
    void findByExequatur_Exitoso() {
        String exequatur = "EX-12345";
        DetallesDoctorProjection projection = mock(DetallesDoctorProjection.class);

        when(doctorService.findByExequatur(exequatur)).thenReturn(projection);

        ResponseEntity<DetallesDoctorProjection> response = doctorController.findByExequatur(exequatur);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEqualTo(projection);

        verify(doctorService).findByExequatur(exequatur);
        verify(doctorService, never()).findByCedula(anyString());
        verify(doctorService, never()).findAllByIdDoctor(any());
        verifyNoMoreInteractions(doctorService);
    }

    @Test
    @DisplayName("findByExequatur debe propagar EntityNotFoundException cuando no existe")
    void findByExequatur_NoExiste_LanzaExcepcion() {
        String exequatur = "EX-99999";
        when(doctorService.findByExequatur(exequatur))
                .thenThrow(new EntityNotFoundException("Doctor no encontrado con el exequatur: " + exequatur));

        assertThatThrownBy(() -> doctorController.findByExequatur(exequatur))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Doctor no encontrado con el exequatur: " + exequatur);

        verify(doctorService).findByExequatur(exequatur);
        verifyNoMoreInteractions(doctorService);
    }

    // =========================================================================
    // 5. PUT /api/doctor/desctivate/{id_doctor} (desactivateDoctor)
    // =========================================================================

    @Test
    @DisplayName("desactivateDoctor debe retornar 204 NO_CONTENT sin cuerpo")
    void desactivateDoctor_Exitoso() {
        doNothing().when(doctorService).desactivarDoctor(doctorId);

        ResponseEntity<Void> response = doctorController.desactivateDoctor(doctorId);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(doctorService).desactivarDoctor(doctorId);
        verify(doctorService, never()).reactivarDoctor(any());
        verify(doctorService, never()).updateDoctor(any(), any());
        verifyNoMoreInteractions(doctorService);
    }

    @Test
    @DisplayName("desactivateDoctor debe propagar excepción si el doctor no existe o falla Keycloak")
    void desactivateDoctor_Fallo_LanzaExcepcion() {
        doThrow(new EntityNotFoundException("Doctor no encontrado con el id: " + doctorId))
                .when(doctorService).desactivarDoctor(doctorId);

        assertThatThrownBy(() -> doctorController.desactivateDoctor(doctorId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Doctor no encontrado con el id: " + doctorId);

        verify(doctorService).desactivarDoctor(doctorId);
        verify(doctorService, never()).reactivarDoctor(any());
        verifyNoMoreInteractions(doctorService);
    }

    // =========================================================================
    // 6. PUT /api/doctor/reactivate/{id_doctor} (reactivateDoctor)
    // =========================================================================

    @Test
    @DisplayName("reactivateDoctor debe retornar 204 NO_CONTENT sin cuerpo")
    void reactivateDoctor_Exitoso() {
        doNothing().when(doctorService).reactivarDoctor(doctorId);

        ResponseEntity<Void> response = doctorController.reactivateDoctor(doctorId);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(doctorService).reactivarDoctor(doctorId);
        verify(doctorService, never()).desactivarDoctor(any());
        verify(doctorService, never()).updateDoctor(any(), any());
        verifyNoMoreInteractions(doctorService);
    }

    // =========================================================================
    // 7. GET /api/doctor/find_by_cedula/{cedula_doctor} (findByCedula)
    // =========================================================================

    @Test
    @DisplayName("findByCedula debe retornar 200 OK y la proyección del doctor")
    void findByCedula_Exitoso() {
        String cedula = "40212345678";
        DetallesDoctorProjection projection = mock(DetallesDoctorProjection.class);

        when(doctorService.findByCedula(cedula)).thenReturn(projection);

        ResponseEntity<DetallesDoctorProjection> response = doctorController.findByCedula(cedula);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEqualTo(projection);

        verify(doctorService).findByCedula(cedula);
        verify(doctorService, never()).findByExequatur(anyString());
        verify(doctorService, never()).findAllByIdDoctor(any());
        verifyNoMoreInteractions(doctorService);
    }

    // =========================================================================
    // 8. GET /api/doctor/find_by_id/{id_Doctor} (findById)
    // =========================================================================

    @Test
    @DisplayName("findById debe retornar 200 OK y la proyección del doctor")
    void findById_Exitoso() {
        DetallesDoctorProjection projection = mock(DetallesDoctorProjection.class);

        when(doctorService.findAllByIdDoctor(doctorId)).thenReturn(projection);

        ResponseEntity<DetallesDoctorProjection> response = doctorController.findById(doctorId);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEqualTo(projection);

        verify(doctorService).findAllByIdDoctor(doctorId);
        verify(doctorService, never()).findByCedula(anyString());
        verify(doctorService, never()).findByExequatur(anyString());
        verifyNoMoreInteractions(doctorService);
    }

    @Test
    @DisplayName("findById debe propagar EntityNotFoundException si el doctor no existe")
    void findById_NoExiste_LanzaExcepcion() {
        when(doctorService.findAllByIdDoctor(doctorId))
                .thenThrow(new EntityNotFoundException("Doctor no encontrado con el id: " + doctorId));

        assertThatThrownBy(() -> doctorController.findById(doctorId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Doctor no encontrado con el id: " + doctorId);

        verify(doctorService).findAllByIdDoctor(doctorId);
        verifyNoMoreInteractions(doctorService);
    }

    // =========================================================================
    // 9. PUT /api/doctor/update/{id_doctor} (updateDotor)
    // =========================================================================

    @Test
    @DisplayName("updateDotor debe retornar 200 OK y el DetalleDoctorDto actualizado")
    void updateDotor_Exitoso() {
        UpdateDoctorDto updateDto = mock(UpdateDoctorDto.class);
        DetalleDoctorDto detalleActualizado = mock(DetalleDoctorDto.class);

        when(doctorService.updateDoctor(doctorId, updateDto)).thenReturn(detalleActualizado);

        ResponseEntity<DetalleDoctorDto> response = doctorController.updateDotor(doctorId, updateDto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEqualTo(detalleActualizado);

        verify(doctorService).updateDoctor(doctorId, updateDto);
        verify(doctorService, never()).createDoctor(any());
        verify(doctorService, never()).desactivarDoctor(any());
        verifyNoMoreInteractions(doctorService);
    }

    @Test
    @DisplayName("updateDotor debe propagar RuntimeException si falla la actualización")
    void updateDotor_FalloServicio_LanzaExcepcion() {
        UpdateDoctorDto updateDto = mock(UpdateDoctorDto.class);

        when(doctorService.updateDoctor(doctorId, updateDto))
                .thenThrow(new RuntimeException("Ha ocurrido un errror durante la actulizacion"));

        assertThatThrownBy(() -> doctorController.updateDotor(doctorId, updateDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ha ocurrido un errror durante la actulizacion");

        verify(doctorService).updateDoctor(doctorId, updateDto);
        verify(doctorService, never()).createDoctor(any());
        verifyNoMoreInteractions(doctorService);
    }
}