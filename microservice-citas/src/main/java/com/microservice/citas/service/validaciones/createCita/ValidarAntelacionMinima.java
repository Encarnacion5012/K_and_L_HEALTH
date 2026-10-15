package com.microservice.citas.service.validaciones.createCita;

import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.exception.custom.AntelacionValidationException;
import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Component
public class ValidarAntelacionMinima implements ValidarCreacionCita {
    @Override
    public void validarCreacionCita(CreateCitaDto createCitaDto, DetalleDoctorDto detalleDoctorDto) {
        ZonedDateTime horaMinimaAntelacion = ZonedDateTime.now().plusHours(1);
        if (createCitaDto.fechaHoraCita().isBefore(horaMinimaAntelacion)) {
            throw new AntelacionValidationException("Las citas deben agendarse con una hora de antelacion");
        }
    }
}
