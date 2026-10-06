package com.microservice.users.service.pacientes;

import com.microservice.users.dto.paciente.CreatePacienteDto;
import com.microservice.users.dto.paciente.DetallePacienteDto;
import com.microservice.users.dto.paciente.UpdatePacienteDto;
import com.microservice.users.dto.user.UpdateUserWithNoRolesDto;
import com.microservice.users.exception.custom.AuthIntegrationException;
import com.microservice.users.exception.custom.CriticalRollbackException;
import com.microservice.users.exception.custom.PersistenceCompensatedException;
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


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements IPacienteService {

    private final PacienteMapperService pacienteMapperService;
    private final PacienteRepository pacienteRepository;
    private final IkeycloakService keycloakService;

    @Override
    @Transactional
    public DetallePacienteDto create(CreatePacienteDto createPacienteDTO) {

       String idAuthorizationNewUser = keycloakService.createUserPacinte(createPacienteDTO.userRegisterDto());
        if (idAuthorizationNewUser == null) {
            throw new EntityNotFoundException("No se puede crear un Paciente, ha habido un error");
        }
        Paciente nuevoPaciente = pacienteMapperService.createDTOtoEntity(createPacienteDTO);
        try {
            nuevoPaciente.setIdAuthentication(UUID.fromString(idAuthorizationNewUser));

            pacienteRepository.saveAndFlush(nuevoPaciente);

        }catch (Exception e){
            try{
                keycloakService.deleteUser(idAuthorizationNewUser);
            }catch (Exception ex){
                throw new CriticalRollbackException("Error al eliminar el Paciente con id_autorization: " + idAuthorizationNewUser, ex);
            }

            throw new PersistenceCompensatedException("Hubo un problema al crear el nuevo Paciente, se ha hecho el rolback correctamente", e);
        }
        return pacienteMapperService.entityToDTO(nuevoPaciente);
    }

    @Override
    public Page<DetallesPacienteProjection> findAllActivo(Pageable pageable) {
        return pacienteRepository.findAllByActivoTrue(pageable);
    }

    @Override
    @Transactional
    public DetallePacienteDto update(UUID id, UpdatePacienteDto updateDTO) {

        Paciente paciente = findEntityByIdHelper(id);
        UpdateUserWithNoRolesDto snapshotAntesDeActulizacion;

        try{
             snapshotAntesDeActulizacion = keycloakService.getUserUpdateData(paciente.getIdAuthentication().toString());
        } catch (Exception e) {
            throw new AuthIntegrationException("Error al obtener el estado previo antes de actulizar con el usuario: " + paciente.getIdAuthentication().toString(), e);
        }

        try{
            keycloakService.updateUserNoRoles(paciente.getIdAuthentication().toString(), updateDTO.userUpdate());
        }catch (Exception e){
            throw new AuthIntegrationException("Error al actualizar el Paciente con id_autorization: " + paciente.getIdAuthentication(), e);
        }


        try {
            pacienteMapperService.updatePaciente(updateDTO, paciente);
            pacienteRepository.flush();

        } catch (Exception e) {
            try{
                keycloakService.updateUserNoRoles(paciente.getIdAuthentication().toString(), snapshotAntesDeActulizacion);
            } catch (Exception ex) {
                throw new CriticalRollbackException("Ha habido un error al hacer rollback del Paciente", ex);
            }

            throw new PersistenceCompensatedException("Ha ocurrido un error al actulizar la data del paciente", e);
        }


        return pacienteMapperService.entityToDTO(paciente);
    }


    private Paciente findEntityByIdHelper(UUID id) {
        return pacienteRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Paciente no encontrado con el id: " + id));
    }

    @Override
    @Transactional
    public void desactivarPaciente(UUID id) {
        Paciente paciente = findEntityByIdHelper(id);

        try {
            keycloakService.changeEnabledState(paciente.getIdAuthentication().toString(), false);
        }catch (Exception e){
            throw new AuthIntegrationException("Error al deactivar el Paciente: " + paciente.getIdAuthentication(), e);
        }
        paciente.desactivarPaciente();
    }

    @Override
    @Transactional
    public void reactivarPaciente(UUID id) {
        Paciente paciente = findEntityByIdHelper(id);

        try {
            keycloakService.changeEnabledState(paciente.getIdAuthentication().toString(), true);
        }catch (Exception e){
            throw new AuthIntegrationException("Error al reactivar el Paciente:  " + paciente.getIdAuthentication(), e);
        }

        paciente.reactivarPaciente();
    }

    @Override
    public DetallesPacienteProjection getByIdPaciente(UUID idPaciente) {
        return pacienteRepository.findByIdPacienteAndActivoTrue(idPaciente)
                .orElseThrow(()-> new EntityNotFoundException("Paciente no encontrado"));
    }

    @Override
    public DetallesPacienteProjection findByEmail(String email) {
        return pacienteRepository.findByActivoTrueAndEmail(email)
                .orElseThrow(()-> new EntityNotFoundException("Paciente no encontrado con email: " + email));
    }

    @Override
    public List<DetallesPacienteProjection> findAllByTelefono(String telefono) {
        return pacienteRepository.findAllByActivoTrueAndTelefono(telefono)
                .orElseThrow(()-> new EntityNotFoundException("Ningun Paciente no encontrado con telefono: " + telefono));
    }

    @Override
    public Paciente findByIdAuthentication(UUID id_authentication) {
        return pacienteRepository.findByIdAuthentication(id_authentication)
                .orElseThrow(()-> new EntityNotFoundException("Paciente no encontrado con authentication_id: " + id_authentication));
    }

    @Override
    public DetallesPacienteProjection findByCedula(String cedula) {
        return pacienteRepository.findByCedulaAndActivoTrue(cedula)
                .orElseThrow(()-> new EntityNotFoundException("Ningun paciente encntrado con la cedula: " + cedula));
    }

    @Override
    public boolean existsByIdPaciente(UUID idPaciente) {
        return pacienteRepository.existsByIdPaciente(idPaciente);
    }
}
