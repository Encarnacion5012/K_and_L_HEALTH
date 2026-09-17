package com.microservice.users.repository;

import com.microservice.users.model.Doctor;
import com.microservice.users.projections.doctor.DetallesDoctorProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    Page<DetallesDoctorProjection> findAllByActivoTrue(Pageable pageable);
}
