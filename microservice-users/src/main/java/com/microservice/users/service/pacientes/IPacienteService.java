package com.microservice.users.service.pacientes;

import com.microservice.users.dto.paciente.DetallePacienteDTO;
import com.microservice.users.dto.paciente.RegistroPacienteDTO;

public interface IPacienteService {
DetallePacienteDTO create(RegistroPacienteDTO registroPacienteDTO);


}
