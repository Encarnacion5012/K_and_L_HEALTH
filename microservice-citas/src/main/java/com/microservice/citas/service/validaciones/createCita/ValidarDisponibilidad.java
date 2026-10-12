package com.microservice.citas.service.validaciones.createCita;

import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import com.microservice.citas.model.Estado_Cita;
import com.microservice.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class ValidarDisponibilidad implements ValidarCreacionCita{
    private final CitaRepository citaRepository;

    @Override
    public void validarCreacionCita(CreateCitaDto createCitaDto, DetalleDoctorDto detalleDoctorDto) {
        ZonedDateTime fechaInicioConsulta = ZonedDateTime.now().minusMinutes(30);

        boolean citaOcupada = citaRepository
                .existenceAndTimeVerificationByIdDoctorAndIdPaciente(
                        null,
                        createCitaDto.idDoctor(),
                        fechaInicioConsulta,
                        createCitaDto.fechaHoraCita(),
                        createCitaDto.idPaciente()
                );

        if (citaOcupada) {
            throw new IllegalArgumentException("El doctor o el paciente ya tienen una cita reservada a" +
                    " menos de 30 minutos de la fechaHora deseada");
        }
        }
    }

