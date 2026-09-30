package com.microservice.users.repository;

import com.microservice.users.model.Paciente;
import com.microservice.users.projections.pacientes.DetallesPacienteProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PacienteRepository extends JpaRepository<Paciente, UUID> {


    Page<DetallesPacienteProjection> findAllByActivoTrue(Pageable pageable);


    Optional<Paciente> findByIdAuthentication(UUID uuid);
    Optional<DetallesPacienteProjection> findByCedulaAndActivoTrue(String cedula);
    Optional<DetallesPacienteProjection> findByIdPacienteAndActivoTrue(UUID idPaciente);
    Optional<DetallesPacienteProjection> findByActivoTrueAndEmail(String email);
    Optional<List<DetallesPacienteProjection>> findAllByActivoTrueAndTelefono(String telefono);

    boolean existsByIdPaciente(UUID idPaciente);

}


