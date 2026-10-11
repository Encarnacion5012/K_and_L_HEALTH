package com.microservice.citas.service.validaciones.reajendar;

import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import com.microservice.citas.model.Cita;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ValidarReagendaCita {
    void validarReagendaCita(UUID id_CitaActual,
                             Cita cita ,
                             LocalDateTime fecha_Nueva_Consulta
                             );
}
