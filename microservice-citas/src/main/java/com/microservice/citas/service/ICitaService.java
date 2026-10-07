package com.microservice.citas.service;

import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.dto.DetalleCitaDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICitaService {
    DetalleCitaDto create (CreateCitaDto createCitaDto);
    Page<DetalleCitaDto> findAllAgendadaAndReagendada (Pageable pageable);
}
