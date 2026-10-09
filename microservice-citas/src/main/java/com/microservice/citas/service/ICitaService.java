package com.microservice.citas.service;

import com.microservice.citas.dto.CreateCitaDto;
import com.microservice.citas.dto.DetalleCitaDto;
import com.microservice.citas.dto.ReagendarCitaDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ICitaService {
    DetalleCitaDto create (CreateCitaDto createCitaDto);
    Page<DetalleCitaDto> findAllAgendadaAndReagendada (Pageable pageable);
    void cancelarCita(UUID id_Cita);
    void reagendarCita(UUID id_Cita, ReagendarCitaDto reagendarCitaDto);

}
