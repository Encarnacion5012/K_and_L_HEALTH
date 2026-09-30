package com.microservice.citas.repository;

import com.microservice.citas.model.Cita;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CitaRepository extends CrudRepository<Cita, UUID> {
}
