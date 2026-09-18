package com.microservice.users.repository;

import com.microservice.users.model.Especialidad;
import com.microservice.users.projections.doctor.DetallesEspecialidadProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Integer> {


    @Query(value = "SELECT e.idEspecialidad as idEspecialidad, e.nombreEspecialidad as nombreEspecialidad FROM Especialidad e")
    List<DetallesEspecialidadProjection> findAllProjection();
}
