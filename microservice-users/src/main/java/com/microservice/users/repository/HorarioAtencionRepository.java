package com.microservice.users.repository;

import com.microservice.users.model.HorarioAtencion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HorarioAtencionRepository extends JpaRepository<HorarioAtencion, Integer> {
    List<HorarioAtencion> findAllByDoctor_IdDoctor(UUID doctorId);
}
