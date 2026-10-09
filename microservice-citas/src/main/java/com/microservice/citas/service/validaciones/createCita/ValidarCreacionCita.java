package com.microservice.citas.service.validaciones.createCita;

import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.http.response.doctor.DetalleDoctorDto;

public interface ValidarCreacionCita {
    void validarCreacionCita(CreateCitaDto createCitaDto, DetalleDoctorDto detalleDoctorDto);
}
