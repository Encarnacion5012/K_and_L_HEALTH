package com.microservice.users.repository;

import com.microservice.users.model.Paciente;
import com.microservice.users.projections.pacientes.DetallesPacienteProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface PacienteRepository extends JpaRepository<Paciente, UUID> {

    @Query(value = """
        SELECT 
        p      
         FROM Paciente p
    """)
    Page<DetallesPacienteProjection> findAllPacientes(Pageable pageable);


    Optional<Paciente> findByIdAuthentication(UUID uuid);
    Optional<Paciente> findByCedula(String cedula);
}


