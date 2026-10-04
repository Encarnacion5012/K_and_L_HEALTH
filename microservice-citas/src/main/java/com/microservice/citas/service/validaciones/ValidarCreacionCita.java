package com.microservice.citas.service.validaciones;

import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import com.microservice.citas.model.Cita;
import com.microservice.citas.repository.CitaRepository;

public interface ValidarCreacionCita {
    void validarCreacionCita(CreateCitaDto createCitaDto, DetalleDoctorDto detalleDoctorDto);
}
