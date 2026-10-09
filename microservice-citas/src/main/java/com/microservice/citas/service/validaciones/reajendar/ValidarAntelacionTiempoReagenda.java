package com.microservice.citas.service.validaciones.reajendar;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class ValidarAntelacionTiempoReagenda implements ValidarReagendaCita{
    @Override
    public void validarReagendaCita( UUID id_CitaActual,UUID id_doctor, LocalDateTime fecha_Nueva_Consulta, UUID id_paciente) {

       LocalDateTime ahora = LocalDateTime.now().plusHours(1);

        if (ahora.isAfter(fecha_Nueva_Consulta)) {
            throw new IllegalArgumentException("La ciata debe agendarse con al menos 1 hora de antelacion");
        }

    }
}
