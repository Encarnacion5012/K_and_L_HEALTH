package com.microservice.citas.repository;

import com.microservice.citas.model.Cita;
import com.microservice.citas.model.Estado_Cita;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CitaRepository extends CrudRepository<Cita, UUID> {

    boolean existsByIdDoctorAndFechaHoaraCitaAndEstado(UUID idDoctor, LocalDateTime fechaHoaraCita, Estado_Cita estado);
    boolean existsByIdPacienteAfterAndFechaHoaraCitaAndEstado(UUID idDoctor, LocalDateTime fechaHoaraCita, Estado_Cita estado);
}
