package com.microservice.users.service.doctor;

import com.microservice.users.dto.doctor.CreateDcotorDto;
import com.microservice.users.dto.doctor.DetalleDoctorDto;
import com.microservice.users.dto.doctor.UpdateDoctorDto;
import com.microservice.users.dto.user.UpdateUserWithNoRolesDto;
import com.microservice.users.exception.custom.AuthIntegrationException;
import com.microservice.users.exception.custom.CriticalRollbackException;
import com.microservice.users.exception.custom.PersistenceCompensatedException;
import com.microservice.users.mapper.DoctorMapperService;
import com.microservice.users.mapper.HorarioATencionMapperService;
import com.microservice.users.model.Doctor;
import com.microservice.users.model.Especialidad;
import com.microservice.users.model.HorarioAtencion;
import com.microservice.users.projections.doctor.DetallesDoctorProjection;
import com.microservice.users.repository.DoctorRepository;
import com.microservice.users.repository.EspecialidadRepository;
import com.microservice.users.repository.HorarioAtencionRepository;
import com.microservice.users.service.keycloak.IkeycloakService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements IDoctorService {

    private  final DoctorRepository doctorRepository;
    private final DoctorMapperService doctorMapperService;
    private final IkeycloakService keycloakService;
    private final HorarioATencionMapperService horarioATencionMapperService;
    private final HorarioAtencionRepository horarioAtencionRepository;
    private final EspecialidadRepository especialidadRepository;


    @Override
    @Transactional
    public DetalleDoctorDto createDoctor(CreateDcotorDto createDTO) {
        String newIdAuthenticationDoctor = keycloakService.createUserDoctor(createDTO.userRegister());

        if (newIdAuthenticationDoctor == null) {
            throw new EntityNotFoundException("Hubo un error al crear el nuevo doctor en el servicio de auntenticacion, el id de autenticacion es nulo");
        }


        Doctor newDoctor = doctorMapperService.registerDtoToEntity(createDTO);
        try{
            newDoctor.setIdAuthentication(UUID.fromString(newIdAuthenticationDoctor));

            doctorRepository.saveAndFlush(newDoctor);

            Set<Especialidad> especialidades = new HashSet<>(especialidadRepository.findAllById(createDTO.idEspecialidades()));


            List<HorarioAtencion> horarioAtencion = createDTO.horarioAtencion().stream()
                    .map(horarioATencionMapperService::registerDtoToEntity)
                    .toList();

            newDoctor.helperRelacionHoracioATencios(horarioAtencion);

            newDoctor.helperRelacionEspecialidad(especialidades);

            horarioAtencionRepository.saveAllAndFlush(horarioAtencion);
        } catch (Exception e) {
            try{
                keycloakService.deleteUser(newIdAuthenticationDoctor);
            }catch (Exception ex){
                throw new CriticalRollbackException("Error al eliminar el doctor con id_autorization: " + newIdAuthenticationDoctor, ex);
            }

            throw new PersistenceCompensatedException("Hubo un problema al crear el nuevo doctor, se ha hecho el rolback correctamente",e);
        }

        return doctorMapperService.entityToDto(newDoctor);
    }


    @Override
    public Page<DetallesDoctorProjection> findAllACtivo(Pageable pageable) {
        return doctorRepository.findAllByActivoTrue(pageable);
    }

    private Doctor findEntityByIdHelper(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Doctor no encontrado con el id: " + id));
    }


    @Override
    @Transactional
    public void desactivarDoctor(UUID idDoctor) {


        Doctor doctor = findEntityByIdHelper(idDoctor);

        try {
            keycloakService.changeEnabledState(doctor.getIdAuthentication().toString(), false);
        }catch (Exception e){
            throw new AuthIntegrationException("Error al deactivar user doctor " + e.getMessage(), e);
        }

        doctor.desactivarDoctor();
    }

    @Override
    @Transactional
    public void reactivarDoctor(UUID idDoctor) {
        Doctor doctor = findEntityByIdHelper(idDoctor);

        try {
            keycloakService.changeEnabledState(doctor.getIdAuthentication().toString(), true);
        }catch (Exception e){
            throw new AuthIntegrationException("Error activating user doctor", e);
        }
        doctor.reactivarDoctor();
    }

    @Override
    public DetallesDoctorProjection findAllByIdDoctor(UUID idDoctor) {
        return doctorRepository.findAllByIdDoctor(idDoctor)
                .orElseThrow(()-> new EntityNotFoundException("Doctor no encontrado con el id: " + idDoctor));
    }

    @Override
    public Page<DetallesDoctorProjection> findALlByEspecialidad(String nombreEspecialidad, Pageable pageable) {
        return doctorRepository.findAllByEspecialidades_NombreEspecialidad(nombreEspecialidad, pageable);
    }

    @Override
    public DetallesDoctorProjection findByExequatur(String exequatur) {
        return doctorRepository.findByExequatur(exequatur)
                .orElseThrow(()-> new EntityNotFoundException("Doctor no encontrado con el exequatur: " + exequatur));
    }

    @Override
    public DetallesDoctorProjection findBYIdAuthentication(UUID idAuthentication) {
        return doctorRepository.findByIdAuthentication(idAuthentication)
                .orElseThrow(()-> new EntityNotFoundException("Doctor no encontrado con el id_Authentication: " + idAuthentication));
    }

    @Override
    public DetallesDoctorProjection findByCedula(String cedula) {
        return doctorRepository.findByCedula(cedula)
                .orElseThrow(()-> new EntityNotFoundException("Doctor no encontrado con cedula: " + cedula));
    }

    @Override
    @Transactional
    public DetalleDoctorDto updateDoctor(UUID idDoctor, UpdateDoctorDto updateDTO) {
        UpdateUserWithNoRolesDto snapShot;
        Doctor doctor = findEntityByIdHelper(idDoctor);;

        try {
           snapShot = keycloakService.getUserUpdateData(doctor.getIdAuthentication().toString());
        }catch (Exception e){
            throw new AuthIntegrationException("Error usuario no encontrado en keycloak, id: "  +  idDoctor.toString());
        }

        try {
            keycloakService.updateUserNoRoles(doctor.getIdAuthentication().toString(), updateDTO.userUpdate());
        } catch (Exception e) {
            throw new AuthIntegrationException("Error al actualizar el Doctor en keycloak con id_auth: " + doctor.getIdAuthentication());
        }

        try {

            doctorMapperService.update(updateDTO, doctor);

            if (updateDTO.id_Especialidades() != null){
                Set<Especialidad> nuevasEspecialidades = new HashSet<>(especialidadRepository.findAllById(updateDTO.id_Especialidades())    );
                if  (updateDTO.id_Especialidades().size() != nuevasEspecialidades.size()){
                    throw new EntityNotFoundException("Hubo alguna especialidad no encontrada");
                }

                doctor.getEspecialidades().clear();
                doctor.getEspecialidades().addAll(nuevasEspecialidades);
            }

            doctorRepository.flush();
        }catch (Exception e){

            try {
                keycloakService.updateUserNoRoles(doctor.getIdAuthentication().toString(), snapShot);
            }catch (Exception ex){
                throw new CriticalRollbackException("Error critico hacer rolback en keycloak al usuario con id: " + doctor.getIdAuthentication().toString(), ex);
            }

            throw new PersistenceCompensatedException("Ha ocurrido un errror durante la actulizacion de usuario del usuarios :" + idDoctor.toString() + "el rolback ha sido exitoso", e);
        }
        return  doctorMapperService.entityToDto(doctor);


    }

    @Override
    public boolean existsByIdDoctor(UUID idDoctor) {
        return doctorRepository.existsByIdDoctor(idDoctor);
    }
}
