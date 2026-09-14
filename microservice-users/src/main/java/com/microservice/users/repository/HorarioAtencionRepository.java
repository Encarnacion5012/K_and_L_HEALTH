package com.microservice.users.repository;

import com.microservice.users.model.HorarioAtencion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioAtencionRepository extends JpaRepository<HorarioAtencion, Integer> {
}
