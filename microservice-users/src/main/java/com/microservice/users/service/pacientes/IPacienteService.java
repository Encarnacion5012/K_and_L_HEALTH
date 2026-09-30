package com.microservice.users.service.pacientes;

import com.microservice.users.dto.paciente.CreatePacienteDto;
import com.microservice.users.dto.paciente.DetallePacienteDto;
import com.microservice.users.dto.paciente.UpdatePacienteDto;
import com.microservice.users.model.Paciente;
import com.microservice.users.projections.pacientes.DetallesPacienteProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface IPacienteService {
DetallePacienteDto create(CreatePacienteDto createPacienteDTO);
Page<DetallesPacienteProjection> findAllActivo(Pageable pageable);
DetallePacienteDto update(UUID id, UpdatePacienteDto updateDTO);
void desactivarPaciente(UUID id);
void reactivarPaciente(UUID id);
DetallesPacienteProjection getByIdPaciente(UUID idPaciente);
DetallesPacienteProjection findByEmail(String email);
List<DetallesPacienteProjection> findAllByTelefono(String telefono);
Paciente findByIdAuthentication(UUID id_authentication);
DetallesPacienteProjection findByCedula(String cedula);
boolean existsByIdPaciente(UUID idPaciente);


}
