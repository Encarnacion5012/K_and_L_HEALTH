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
import org.springframework.stereotype.Service;

import java.util.List;

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
            DetalleDoctorDto doctor = userClient.findById(createCitaDto.idDoctor());
            DetallePacienteDto pacienteDto = userClient.findByIdPaciente(createCitaDto.idPaciente());

            validaciones.forEach(v-> v.validarCreacionCita(createCitaDto, doctor));

            Cita cita = citaMapperService.createDtoToEntity(createCitaDto);

            citaRepository.save(cita);

            return citaMapperService.entitysToDetalleDto(doctor, pacienteDto, cita);


        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }


    }
}
