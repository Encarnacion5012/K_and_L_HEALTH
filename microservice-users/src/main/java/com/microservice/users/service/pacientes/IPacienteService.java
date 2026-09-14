package com.microservice.users.service.pacientes;

import com.microservice.users.dto.paciente.DetallePacienteDTO;
import com.microservice.users.dto.paciente.CreatePacienteDTO;
import com.microservice.users.dto.paciente.UpdatePacienteDTO;
import com.microservice.users.projections.pacientes.DetallesPacienteProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;


public interface IPacienteService {
DetallePacienteDTO create(CreatePacienteDTO createPacienteDTO);
Page<DetallesPacienteProjection> findAll(Pageable pageable);
DetallePacienteDTO update(UUID id, UpdatePacienteDTO updateDTO);

}
