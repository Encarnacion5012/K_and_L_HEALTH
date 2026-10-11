package com.microservice.citas.service.validaciones.reajendar;


import com.microservice.citas.model.Cita;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ValidarAntelacionTiempoReagenda implements ValidarReagendaCita{
    @Override
    public void validarReagendaCita(UUID id_CitaActual, Cita cita, LocalDateTime fecha_Nueva_Consulta) {

       LocalDateTime ahora = LocalDateTime.now().plusHours(1);

        if (ahora.isAfter(fecha_Nueva_Consulta)) {
            throw new IllegalArgumentException("La ciata debe agendarse con al menos 1 hora de antelacion");
        }

    }
}
