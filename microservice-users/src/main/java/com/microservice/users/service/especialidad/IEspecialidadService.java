package com.microservice.users.service.especialidad;

import com.microservice.users.dto.especialidad.CreateEspecialidadDto;
import com.microservice.users.dto.especialidad.DetalleEspecialidadDto;
import com.microservice.users.projections.doctor.DetallesEspecialidadProjection;

import java.util.List;

public interface IEspecialidadService {
    DetalleEspecialidadDto createEspcialidad(CreateEspecialidadDto createEspecialidadDto);

    List<DetallesEspecialidadProjection> findAll();
}
