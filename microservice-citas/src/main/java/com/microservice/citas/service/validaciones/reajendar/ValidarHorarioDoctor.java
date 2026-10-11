package com.microservice.citas.service.validaciones.reajendar;

import com.microservice.citas.clients.UserClient;
import com.microservice.citas.http.response.doctor.DetalleHorarioAtencionDto;
import com.microservice.citas.model.Cita;
import com.microservice.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor

public class ValidarHorarioDoctor implements ValidarReagendaCita{

    private final UserClient userClient;

    @Override
    public void validarReagendaCita(UUID id_CitaActual, Cita cita, ZonedDateTime fecha_Nueva_Consulta) {

        DayOfWeek diaCita = fecha_Nueva_Consulta.getDayOfWeek();
        LocalTime horaCita = fecha_Nueva_Consulta.toLocalTime();

        List<DetalleHorarioAtencionDto> horariosAtencionDoctor = userClient.findDoctorById(cita.getIdDoctor()).horarioAtencion();

        DetalleHorarioAtencionDto horarioValido = horariosAtencionDoctor.stream()
                .filter(h-> h.diaDeSemana().equals(diaCita))
                .findFirst()
                .orElseThrow(()-> new RuntimeException("EL doctor deseado no trabaja los dias: " + diaCita));

        boolean horaValida = horaCita.isAfter(horarioValido.horaInicio()) && horaCita.isBefore(horarioValido.horaFin());

        if (!horaValida) {
            throw new IllegalArgumentException("La hora seleccionada (" + horaCita +
                    ") está fuera del horario de atención del doctor (" +
                    horarioValido.horaInicio() + " - " + horarioValido.horaFin() + ")");
        }


    }
}
