package com.microservice.users.service.doctor;

import com.microservice.users.dto.doctor.CreateDcotorDto;
import com.microservice.users.dto.doctor.DetalleDoctorDto;
import com.microservice.users.mapper.DoctorMapperService;
import com.microservice.users.mapper.EspecialidadMappperService;
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
    private final EspecialidadMappperService especialidadMappperService;
    private final HorarioATencionMapperService horarioATencionMapperService;
    private final HorarioAtencionRepository horarioAtencionRepository;
    private final EspecialidadRepository especialidadRepository;


    @Override
    @Transactional
    public DetalleDoctorDto createDoctor(CreateDcotorDto createDTO) {
        String uuidDoctor;
        try {
            uuidDoctor =  keycloakService.createUserDoctor(createDTO.userRegister());
        }catch (Exception e){

          throw new RuntimeException("Error creating user doctor " + e.getMessage(), e);

        }

        Doctor newDoctor = doctorMapperService.registerDtoToEntity(createDTO);
        newDoctor.setIdAuthentication(UUID.fromString(uuidDoctor));

        doctorRepository.save(newDoctor);

        Set<Especialidad> especialidades = new HashSet<>(especialidadRepository.findAllById(createDTO.idEspecialidades()));


        List<HorarioAtencion> horarioAtencion = createDTO.horarioAtencion().stream()
                .map(horarioATencionMapperService::registerDtoToEntity)
                .toList();

        newDoctor.helperRelacionHoracioATencios(horarioAtencion);

        newDoctor.helperRelacionEspecialidad(especialidades);

        horarioAtencionRepository.saveAll(horarioAtencion);





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
            throw new RuntimeException("Error deleting user doctor " + e.getMessage(), e);
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
            throw new RuntimeException("Error activating user doctor " + e.getMessage(), e);
        }
        doctor.reactivarDoctor();
    }

    @Override
    public DetallesDoctorProjection findAllByIdDoctor(UUID idDoctor) {
        return doctorRepository.findAllByIdDoctor(idDoctor)
                .orElseThrow(()-> new EntityNotFoundException("Doctor no encontrado con el id: " + idDoctor));
    }
}
