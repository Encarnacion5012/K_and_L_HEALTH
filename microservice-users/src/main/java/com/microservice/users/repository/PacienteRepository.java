package com.microservice.users.repository;

import com.microservice.users.model.Paciente;
import com.microservice.users.projections.pacientes.DetallesPacienteProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.UUID;

public interface PacienteRepository extends JpaRepository<Paciente, UUID> {

    @Query(value = """
        SELECT 
        p.idPaciente, p.nombre, p.apellido,p.email, p.cedula, p.direccion, p.sexo, p.fechaNacimiento, p.telefono        
         FROM Paciente p
    """)
    Page<DetallesPacienteProjection> findAllPacientes(Pageable pageable);


}


