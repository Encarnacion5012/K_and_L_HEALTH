package com.microservice.citas.service;

import com.microservice.citas.clients.UserClient;
import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.dto.DetalleCitaDto;
import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import com.microservice.citas.http.response.paciente.DetallePacienteDto;
import com.microservice.citas.mapper.CitaMapperService;
import com.microservice.citas.model.Cita;
import com.microservice.citas.repository.CitaRepository;
import com.microservice.citas.service.validaciones.ValidarCreacionCita;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaServiceImp implements ICitaService{
    private final UserClient userClient;
    private final CitaRepository citaRepository;
    private final List<ValidarCreacionCita> validaciones;
    private final CitaMapperService citaMapperService;

    @Transactional
    @Override
    public DetalleCitaDto create(CreateCitaDto createCitaDto) {

        try {

            DetallePacienteDto pacienteDto = userClient.findByIdPaciente(createCitaDto.idPaciente());
            DetalleDoctorDto doctor = userClient.findDoctorById(createCitaDto.idDoctor());

            validaciones.forEach(v-> v.validarCreacionCita(createCitaDto, doctor));

            Cita cita = citaMapperService.createDtoToEntity(createCitaDto);

            citaRepository.save(cita);

            return citaMapperService.entitysToDetalleDto(doctor, pacienteDto, cita);


        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }


    }

    @Override
    public Page<DetalleCitaDto> findAllAgendadaAndReagendada(Pageable pageable) {
        Page<Cita> citas = citaRepository.findAllAgendadaAndReagendada(pageable);

        Set<UUID> id_Pacientes = citas
                .map(Cita::getIdPaciente)
                .toSet();

        Set<UUID>id_Doctors = citas
                .map(Cita::getIdDoctor)
                .toSet();
        Map<UUID, DetallePacienteDto> pacienteDtoMap = id_Pacientes.stream()
                .collect(Collectors.toMap(id-> id, userClient::findByIdPaciente));

        Map<UUID, DetalleDoctorDto> doctorDtoMap = id_Doctors.stream()
                .collect(Collectors.toMap(id-> id, userClient::findDoctorById));

        return citas
                .map(c-> citaMapperService.entitysToDetalleDto(
                        doctorDtoMap.get(c.getIdDoctor()),
                        pacienteDtoMap.get(c.getIdPaciente()),
                        c
                )) ;
    }


    private DetalleCitaDto helperParaArmarDto(Cita cita, DetallePacienteDto pacienteDto, DetalleDoctorDto doctorDto) {
        return citaMapperService.entitysToDetalleDto(doctorDto, pacienteDto, cita);
    }


}
