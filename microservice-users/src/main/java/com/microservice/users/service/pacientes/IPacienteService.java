package com.microservice.users.service.pacientes;

import com.microservice.users.dto.paciente.DetallePacienteDTO;
import com.microservice.users.dto.paciente.RegistroPacienteDTO;
import com.microservice.users.dto.paciente.UpdatePacienteDTO;
import com.microservice.users.projections.pacientes.DetallesPacienteProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;


public interface IPacienteService {
DetallePacienteDTO create(RegistroPacienteDTO registroPacienteDTO);
Page<DetallesPacienteProjection> findAll(Pageable pageable);
DetallePacienteDTO update(UUID id, UpdatePacienteDTO updateDTO);

}
