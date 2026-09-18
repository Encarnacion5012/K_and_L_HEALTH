package com.microservice.users.repository;

import com.microservice.users.model.Doctor;
import com.microservice.users.projections.doctor.DetallesDoctorProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    @EntityGraph(attributePaths = {"horarioAtencion", "especialidades"})
    Page<DetallesDoctorProjection> findAllByActivoTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"horarioAtencion", "especialidades"})
    Optional<DetallesDoctorProjection> findAllByIdDoctor(UUID idDoctor);
}
