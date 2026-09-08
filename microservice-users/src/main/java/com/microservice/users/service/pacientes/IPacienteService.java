package com.microservice.users.service.pacientes;

import com.microservice.users.dto.paciente.DetallePacienteDTO;
import com.microservice.users.dto.paciente.RegistroPacienteDTO;
import com.microservice.users.model.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IPacienteService {
DetallePacienteDTO create(RegistroPacienteDTO registroPacienteDTO);
Page<DetallePacienteDTO> findAll(Pageable pageable);

}
