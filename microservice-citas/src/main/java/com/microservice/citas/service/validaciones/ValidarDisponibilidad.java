package com.microservice.citas.service.validaciones;

import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import com.microservice.citas.model.Estado_Cita;
import com.microservice.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidarDisponibilidad implements ValidarCreacionCita{
    private final CitaRepository citaRepository;

    @Override
    public void validarCreacionCita(CreateCitaDto createCitaDto, DetalleDoctorDto detalleDoctorDto) {
        boolean citaOcupada = citaRepository
                .existsByIdDoctorAndFechaHoaraCitaAndEstado(createCitaDto.idDoctor(), createCitaDto.fechaHoaraCita(), Estado_Cita.AGENDADA);
        if (citaOcupada) {
            throw new IllegalArgumentException("El doctor ya tiene una cita reservada en ese horario");
        }
        }
    }

