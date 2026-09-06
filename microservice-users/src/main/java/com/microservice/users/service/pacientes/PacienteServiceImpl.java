package com.microservice.users.service.pacientes;

import com.microservice.users.dto.paciente.DetallePacienteDTO;
import com.microservice.users.dto.paciente.RegistroPacienteDTO;
import com.microservice.users.mapper.PacienteMapperService;
import com.microservice.users.model.Paciente;
import com.microservice.users.repository.PacienteRepository;
import com.microservice.users.service.keycloak.IkeycloakService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements IPacienteService {

    private PacienteMapperService pacienteMapperService;
    private PacienteRepository pacienteRepository;
    private IkeycloakService keycloakService;

    @Override
    @Transactional
    public DetallePacienteDTO create(RegistroPacienteDTO registroPacienteDTO) {

       UUID idAuthorizationNewUser = keycloakService.createUser(registroPacienteDTO.userRegisterDto());

        Paciente nuevoPaciente = new Paciente();
        nuevoPaciente = pacienteMapperService.createDTOtoEntity(registroPacienteDTO);

        if (idAuthorizationNewUser != null) {
            nuevoPaciente.setIdAuthentication(idAuthorizationNewUser);
        }else {
            throw new RuntimeException("No se puede crear un Paciente, ha habido un error");
        }

        pacienteRepository.save(nuevoPaciente);
       return pacienteMapperService.entityToDTO(nuevoPaciente);

    }
}
