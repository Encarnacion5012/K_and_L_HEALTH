package com.microservice.users.service.pacientes;

import com.microservice.users.dto.paciente.CreatePacienteDto;
import com.microservice.users.dto.paciente.DetallePacienteDto;
import com.microservice.users.dto.paciente.UpdatePacienteDto;
import com.microservice.users.projections.pacientes.DetallesPacienteProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;


public interface IPacienteService {
DetallePacienteDto create(CreatePacienteDto createPacienteDTO);
Page<DetallesPacienteProjection> findAll(Pageable pageable);
DetallePacienteDto update(UUID id, UpdatePacienteDto updateDTO);

}
