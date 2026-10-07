package com.microservice.citas.repository;

import com.microservice.citas.model.Cita;
import com.microservice.citas.model.Estado_Cita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CitaRepository extends CrudRepository<Cita, UUID> {

    boolean existsByIdDoctorAndFechaHoaraCitaAndEstado(UUID idDoctor, LocalDateTime fechaHoaraCita, Estado_Cita estado);
    boolean existsByIdPacienteAfterAndFechaHoaraCitaAndEstado(UUID idDoctor, LocalDateTime fechaHoaraCita, Estado_Cita estado);

    @Query(value = """
    SELECT c FROM Cita c 
    WHERE c.estado = com.microservice.citas.model.Estado_Cita.AGENDADA
    or c.estado =  com.microservice.citas.model.Estado_Cita.REAGENDADA
""")
    Page<Cita> findAllAgendadaAndReagendada(Pageable pageable);
}
