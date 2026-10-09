package com.microservice.citas.service.validaciones.reajendar;

import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ValidarReagendaCita {
    void validarReagendaCita(UUID id_CitaActual,UUID id_doctor,
                             LocalDateTime fecha_Nueva_Consulta,
                             UUID id_paciente);
}
