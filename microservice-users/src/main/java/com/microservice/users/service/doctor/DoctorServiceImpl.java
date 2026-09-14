package com.microservice.users.service.doctor;

import com.microservice.users.dto.doctor.CreateDcotorDTO;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public DetalleDoctorDto createDoctor(CreateDcotorDTO createDTO) {
        String uuidDoctor;
        try {
            uuidDoctor =  keycloakService.createUserDoctor(createDTO.userRegister());
        }catch (Exception e){

          throw new RuntimeException("Error creating user doctor", e.getCause());

        }

        Doctor newDoctor = doctorMapperService.registerDtoToEntity(createDTO);
        newDoctor.setIdAuthentication(UUID.fromString(uuidDoctor));

        doctorRepository.save(newDoctor);

        Set<Especialidad> especialidades = createDTO.especialidades().stream()
                .map(especialidadMappperService::createDtoTOEntity)
                .collect(Collectors.toSet());

        especialidadRepository.saveAll(especialidades);

        List<HorarioAtencion> horarioAtencion = createDTO.horarioAtencion().stream()
                .map(horarioATencionMapperService::registerDtoToEntity)
                .toList();

        horarioAtencionRepository.saveAll(horarioAtencion);


        newDoctor.helperRelacionEspecialidad(especialidades);

        newDoctor.helperRelacionHoracioATencios(horarioAtencion);

        return doctorMapperService.entityToDto(newDoctor);
    }
}
