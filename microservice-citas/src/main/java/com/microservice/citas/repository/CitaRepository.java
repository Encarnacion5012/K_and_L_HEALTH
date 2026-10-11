package com.microservice.citas.repository;

import com.microservice.citas.model.Cita;
import com.microservice.citas.model.Estado_Cita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CitaRepository extends JpaRepository<Cita, UUID> {


    @Query(value = """
    SELECT  COUNT (c) >0 
    FROM Cita c 
    WHERE (
    (:id_Cita_actual IS NULL OR c.idCita !=:id_Cita_actual)
    AND(c.idDoctor=:id_doctor OR c.idPaciente=:id_paciente) 
    AND c.estado IN (com.microservice.citas.model.Estado_Cita.AGENDADA, com.microservice.citas.model.Estado_Cita.REAGENDADA)
    AND c.fechaHoraCita BETWEEN :fecha_Hora_Inicio_Consulta AND :fecha_Hora_nueva_Cita 
   
    ) 
""")
    boolean existenceAndTimeVerificationByIdDoctorAndIdPaciente(
            @Param("id_Cita_actual") UUID id_Cita_actual,
            @Param("id_doctor") UUID id_doctor,
            @Param("fecha_Hora_Inicio_Consulta") ZonedDateTime fecha_Hora_Inicio_Consulta,
            @Param("fecha_Hora_nueva_Cita") ZonedDateTime fecha_Hora_nueva_Cita,
            @Param("id_paciente") UUID id_paciente
            );

    @Query(value = """
    SELECT c FROM Cita c 
    WHERE c.estado IN (com.microservice.citas.model.Estado_Cita.AGENDADA, com.microservice.citas.model.Estado_Cita.REAGENDADA)
    """)
    Page<Cita> findAllAgendadaAndReagendada(Pageable pageable);

    @Query(value = """
    SELECT c FROM Cita c 
    where c.idCita = :id_Cita
    AND c.estado IN (com.microservice.citas.model.Estado_Cita.AGENDADA, com.microservice.citas.model.Estado_Cita.REAGENDADA)
           
    """)
    Optional<Cita> findByIdAndEstadoAgendadaAndReagendada(@Param("id_Cita") UUID id_Cita) ;

}
