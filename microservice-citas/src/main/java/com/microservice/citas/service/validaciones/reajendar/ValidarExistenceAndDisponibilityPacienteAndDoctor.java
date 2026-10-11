package com.microservice.citas.service.validaciones.reajendar;

import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import com.microservice.citas.model.Cita;
import com.microservice.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ValidarExistenceAndDisponibilityPacienteAndDoctor implements  ValidarReagendaCita{

    private final CitaRepository citaRepository;


    @Override
    public void validarReagendaCita(UUID id_CitaActual, Cita cita, LocalDateTime fecha_Nueva_Consulta) {
        LocalDateTime fecha_Inicio = fecha_Nueva_Consulta.minusMinutes(30);


        boolean isUnavailable = citaRepository.existenceAndTimeVerificationByIdDoctorAndIdPaciente(
                id_CitaActual ,cita.getIdDoctor(), fecha_Inicio, fecha_Nueva_Consulta, cita.getIdPaciente()
        );

        if (isUnavailable){
            throw  new RuntimeException("NO se puede agendar la cita en este horario, el paciente o el medico tienen" +
                    " una cita agendada a menos de 30 minutos de esta");
        }
    }
}
