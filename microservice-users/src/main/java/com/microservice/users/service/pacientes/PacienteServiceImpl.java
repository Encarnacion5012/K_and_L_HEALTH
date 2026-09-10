package com.microservice.users.service.pacientes;

import com.microservice.users.dto.paciente.DetallePacienteDTO;
import com.microservice.users.dto.paciente.RegistroPacienteDTO;
import com.microservice.users.dto.paciente.UpdatePacienteDTO;
import com.microservice.users.mapper.PacienteMapperService;
import com.microservice.users.model.Paciente;
import com.microservice.users.projections.pacientes.DetallesPacienteProjection;
import com.microservice.users.repository.PacienteRepository;
import com.microservice.users.service.keycloak.IkeycloakService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements IPacienteService {

    private final PacienteMapperService pacienteMapperService;
    private final PacienteRepository pacienteRepository;
    private final IkeycloakService keycloakService;

    @Override
    @Transactional
    public DetallePacienteDTO create(RegistroPacienteDTO registroPacienteDTO) {

       UUID idAuthorizationNewUser = UUID.fromString(keycloakService.createUserPacinte(registroPacienteDTO.userRegisterDto())) ;

        Paciente nuevoPaciente;
        nuevoPaciente = pacienteMapperService.createDTOtoEntity(registroPacienteDTO);

        if (idAuthorizationNewUser == null) {
            throw new RuntimeException("No se puede crear un Paciente, ha habido un error");
        }
        nuevoPaciente.setIdAuthentication(idAuthorizationNewUser);

        pacienteRepository.save(nuevoPaciente);
       return pacienteMapperService.entityToDTO(nuevoPaciente);

    }

    @Override
    public Page<DetallesPacienteProjection> findAll(Pageable pageable) {
        return pacienteRepository.findAllPacientes(pageable);
    }

    @Override
    @Transactional
    public DetallePacienteDTO update(UUID id, UpdatePacienteDTO updateDTO) {

        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Paciente no encontrado"));


        keycloakService.updateUserNoRoles(paciente.getIdAuthentication().toString(), updateDTO.userUpdate());

        pacienteMapperService.updatePaciente(updateDTO, paciente);


        return pacienteMapperService.entityToDTO(paciente);
    }
}
