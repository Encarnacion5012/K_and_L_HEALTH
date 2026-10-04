package com.microservice.citas.service;

import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.dto.DetalleCitaDto;

public interface ICitaService {
    DetalleCitaDto create (CreateCitaDto createCitaDto);
}
