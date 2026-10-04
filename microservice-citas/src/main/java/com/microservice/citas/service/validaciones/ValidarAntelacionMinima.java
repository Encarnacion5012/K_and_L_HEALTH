package com.microservice.citas.service.validaciones;

import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import com.microservice.citas.repository.CitaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ValidarAntelacionMinima implements ValidarCreacionCita{
    @Override
    public void validarCreacionCita(CreateCitaDto createCitaDto, DetalleDoctorDto detalleDoctorDto) {
        LocalDateTime ahora = LocalDateTime.now();
        if (!createCitaDto.fechaHoaraCita().isBefore(ahora.plusHours(1))) {
            throw new RuntimeException("Las citas deben agendarse con una hora de antelacion");
        }
    }
}
